package org.example.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mavenr.email.MavenrQQEmail;
import com.mavenr.image.DownloadImageToFileByUrl;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.CommentInfo;
import org.example.entity.TemplateInfo;
import org.example.init.Config;
import org.example.util.Unit;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author mavenr
 * @Classname GetCommentNumService
 * @Description TODO
 * @Date 2023/6/13 16:57
 */
public class GetCommentNumService implements Job {

    private Unit unit = new Unit();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");

    /**
     * 邮箱用户名
     */
    private static final String EMAILUSERNAME = "EMAILUSERNAME";

    /**
     * 邮箱用户授权码
     */
    private static final String EMAILPASSWORD = "EMAILPASSWORD";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        TextArea ta = (TextArea) jobDataMap.get("ta");
        List<Node> dataFile = unit.objToList(jobDataMap.get("dataFile"), Node.class);
        List<Node> template = unit.objToList(jobDataMap.get("template"), Node.class);
        List<Node> summaryDataFile = unit.objToList(jobDataMap.get("summary"), Node.class);
        int commentPageNum = jobDataMap.getInt("pageNum");
        String dataStartTime = jobDataMap.getString("dataStartTime");
        String dataEndTime = jobDataMap.getString("dataEndTime");
        boolean sendMail = jobDataMap.getBoolean("sendMail");
        String to = jobDataMap.getString("to");

        // 执行实际逻辑
        executeButton(ta, dataFile, template, summaryDataFile, commentPageNum,
                true, dataStartTime, dataEndTime, sendMail, to);
    }

    /**
     * 微信文章评论数获取
     * @param ta 多行文本显示区域
     * @param dataFile 数据文件
     * @param template 模板文件
     * @param summaryDataFile 汇总文件
     * @param commentPageNum 请求评论页数
     * @param downloadFlag 是否下载数据文件
     * @param startTime 数据文件开始时间
     * @param endTime 数据文件结束时间
     * @param sendMail 是否发送邮件
     * @param to 目标邮件地址，多个以英文;分隔
     */
    public void executeButton(TextArea ta, List<Node> dataFile, List<Node> template, List<Node> summaryDataFile,
                              int commentPageNum, boolean downloadFlag, String startTime, String endTime, boolean sendMail, String to) {
        // 获取文件工具的路径
        String currentPath = System.getProperty("user.dir");
        String driverPath = currentPath + File.separator + "chromedriver";
        // 判断系统类型
        String osName = System.getProperty("os.name");
        updateTextArea(ta, "系统类型：" + osName);
        if (osName.startsWith("Windows")) {
            driverPath = currentPath + File.separator + "chromedriver.exe";
        }
        updateTextArea(ta, "驱动器路径：" + driverPath);
        // 在jvm运行环境中添加驱动配置
        System.setProperty("webdriver.chrome.driver", driverPath);
        System.setProperty("webdriver.http.factory", "jdk-http-client");

        ChromeOptions chromeOptions = new ChromeOptions();

        chromeOptions.setExperimentalOption("debuggerAddress", "127.0.0.1:9527");
        chromeOptions.addArguments("--remote-allow-origins=*");
        // # driver就是当前浏览器窗口
        WebDriver driver;
        try {
            driver = new ChromeDriver(chromeOptions);
        } catch (Exception e) {
            String message = e.getMessage();
            String[] split = message.split("\n");
            StringBuilder sb = new StringBuilder("\n浏览器驱动版本错误！").append("\n");
            String[] first = split[1].split(" ");
            String[] second = split[2].split(" ");
            sb.append("当前驱动版本为：").append(first[first.length - 1]).append("\n");
            sb.append("当前浏览器版本为：").append(second[second.length - 1]).append("\n");
            sb.append("请下载与浏览器版本相对应的驱动，并将驱动放置到该工具所在目录，下载网址：https://chromedriver.storage.googleapis.com/index.html 或 https://googlechromelabs.github.io/chrome-for-testing/");
            updateTextArea(ta, sb.toString());
            return;
        }
//		// 请求草稿箱地址
//		String draftUrl = draftsPathTf.getText();
//		driver.get(draftUrl);
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
        Set<Cookie> cookies = driver.manage().getCookies();
        // 拼接cookie
        StringBuilder ck = new StringBuilder();
        for (Cookie cookie : cookies){
            ck.append(cookie.getName() + "=" + cookie.getValue()+"; ");
        }
        String realCookie = ck.substring(0, ck.length() - 2);
        // 获取token
        String currentUrl = driver.getCurrentUrl();
        updateTextArea(ta, "当前网页地址：" + currentUrl);
        String[] split = currentUrl.split("&");
        String token = null;
        for (String s : split) {
            String[] split1 = s.split("=");
            if (split1.length == 2 && "token".equals(split1[0])) {
                token = split1[1];
            }
        }
        String realToken = token;

        updateTextArea(ta, "获取的token：" + realToken);

        String url = "https://mp.weixin.qq.com/misc/appmsgcomment?action=get_unread_appmsg_comment&has_comment=0&sort_type=1&sendtype=MASSSEND&lang=zh_CN&f=json&ajax=1&token=";
        HttpClient client = HttpClients.createDefault();
        Map<String, Integer> commentsMap = new HashMap<>();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 模板文件路径
                String templateFilePath = ((TextField) template.get(1)).getText();
                // 获取数据文件中的有效条数
                String dataFilePath = ((TextField) dataFile.get(1)).getText();
                if (downloadFlag) {
                    // 下载数据文件
                    String downloadDataUrl = "https://mp.weixin.qq.com/misc/datacubequery?action=query_download&busi=3&tmpl=1" +
                            "&args={%22begin_date%22:" + startTime + ",%22end_date%22:" + endTime + "}" +
                            "&token=" + realToken +
                            "&lang=zh_CN";

                    System.out.println("请求数据文件地址：" + downloadDataUrl);
                    updateTextArea(ta, "请求数据文件地址：" + downloadDataUrl);
                    // 下载
                    String targetPath = templateFilePath.substring(0, templateFilePath.lastIndexOf(File.separator));
                    String downloadPath= DownloadImageToFileByUrl.download(downloadDataUrl, targetPath, "dataFile.xls", null, realCookie);
                    System.out.println("下载的数据文件路径为：" + downloadPath);
                    updateTextArea(ta, "下载的数据文件路径为：" + downloadPath);
                    dataFilePath = downloadPath;
                }
                Workbook dataWb = null;
                Workbook summaryWb = null;
                FileOutputStream fos = null;
                try {
                    if (dataFilePath.endsWith(".xlsx")) {
                        dataWb = new XSSFWorkbook(new FileInputStream(dataFilePath));
                    } else {
                        dataWb = new HSSFWorkbook(new FileInputStream(dataFilePath));
                    }
                    updateTextArea(ta, "开始读取数据文件！");

                    // 获取第一个sheet页的文章条数
                    Sheet sheet = dataWb.getSheetAt(0);
                    int articleNum = sheet.getLastRowNum();
                    System.out.println("articleNum = " + articleNum);
                    updateTextArea(ta, "数据文件中的文章条数为：" + articleNum);

                    // 每页的数据条数
                    int divisor = 10;
                    int num = commentPageNum;
                    updateTextArea(ta, "开始请求所有文章的评论数！共 " + num + " 页");
                    System.out.println("开始请求所有文章的评论数！共 " + num + " 页");
                    for (int i = 0; i < num; i++) {
                        String uri = url + realToken + "&begin=" + i * divisor + "&count=" + divisor;
                        System.out.println("请求文章的评论数的url: "+ uri);
                        updateTextArea(ta, "请求第" + (i + 1) + "页的文章评论数");
                        HttpGet httpGet = new HttpGet(uri);
                        httpGet.setHeader("cookie", realCookie);

                        HttpResponse response = client.execute(httpGet);
                        String content = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                        JSONObject jsonObject = JSONObject.parseObject(content);
                        // 获取评论数组
                        JSONArray item = jsonObject.getJSONArray("item");
                        System.out.println("请求到的评论数信息为：" + item.toString());
                        if (item == null) {
                            // 弹窗提示cookie和token过期
                            System.out.println("cookie和token过期，请重新输入！");
                            updateTextArea(ta, "cookie和token过期，请重新输入！");
                            return;
                        }

                        // 遍历评论，并将文章id和评论数放到map中
                        for (int j = 0; j < item.size(); j++) {
                            JSONObject commentInfo = item.getJSONObject(j);
                            commentsMap.put(commentInfo.getString("comment_id"), commentInfo.getIntValue("total_count"));
                        }
                        httpGet.releaseConnection();
                    }
                    System.out.println("获取到的评论数信息为：" + commentsMap);
                    updateTextArea(ta, "获取到所有文章的评论数！");

                    // 在sheet页最后插入一列
                    updateTextArea(ta, "在数据文件的最后插入一列！");
                    Row titleRow = sheet.getRow(0);
                    int lastCellNum = titleRow.getLastCellNum();
                    int lastNum = lastCellNum;

                    CellStyle titleCellStyle = null;
                    for (int i = lastCellNum; i >= 0; i--) {
                        Cell latCell = titleRow.getCell(i);
                        if (latCell == null) {
                            continue;
                        }
                        if ("内容url".equals(unit.getCellValue(titleRow.getCell(i)))) {
                            lastNum = i;
                            titleCellStyle = titleRow.getCell(i).getCellStyle();
                            break;
                        }
                    }

                    // 存储数据文件中每行的数据的map，先以送达人数为key，再以文章标题为key
                    Map<Integer, Map<String, CommentInfo>> commentInfoMap = new HashMap<>();
                    int urlCount = 0;
                    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
//						System.out.println("查询下标 " + i + "行");
                        Row row = sheet.getRow(i);
                        if (row == null) {
                            continue;
                        }
//						System.out.println("最后一列：" + lastNum);
                        int newLastNum = lastNum + 1;
                        Cell cell = row.createCell(newLastNum);
                        if (i == 0) {
                            cell.setCellValue("评论数");
                            cell.setCellStyle(titleCellStyle);
                            continue;
                        }
                        // 获取前一个单元格的内容
//						System.out.println("查询下标 " + lastNum + "列");
                        Cell lastCellOnRow = row.getCell(lastNum);
                        if (lastCellOnRow == null) {
                            continue;
                        }
                        String contentUrl = unit.getCellValue(lastCellOnRow);
                        CellStyle preCellStyle = row.getCell(lastNum).getCellStyle();
                        cell.setCellStyle(preCellStyle);
                        System.out.println("请求url为" + contentUrl);
                        updateTextArea(ta, "请求第" + (i + 1) + "行的url");
                        urlCount++;
                        HttpGet httpGet = new HttpGet(contentUrl);
                        HttpResponse response = client.execute(httpGet);
//						System.out.println("请求微信公众号文章");
                        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        // 遍历每一行
                        String line;
                        int commentNum = 0;
                        while ((line = br.readLine()) != null) {
                            if (line.contains("comment_id")) {
                                // 解析到 comment_id
                                String commentId = line.substring(line.indexOf("\"") + 1);
                                commentId = commentId.substring(0, commentId.indexOf("\""));
                                // 获取评论数map中的值
                                Integer integer = commentsMap.get(commentId);
                                if (integer == null) {
                                    updateTextArea(ta, "未查询到对应的微信文章评论信息，请增大”查询评论的页数“");
                                }
                                commentNum = integer;
                                cell.setCellValue(integer);
                                break;
                            }
                        }
                        updateTextArea(ta, "获取到公众号文章ID，并插入对应的评论数");
                        httpGet.releaseConnection();
                        // 将行数据放入到map中
                        String articleTitle = unit.getCellValue(row.getCell(0));
                        int pushPeople = Integer.parseInt(unit.getCellValue(row.getCell(7)));
                        // 根据url获取文章的排序
                        Integer index = -1;
                        String[] urlParams = contentUrl.split("&");
                        for (String p : urlParams) {
                            // 判断参数是否为 idx
                            if (!p.startsWith("idx")) {
                                continue;
                            }
                            // 获取idx=之后的数据
                            index = Integer.valueOf(p.substring(p.indexOf("=") + 1));
                            break;
                        }
                        CommentInfo info = CommentInfo.builder()
                                .title(articleTitle.trim())
                                .pushDate(unit.getCellValue(row.getCell(1)))
                                .allReadPeople(Integer.valueOf(unit.getCellValue(row.getCell(2))))
                                .allSharePeople(Integer.valueOf(unit.getCellValue(row.getCell(4))))
                                .pushPeople(pushPeople)
                                .completeReadRate(unit.getCellValue(row.getCell(14)))
                                .commentNum(commentNum)
                                .url(contentUrl)
                                .index(index)
                                .build();
                        Map<String, CommentInfo> temp = commentInfoMap.get(pushPeople);
                        if (temp == null) {
                            Map<String, CommentInfo> init = new HashMap<>();
                            init.put(articleTitle, info);
                            commentInfoMap.put(pushPeople, init);
                        } else {
                            temp.put(articleTitle, info);
                        }
                    }
                    // 数据文件中每个分组的顺序
                    Map<Integer, List<String>> groupArtFromData = new HashMap<>();
                    commentInfoMap.forEach((k, v) -> {
                        List<String> titles = v.values().stream().sorted(new Comparator<CommentInfo>() {
                            @Override
                            public int compare(CommentInfo o1, CommentInfo o2) {
                                // 升序
                                return o1.getIndex() - o2.getIndex();
                            }
                        }).map(CommentInfo::getTitle).collect(Collectors.toList());
                        groupArtFromData.put(k, titles);
                    });
                    updateTextArea(ta, "实际共获取到公众号文章 " + urlCount + " 条！");
                    updateTextArea(ta, "数据文件的最后插入一列插入完成！");

                    dataWb.close();
                    // 获取模板文件
                    updateTextArea(ta, "开始读取模板文件！");
                    if (templateFilePath.endsWith(".xlsx")) {
                        dataWb = new XSSFWorkbook(new FileInputStream(templateFilePath));
                    } else {
                        dataWb = new HSSFWorkbook(new FileInputStream(templateFilePath));
                    }
                    // 遍历行
                    Sheet templateSheet = dataWb.getSheetAt(0);
                    List<TemplateInfo> baseList = new ArrayList<>();
                    String group = "";
                    String pushDate = "";
                    // 从文件名获取推送日期
                    Pattern compile = Pattern.compile("\\d+月\\d+日");
                    Matcher matcher = compile.matcher(templateFilePath.substring(templateFilePath.lastIndexOf(File.separator)));
                    if (matcher.find()) {
                        String group0 = matcher.group(0);
                        pushDate = group0;
                    }

                    int pushPeople = -1;
                    boolean flag = false;
                    // 根据标题行（第二行）获取到单元格的坐标
                    Row row1 = templateSheet.getRow(1);
                    int titleIndex = 0;
                    int titleTypeIndex = 1;
                    int positionIndex = 2;
                    int groupIndex = 3;
                    int peopleNumIndex = 5;

                    for (int i = 0; i <= row1.getLastCellNum(); i++) {
                        Cell cell = row1.getCell(i);
                        if (cell == null || cell.getCellType().equals(CellType.BLANK)) {
                            continue;
                        }
                        String cellV = unit.getCellValue(cell);
                        if ("文章标题".equals(cellV)) {
                            titleIndex = i;
                        } else if ("标题类型".equals(cellV)) {
                            titleTypeIndex = i;
                        } else if ("位置".equals(cellV)) {
                            positionIndex = i;
                        } else if ("组别".equals(cellV)) {
                            groupIndex = i;
                        } else if ("新系统分组人数".equals(cellV)) {
                            peopleNumIndex = i;
                        }
                    }

                    // 存储模板文件中每个分组的顺序
                    Map<Integer, List<String>> groupArtFromTemp = new HashMap<>();
                    for (int i = 2; i <= templateSheet.getLastRowNum(); i++) {
                        Row row = templateSheet.getRow(i);
                        if (row == null || row.getCell(titleIndex) == null
                                || StringUtils.isBlank(unit.getCellValue(row.getCell(titleIndex)))) {
                            continue;
                        } else {
                            System.out.println(" i = " + i);
                            TemplateInfo info = TemplateInfo.builder()
                                    .title(unit.getCellValue(row.getCell(titleIndex)).trim())
                                    .titleType(unit.getCellValue(row.getCell(titleTypeIndex)))
                                    .position(BigDecimal.valueOf(row.getCell(positionIndex).getNumericCellValue()).intValue())
                                    .build();
                            String cell3Value = unit.getCellValue(row.getCell(groupIndex));
                            if (StringUtils.isNotBlank(cell3Value)) {
                                group = cell3Value;
                                info.setFirst(true);
                                flag = true;
                            }
                            if (flag) {
                                int cell5Value = BigDecimal.valueOf(row.getCell(peopleNumIndex).getNumericCellValue()).intValue();
                                pushPeople = cell5Value;
                                flag = false;
                            }
                            if (groupArtFromTemp.get(pushPeople) == null) {
                                List<String> arts = new ArrayList<>();
                                arts.add(info.getTitle());
                                groupArtFromTemp.put(pushPeople, arts);
                            } else {
                                groupArtFromTemp.get(pushPeople).add(info.getTitle());
                            }
                            info.setGroup(group);
                            info.setPushDate(pushDate);
                            info.setPushPeople(pushPeople);
                            baseList.add(info);
                        }
                    }
                    updateTextArea(ta, "获取到模板文件中所有数据！");

                    dataWb.close();
                    // 获取结果模板文件
                    updateTextArea(ta, "开始写出到结果数据文件！");
                    dataWb = new XSSFWorkbook(ClassLoader.getSystemResourceAsStream("template/dataResultTemplate.xlsx"));
                    Sheet resultSheet = dataWb.getSheetAt(0);
                    // 自动计算公式
                    resultSheet.setForceFormulaRecalculation(true);
                    // 单元格的默认样式
                    CellStyle defaultCellStyle = dataWb.createCellStyle();
                    // 字体
                    Font defaultFont = dataWb.createFont();
                    defaultFont.setFontName("冬青黑体简体中文 W3");
                    defaultFont.setFontHeightInPoints((short) 11);
                    defaultCellStyle.setFont(defaultFont);
                    // 内容水平、垂直居中
                    defaultCellStyle.setAlignment(HorizontalAlignment.CENTER);
                    defaultCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    // 设置边框实线
                    defaultCellStyle.setBorderLeft(BorderStyle.THIN);
                    defaultCellStyle.setBorderBottom(BorderStyle.THIN);
                    defaultCellStyle.setBorderRight(BorderStyle.THIN);

                    Set<Integer> pushPeopleKeys = commentInfoMap.keySet();
                    // 存储推送人群需要合并单元格的起始行和结束行
                    List<Integer> beginAndEnd = new ArrayList<>();
                    String groupStandard = "";
                    updateTextArea(ta, "将文章数据写入到结果数据文件！");
                    for (int i = 0; i < baseList.size(); i++) {
                        int rowNum = i + 1;
                        CellStyle cloneCellStyle = dataWb.createCellStyle();
                        cloneCellStyle.cloneStyleFrom(defaultCellStyle);
                        if (rowNum == baseList.size()) {
                            cloneCellStyle.setBorderBottom(BorderStyle.MEDIUM);
                        }
                        if (rowNum == 1) {
                            cloneCellStyle.setBorderTop(BorderStyle.MEDIUM);
                        }
                        Row resultRow = resultSheet.createRow(rowNum);
                        // 设置行高 22
                        resultRow.setHeightInPoints(22);
                        TemplateInfo templateInfo = baseList.get(i);
                        // 先判断推送人数
                        int tpush = templateInfo.getPushPeople();
                        // 获取模板文件中的文章顺序
                        List<String> templateArtsByGroup = groupArtFromTemp.get(tpush);
                        int key = -1;
                        int min = 1000000000;
                        String tTitle = templateInfo.getTitle();
                        updateTextArea(ta, "开始匹配模板文件中：" + tTitle + "的推送人数：" + tpush);
                        updateTextArea(ta, "模板文件中的文章顺序：" + String.join(",", templateArtsByGroup));
                        for (Integer k : pushPeopleKeys) {
                            List<String> dataArtsByGroup = groupArtFromData.get(k);
                            if (Math.abs(tpush - k) < min && templateArtsByGroup.equals(dataArtsByGroup)) {
                                key = k;
                                min = Math.abs(tpush - k);
                            }
                        }
                        updateTextArea(ta, "匹配到数据文件中的实际推送人数：" + key);

                        if (key < 1000000 && templateInfo.isFirst()) {
                            cloneCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            cloneCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                        }

                        Cell cell0 = resultRow.createCell(0);
                        CellStyle leftCellStyle = dataWb.createCellStyle();
                        leftCellStyle.cloneStyleFrom(cloneCellStyle);
                        leftCellStyle.setBorderLeft(BorderStyle.MEDIUM);
                        cell0.setCellValue(tTitle);
                        cell0.setCellStyle(leftCellStyle);
                        Cell cell1 = resultRow.createCell(1);
                        cell1.setCellValue(templateInfo.getTitleType());
                        cell1.setCellStyle(cloneCellStyle);
                        Cell cell2 = resultRow.createCell(2);
                        cell2.setCellValue(templateInfo.getPushDate());
                        cell2.setCellStyle(cloneCellStyle);
                        Cell cell3 = resultRow.createCell(3);
                        cell3.setCellValue(templateInfo.getGroup());
                        cell3.setCellStyle(cloneCellStyle);
                        Cell cell11 = resultRow.createCell(11);
                        if (templateInfo.getPosition() != 1) {
                            cell11.setCellValue("");
                        } else {
                            cell11.setCellValue(String.valueOf(templateInfo.getPosition()));
                        }
                        CellStyle rightCellStyle = dataWb.createCellStyle();
                        rightCellStyle.cloneStyleFrom(cloneCellStyle);
                        rightCellStyle.setBorderRight(BorderStyle.MEDIUM);
                        cell11.setCellStyle(rightCellStyle);

                        Cell cell4 = resultRow.createCell(4);
                        cell4.setCellValue(key);
                        cell4.setCellStyle(cloneCellStyle);

                        int excelRowNum = rowNum + 1;
                        // 获取map中的评论信息
                        Map<String, CommentInfo> stringCommentInfoMap = commentInfoMap.get(key);
                        if (stringCommentInfoMap == null || stringCommentInfoMap.isEmpty()) {
                            updateTextArea(ta, "未匹配到送达人数：" + key + " 的评论信息！");
                        }
                        CommentInfo commentInfo = stringCommentInfoMap.get(tTitle);
                        if (commentInfo == null) {
                            updateTextArea(ta, "未匹配到送达人数：" + key + "，文章标题：" + tTitle + " 的评论信息！");
                        } else {
                            Cell cell5 = resultRow.createCell(5);
                            cell5.setCellValue(commentInfo.getAllReadPeople());
                            cell5.setCellStyle(cloneCellStyle);
                            Cell cell6 = resultRow.createCell(6);
                            cell6.setCellValue(commentInfo.getAllSharePeople());
                            cell6.setCellStyle(cloneCellStyle);
                            // 设置百分比格式
                            CellStyle rateCellStyle = dataWb.createCellStyle();
                            rateCellStyle.cloneStyleFrom(cloneCellStyle);
                            DataFormat xdf = dataWb.createDataFormat();
                            rateCellStyle.setDataFormat(xdf.getFormat("0.00%"));

                            Cell cell7 = resultRow.createCell(7);
                            String cell7Formula = "F" + excelRowNum + "/E" + excelRowNum;
                            cell7.setCellFormula(cell7Formula);
                            cell7.setCellStyle(rateCellStyle);
                            Cell cell8 = resultRow.createCell(8);
                            String cell8Formula = "G" + excelRowNum + "/F" + excelRowNum;
                            cell8.setCellFormula(cell8Formula);
                            cell8.setCellStyle(rateCellStyle);
                            Cell cell9 = resultRow.createCell(9);
                            cell9.setCellValue(Double.valueOf(commentInfo.getCompleteReadRate()));
                            cell9.setCellStyle(rateCellStyle);
                            Cell cell10 = resultRow.createCell(10);
                            cell10.setCellValue(commentInfo.getCommentNum());
                            cell10.setCellStyle(cloneCellStyle);

                            String groupValue = templateInfo.getGroup();
                            String cell12Value = "";
                            if (!groupStandard.equals(groupValue)) {
                                beginAndEnd.add(rowNum);
                                groupStandard = groupValue;
                                cell12Value = commentInfo.getUrl();
                            }
                            Cell cell12 = resultRow.createCell(12);
                            cell12.setCellValue(cell12Value);
                        }

                    }
                    updateTextArea(ta, "写入结果数据文件完成！");
                    updateTextArea(ta, "开始合并数据文件中的单元格！");
                    // 合并单元格
                    // 推送时间合并
                    CellRangeAddress dateCra = new CellRangeAddress(1, baseList.size(), 2, 2);
                    System.out.println("合并推送时间列的行为 " + 2 + " : " + (baseList.size() + 1));
                    resultSheet.addMergedRegion(dateCra);
                    // 推送人群合并
                    for (int i = 0; i < beginAndEnd.size(); i++) {
                        int begin = beginAndEnd.get(i);
                        int end;
                        if (i == beginAndEnd.size() - 1) {
                            // 最后一个特殊处理
                            end = baseList.size();
                        } else {
                            end = beginAndEnd.get(i + 1) - 1;
                        }
                        CellRangeAddress groupCra = new CellRangeAddress(begin, end, 3, 3);
                        System.out.println("合并推送人群列的行为 " + (begin + 1) + " : " + (end + 1));
                        resultSheet.addMergedRegion(groupCra);
                    }
                    updateTextArea(ta, "合并数据文件中的单元格完成！");

                    // 将信息写入到文件
                    String appendTimeTag = sdf.format(new Date());
                    String extend = dataFilePath.substring(dataFilePath.lastIndexOf("."));
                    String newDataFilePath = dataFilePath.substring(0, dataFilePath.lastIndexOf(".")) + "_" + appendTimeTag + extend;
                    File file = new File(newDataFilePath);
                    fos = new FileOutputStream(file);
                    dataWb.write(fos);
                    fos.close();
                    fos = null;
                    updateTextArea(ta, "结果数据文件导出完成！结果数据文件路径：" + newDataFilePath);

                    // 获取汇总文件
                    String summaryFilePath = ((TextField) summaryDataFile.get(1)).getText();
                    if (StringUtils.isNotBlank(summaryFilePath)) {
                        updateTextArea(ta, "开始读取汇总文件！");
                        if (summaryFilePath.endsWith(".xlsx")) {
                            summaryWb = new XSSFWorkbook(summaryFilePath);
                        } else {
                            summaryWb = new HSSFWorkbook(new FileInputStream(summaryFilePath));
                        }
                        // 获取文件的起始行(行为空或单元格为空或单元格数据为空字符串)
                        String sheetName = pushDate.substring(0, pushDate.indexOf("月") + 1).trim();
                        if (summaryWb.getSheetIndex(sheetName) == -1) {
                            updateTextArea(ta, "未查询到对应" + sheetName + "名称的sheet页！");
                            return;
                        }
                        Sheet summarySheet = summaryWb.getSheet(sheetName);
                        // 设置公式自动计算
                        summarySheet.setForceFormulaRecalculation(true);
                        int startLine = 0;
                        for (int i = 0; i < summarySheet.getLastRowNum(); i++) {
                            Row summaryRow = summarySheet.getRow(i);
                            if (summaryRow != null && summaryRow.getCell(0) != null
                                    && StringUtils.isNotBlank(unit.getCellValue(summaryRow.getCell(0)))) {
                                continue;
                            }
                            startLine = i;
                            break;
                        }
                        // 在汇总文件中插入行(将结束的空行下移)
                        summarySheet.shiftRows(startLine, summarySheet.getLastRowNum(), resultSheet.getLastRowNum() + 1, true, false);
                        updateTextArea(ta, "在汇总文件中插入空行（" + (startLine + 1) +"行~" + (startLine + resultSheet.getLastRowNum() + 2) + "行）！");
                        updateTextArea(ta, "开始将结果数据插入到汇总文件！");
                        for (int i = 0; i <= resultSheet.getLastRowNum(); i++) {
                            // 开始插入单元格
                            // 获取数据文件的行
                            Row newRow = summarySheet.createRow(startLine + i);
                            Row dataRow = resultSheet.getRow(i);
                            newRow.setHeightInPoints(dataRow.getHeightInPoints());
                            for (int j = 0; j <= 12; j++) {
                                Cell newCell = newRow.createCell(j);
                                Cell dataCell = dataRow.getCell(j);
                                if (dataCell == null) {
                                    continue;
                                }
                                CellStyle newCellStyle = summaryWb.createCellStyle();
                                newCellStyle.cloneStyleFrom(dataCell.getCellStyle());
                                newCell.setCellStyle(newCellStyle);
                                if (i != 0) {
                                    int excelRowNum = startLine + i + 1;
                                    if (j == 7) {
                                        String cell7Formula = "F" + excelRowNum + "/E" + excelRowNum;
                                        newCell.setCellFormula(cell7Formula);
                                    } else if (j == 8) {
                                        String cell8Formula = "G" + excelRowNum + "/F" + excelRowNum;
                                        newCell.setCellFormula(cell8Formula);
                                    } else if (j == 0 || j == 1 || j == 2 || j == 3 || j == 12 || j == 11) {
                                        newCell.setCellValue(unit.getCellValue(dataCell));
                                    } else {
                                        newCell.setCellValue(dataCell.getNumericCellValue());
                                    }
                                } else {
                                    newCell.setCellValue(unit.getCellValue(dataCell));
                                }
                            }
                        }
                        updateTextArea(ta, "结果数据插入到汇总文件完成！");
                        // 拷贝合并区域
                        List<CellRangeAddress> mergedRegions = resultSheet.getMergedRegions();
                        // 存储合并区域的开始行和结束行，不包括推送时间合并语句
                        List<int[]> rangeBeginAndEnd = new ArrayList<>();
                        for (CellRangeAddress cra : mergedRegions) {
                            int oldFirstRow = cra.getFirstRow();
                            int oldLastRow = cra.getLastRow();
                            int newFirstRow = oldFirstRow + startLine;
                            int newLastRow = oldLastRow + startLine;
                            cra.setFirstRow(newFirstRow);
                            cra.setLastRow(newLastRow);
                            summarySheet.addMergedRegion(cra);
                            if (!pushDate.equals(unit.getCellValue(resultSheet.getRow(oldFirstRow).getCell(cra.getFirstColumn())))) {
                                int[] be = new int[]{newFirstRow + 1, newLastRow + 1};
                                rangeBeginAndEnd.add(be);
                            }
                        }
                        updateTextArea(ta, "合并汇总文件中单元格完成！");

                        // 汇总文件下面的汇总区域
                        updateTextArea(ta, "开始处理汇总文件下面的汇总区域！");
                        int start = startLine + resultSheet.getLastRowNum() + 1;
                        boolean countFlag = false;
                        for (int i = start; i < summarySheet.getLastRowNum(); i++) {
                            Row row = summarySheet.getRow(i);
                            // 获取第四个单元格
                            if (row == null) {
                                continue;
                            }
                            Cell cell3 = row.getCell(3);
                            if (!countFlag && (cell3 == null || !cell3.getCellType().equals(CellType.STRING))) {
                                continue;
                            }
                            if (cell3.getCellType().equals(CellType.STRING) && "推送时间".equals(unit.getCellValue(cell3).trim())) {
                                countFlag = true;
                                continue;
                            }

                            boolean blankFlag = cell3.getCellType().equals(CellType.BLANK);
                            if (blankFlag && countFlag) {
                                cell3.setCellValue(pushDate);
                                Cell cell4 = row.getCell(4);
                                Cell cell5 = row.getCell(5);
                                Cell cell6 = row.getCell(6);
                                Cell cell7 = row.getCell(7);
                                Cell cell8 = row.getCell(8);
                                Cell cell9 = row.getCell(9);

                                StringBuilder cell4Formula = new StringBuilder("SUM(");
                                StringBuilder cell5Formula = new StringBuilder("SUM(");
                                StringBuilder cell6Formula = new StringBuilder("SUM(");
                                StringBuilder cell7Formula = new StringBuilder("SUM(");
                                StringBuilder cell8Formula = new StringBuilder("SUM(");
                                StringBuilder cell9Formula = new StringBuilder("SUM(");

                                int divisor456 = 0;
                                int divisor789 = 0;
                                for (int k = 0; k < rangeBeginAndEnd.size(); k++) {
                                    int[] be = rangeBeginAndEnd.get(k);
                                    divisor456++;
                                    // 下标第4列的求和处理
                                    cell4Formula.append("H").append(be[0]).append(",");
                                    // 下标第5列
                                    cell5Formula.append("I").append(be[0]).append(",");
                                    // 下标第6列
                                    cell6Formula.append("J").append(be[0]).append(",");

                                    for (int m = be[0] + 1; m <= be[1]; m++) {
                                        divisor789++;
                                    }
                                    // 下标第7列
                                    cell7Formula.append("H").append(be[0] + 1).append(":").append("H").append(be[1]).append(",");
                                    // 下标第8列
                                    cell8Formula.append("I").append(be[0] + 1).append(":").append("I").append(be[1]).append(",");
                                    // 下标第9列
                                    cell9Formula.append("J").append(be[0] + 1).append(":").append("J").append(be[1]).append(",");
                                }
                                cell4.setCellFormula(cell4Formula.substring(0, cell4Formula.length() - 1) + ")/" + divisor456);
                                cell5.setCellFormula(cell5Formula.substring(0, cell5Formula.length() - 1) + ")/" + divisor456);
                                cell6.setCellFormula(cell6Formula.substring(0, cell6Formula.length() - 1) + ")/" + divisor456);
                                cell7.setCellFormula(cell7Formula.substring(0, cell7Formula.length() - 1) + ")/" + divisor789);
                                cell8.setCellFormula(cell8Formula.substring(0, cell8Formula.length() - 1) + ")/" + divisor789);
                                cell9.setCellFormula(cell9Formula.substring(0, cell9Formula.length() - 1) + ")/" + divisor789);
                                break;
                            }
                        }
                        updateTextArea(ta, "汇总文件下面的汇总区域处理完成！");

                        String newSummaryFilePath = summaryFilePath.substring(0, summaryFilePath.lastIndexOf("."))
                                + "_" + appendTimeTag + summaryFilePath.substring(summaryFilePath.lastIndexOf("."));
                        File summaryFile = new File(newSummaryFilePath);
                        fos = new FileOutputStream(summaryFile);
                        summaryWb.write(fos);
                        fos.close();
                        fos = null;
                        updateTextArea(ta, "结果汇总文件导出完成！结果汇总文件路径：" + newSummaryFilePath);
                        if (sendMail && StringUtils.isNotBlank(to)) {
                            String[] tos = to.split(";");
                            // 发送邮件
                            String username = Config.get(EMAILUSERNAME);
                            String password = Config.get(EMAILPASSWORD);
                            password = "en" + password;
                            // base64解密
                            String realPassword = new String(Base64.getDecoder().decode(password.getBytes()));
                            MavenrQQEmail email = new MavenrQQEmail(username, realPassword);
                            List<File> files = new ArrayList<>();
                            files.add(summaryFile);
                            email.sendAttachMail(username, Arrays.asList(tos), "汇总数据-" + startTime + "-" + endTime, ta.getText(), files);

                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    if (dataWb != null) {
                        try {
                            dataWb.close();
                        } catch (IOException de) {
                            de.printStackTrace();
                        }
                    }
                    if (summaryWb != null) {
                        try {
                            summaryWb.close();
                        } catch (IOException se) {
                            se.printStackTrace();
                        }
                    }
//                    if (e.getMessage().contains("timed out")) {
//                        updateTextArea(ta, "请求连接超时，请重新开始处理！");
//                    }
                    e.printStackTrace();
                } finally {
                    if (dataWb != null) {
                        try {
                            dataWb.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }


    /**
     * TextArea区域填充文本后自动滑动
     * @param ta
     * @param message
     */
    public void updateTextArea(TextArea ta, String message) {
        if (Platform.isFxApplicationThread()) {
            ta.appendText("\n" + message);
        } else {
            Platform.runLater(() -> ta.appendText("\n" + message));
        }
    }
}
