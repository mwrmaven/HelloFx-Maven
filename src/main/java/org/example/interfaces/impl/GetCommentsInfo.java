package org.example.interfaces.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.button.BatchButton;
import org.example.entity.NickNameAndComment;
import org.example.init.Config;
import org.example.interfaces.Function;
import org.example.util.Unit;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author mavenr
 * @Classname GetCommentsInfo
 * @Description 获取微信文章评论信息
 * @Date 2023/6/5 12:12
 */
public class GetCommentsInfo implements Function {

    /**
     * 工具类
     */
    private Unit unit = new Unit();

    /**
     * chrome浏览器启动器的文件路径
     */
    private static final String CHROMESTARTPATH = "CHROMESTARTPATH";

    @Override
    public String tabName() {
        return "微信文章评论信息";
    }

    @Override
    public String tabStyle() {
        String style = "-fx-font-weight: bold; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-focus-color: transparent; -fx-text-base-color: white; " +
                "-fx-background-color: #FFD700;  -fx-pref-height: 30; ";
        return style;
    }

    @Override
    public AnchorPane tabPane(Stage stage, double width, double h) {
        AnchorPane root = new AnchorPane();

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        root.getChildren().add(vBox);

        // 一行的前置行，请输入chrome浏览器的启动器类的文件路径
        HBox line1Pre = new HBox();
        line1Pre.setAlignment(Pos.CENTER_LEFT);
        line1Pre.setSpacing(10);
        List<Node> chrome = unit.newInputText(width - 200, "请输入chrome浏览器的启动器类的文件路径：", 290);
        for (Node n : chrome) {
            line1Pre.getChildren().add(n);
        }

        TextField chromePathTf = (TextField) chrome.get(1);
        // 设置样式为下划线
        String param = Config.get(CHROMESTARTPATH);
        // 加载配置文件中的参数
        if (StringUtils.isNotEmpty(param)) {
            chromePathTf.setText(param);
        }
        String text = chromePathTf.getText();
        // 失去焦点触发保存事件
        chromePathTf.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // 判断内容改变，则保存内容
                if (!text.equals(chromePathTf.getText())) {
                    // 设置配置文件
                    Config.set(CHROMESTARTPATH, chromePathTf.getText());
                }
            }
        });

        // 在chrome浏览器winding路径后添加按钮
        BatchButton startBatchButton = new BatchButton();
        Button startButton = startBatchButton.createInstance("启动测试浏览器", 15, 150, 10);
        line1Pre.getChildren().add(startButton);

        // 提示信息
        HBox tips = new HBox();
        Label label = new Label("请点击按钮启动测试浏览器，然后在测试浏览器中输入草稿箱地址，如果需要登陆则先扫码登陆");
        label.setStyle("-fx-text-fill: red; -fx-font-size: 20");
        tips.getChildren().add(label);

        HBox line1 = new HBox();
        line1.setAlignment(Pos.CENTER_LEFT);
        line1.setSpacing(10);
        // 输入要统计评论的文章标题
        List<Node> articleTitle = unit.newInputText(width - 200, "请输入要统计的文章标题（多个以英文;分隔）:", 290);
        for (Node n : articleTitle) {
            line1.getChildren().add(n);
        }

        HBox line2 = new HBox();
        line2.setAlignment(Pos.CENTER_LEFT);
        line2.setSpacing(10);
        // 选择数据文件
        List<Node> dataFile = unit.chooseFile(stage, width, "数据文件");
        for (Node n : dataFile) {
            line2.getChildren().add(n);
        }

        // 执行按钮
        BatchButton executeBatchButton = new BatchButton();
        Button executeButton = executeBatchButton.createInstance("批量获取", 15, 150, 10);

        // 末尾行的文本框
        HBox lineEnd = new HBox();
        lineEnd.setAlignment(Pos.CENTER_LEFT);
        lineEnd.setSpacing(10);
        TextArea ta = new TextArea();
        ta.setPrefWidth(width / 2);
        ta.setPrefHeight(stage.getHeight() - 500);
        ta.setEditable(false);
        lineEnd.getChildren().add(ta);

        vBox.getChildren().addAll(line1Pre, tips, line1, line2, executeButton, lineEnd);

        // 启动测试浏览器按钮事件
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // 判断chrome启动器类的路径
                String chromePath = ((TextField) chrome.get(1)).getText();
                // 启动浏览器
                unit.startChrome(chromePath, ta);
            }
        });

        // 执行按钮的逻辑
        executeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 获取填入的文章标题
                        String articleNames = ((TextField) articleTitle.get(1)).getText();
                        if (StringUtils.isBlank(articleNames)) {
                            ta.setText("请输入要查询的文章标题");
                            return;
                        }

                        String dataFilePath = ((TextField) dataFile.get(1)).getText();
                        if (StringUtils.isBlank(dataFilePath)) {
                            ta.setText("请选择数据文件");
                            return;
                        }

                        ta.setText("");
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
                            sb.append("请下载与浏览器版本相对应的驱动，并将驱动放置到该工具所在目录，下载网址：https://chromedriver.storage.googleapis.com/index.html");
                            updateTextArea(ta, sb.toString());
                            return;
                        }

                        Set<Cookie> cookies = driver.manage().getCookies();
                        // 拼接cookie
                        StringBuilder ck = new StringBuilder();
                        for (Cookie cookie : cookies){
                            ck.append(cookie.getName() + "=" + cookie.getValue()+"; ");
                        }
                        String realCookie = ck.substring(0, ck.length() - 2);
                        // 获取token
                        String currentUrl = driver.getCurrentUrl();
                        String[] split = currentUrl.split("&");
                        String token = null;
                        for (String s : split) {
                            String[] split1 = s.split("=");
                            if (split1.length == 2 && "token".equals(split1[0])) {
                                token = split1[1];
                            }
                        }
                        String realToken = token;

                        updateTextArea(ta, "获取到cookie = " + realCookie);
                        updateTextArea(ta, "获取到token = " + realToken);

                        // 文章标题转为list集合
                        String[] split1 = articleNames.split(";");
                        List<String> articleNameList = new ArrayList<>();
                        for (String sp : split1) {
                            articleNameList.add(sp.trim());
                        }

                        // 获取文章id
                        HttpClient client = HttpClients.createDefault();
                        String commentIdListBaseUrl = "https://mp.weixin.qq.com/misc/appmsgcomment?action=list_latest_comment&count=10&sort_type=1&sendtype=MASSSEND&lang=zh_CN&f=json&ajax=1&token=" + realToken;
                        List<String> commentIdList = new ArrayList<>();
                        HashMap<String, Long> commentIdMid = new HashMap<>();
                        for (int i = 0; i < 15; i++) {
                            int num = i * 10;
                            String commentIdUrl = commentIdListBaseUrl + "&begin=" + num;
                            HttpGet commentIdGet = new HttpGet(commentIdUrl);
                            commentIdGet.setHeader("Cookie", realCookie);
                            // 请求文章信息
                            try {
                                HttpResponse execute = client.execute(commentIdGet);
                                String commentIdInfoList = IOUtils.toString(execute.getEntity().getContent(), "UTF-8");
                                System.out.println("请求到的文章标题信息为：" + commentIdInfoList);
                                JSONObject commentIdJson = JSONObject.parseObject(commentIdInfoList);
                                JSONObject appMsgList = commentIdJson.getJSONObject("app_msg_list");
                                JSONArray appMsg = appMsgList.getJSONArray("app_msg");
                                for (int j = 0; j < appMsg.size(); j++) {
                                    JSONObject jsonObject = appMsg.getJSONObject(j);
                                    JSONObject item = jsonObject.getJSONObject("item");
                                    // 获取文章的标题，如果匹配，则添加到id集合中
                                    String title = item.getString("title");
                                    if (articleNameList.contains(title)) {
                                        String commentId = item.getString("comment_id");
                                        commentIdList.add(commentId);
                                        commentIdMid.put(commentId, jsonObject.getLongValue("id"));
                                    }
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } finally {
                                commentIdGet.releaseConnection();
                            }
                        }

                        // 文章id和名称
                        HashMap<String, String> commendIdAndName = new HashMap<>();
                        // 文章id和评论
                        HashMap<String, List<NickNameAndComment>> commendIdAndStr = new HashMap<>();
                        for (int i = 0; i < commentIdList.size(); i++) {
                            String id = commentIdList.get(i);
                            int totalCount = getAndParse(client, id, "&begin=0", commendIdAndName, commendIdAndStr, realToken, realCookie, ta);
                            int pageNum = 0;
                            if (totalCount > 0) {
                                if (totalCount > 20) {
                                    if (totalCount % 20 == 0) {
                                        pageNum = totalCount / 20;
                                    } else {
                                        pageNum = totalCount / 20 + 1;
                                    }
                                }
                            }

                            if (pageNum > 0) {
                                for (int j = 1; j < pageNum; j++) {
                                    getAndParse(client, id, "&begin=" + (j * 20), commendIdAndName, commendIdAndStr, realToken, realCookie, ta);
                                }
                            }
                            System.out.println("**********************************************************************************************");

                        }

                        System.out.println(commendIdAndName);
                        System.out.println("-------------------------------------------------------");
                        System.out.println(commendIdAndStr);

                        updateTextArea(ta, "开始写入结果文件===============");
                        outToFile(commendIdAndName, commendIdAndStr, commentIdMid, dataFilePath, ta);
                    }
                }).start();
            }
        });

        return root;
    }

    public void outToFile(HashMap<String, String> commendIdAndName,
                          HashMap<String, List<NickNameAndComment>> commendIdAndStr,
                          HashMap<String, Long> commentIdAndMid, String dataFilePath, TextArea ta) {
        // 写入到excel的路径
        String outPath = System.getProperty("user.dir");
        long currentTime = System.currentTimeMillis();
        String fileName = "comments_" + currentTime + ".xlsx";
        String outFilePath = outPath + File.separator + fileName;
        File outFile = new File(outFilePath);
        FileOutputStream fileOutputStream = null;
        Workbook wb = new XSSFWorkbook();
        FileInputStream dataFis = null;
        Workbook dataWb = null;
        try {
            // 获取数据文件中的url
            File dataFile = new File(dataFilePath);
            dataFis = new FileInputStream(dataFile);
            if (dataFilePath.endsWith(".xlsx")) {
                dataWb = new XSSFWorkbook(new FileInputStream(dataFilePath));
            } else {
                dataWb = new HSSFWorkbook(new FileInputStream(dataFilePath));
            }
            // 获取sheet页面中的url列
            Sheet sheetAt = dataWb.getSheetAt(0);
            // 标题列和url列的下标（根据数据文件确认）
            int titleIndex = 0;
            int urlIndex = 15;
            HashMap<String, String> urlAndName = new HashMap<>();
            for (int i = 1; i <= sheetAt.getLastRowNum(); i++) {
                Row row = sheetAt.getRow(i);
                if (row == null) {
                    continue;
                }
                Cell titleCell = row.getCell(titleIndex);
                if (titleCell == null) {
                    continue;
                }
                Cell urlCell = row.getCell(urlIndex);
                urlAndName.put(urlCell.getStringCellValue(), titleCell.getStringCellValue());
            }

            Sheet sheet = wb.createSheet();
            Row titleRow = sheet.createRow(0);
//            Cell cell1 = titleRow.createCell(0);
//            cell1.setCellValue("文章ID");
            Cell cell2 = titleRow.createCell(0);
            cell2.setCellValue("文章标题");
            Cell cell3 = titleRow.createCell(1);
            cell3.setCellValue("链接");
            Cell cell4 = titleRow.createCell(2);
            cell4.setCellValue("评论");
            Cell cell5 = titleRow.createCell(3);
            cell5.setCellValue("用户名称");

            int i = 0;
            for (String key : commendIdAndStr.keySet()) {
                List<NickNameAndComment> nickNameAndComments = commendIdAndStr.get(key);
                String articleName = commendIdAndName.get(key);

                // 获取mid
                Long mid = commentIdAndMid.get(key);
                // 遍历 urlAndName 获取url
                String realUrl = "";
                for (String url : urlAndName.keySet()) {
                    if (url.contains(String.valueOf(mid))) {
                        // 判断标题是否一致
                        if(urlAndName.get(url).equals(articleName)) {
                            realUrl = url;
                        }
                    }
                }
                int start = i + 1;
                // 留言列表是最晚发布的在列表最前面，所以这里倒序输出，让最早发布的放在文件的最开始
                for (int l = nickNameAndComments.size() - 1; l >= 0; l--) {
                    NickNameAndComment n = nickNameAndComments.get(l);
                    i++;
                    Row row = sheet.createRow(i);
//                    // id
//                    Cell idCell = row.createCell(1);
//                    idCell.setCellValue(key);
                    // 标题
                    Cell titleCell = row.createCell(0);
                    titleCell.setCellValue(articleName);
                    // 文章链接
                    Cell urlCell = row.createCell(1);
                    urlCell.setCellValue(realUrl);
                    // 评论
                    Cell commentCell = row.createCell(2);
                    commentCell.setCellValue(n.getComment());
                    // 用户名称
                    Cell nickNameCell = row.createCell(3);
                    nickNameCell.setCellValue(n.getNickName());
                }

                int end = i;
                if (start != end) {
                    // 合并单元格
                    CellRangeAddress groupCra = new CellRangeAddress(start, end, 0, 0);
                    System.out.println("合并第一列行数为： " + (start + 1) + " 到 " + (end + 1));
                    updateTextArea(ta, "合并第一列行数为： " + (start + 1) + " 到 " + (end + 1));
                    sheet.addMergedRegion(groupCra);

                    CellRangeAddress groupCra2 = new CellRangeAddress(start, end, 1, 1);
                    System.out.println("合并第二列行数为： " + (start + 1) + " 到 " + (end + 1));
                    updateTextArea(ta, "合并第二列行数为： " + (start + 1) + " 到 " + (end + 1));
                    sheet.addMergedRegion(groupCra2);
                }
            }
            fileOutputStream = new FileOutputStream(outFile);
            wb.write(fileOutputStream);
            updateTextArea(ta, "执行结束==========================");
            updateTextArea(ta, "结果文件输出路径为：" + outFilePath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                wb.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (dataWb != null) {
                try {
                    dataWb.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (dataFis != null) {
                try {
                    dataFis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     *
     * @param client httpcliet
     * @param id 公众号文章id
     * @param beginStr 公众号文章评论数起始位置
     * @param commendIdAndName 文章id - 文章名称
     * @param commendIdAndStr 文章id - 文章评论
     * @param token
     * @param cookie
     * @return 文章评论的总个数（只需要第一次请求返回的数据）
     */
    public int getAndParse(HttpClient client, String id, String beginStr, HashMap<String, String> commendIdAndName,
                           HashMap<String, List<NickNameAndComment>> commendIdAndStr, String token, String cookie, TextArea ta) {
        String url = "https://mp.weixin.qq.com/misc/appmsgcomment?action=list_comment&count=20&filtertype=0&" +
                "day=0&type=2&token=" + token + "&lang=zh_CN&f=json&ajax=1&max_id=0&comment_id=";
        System.out.println("文章id = " + id);
        updateTextArea(ta, "文章id = " + id);
        String newUrl = url + id;
        newUrl += beginStr;
        System.out.println("请求url：" + newUrl);
        updateTextArea(ta, "请求url：" + newUrl);
        HttpGet get = new HttpGet(newUrl);
//            HttpHead httpHead = new HttpHead();
//            httpHead.setHeader("cookie", cookie);
        get.setHeader("Cookie", cookie);
        try {
            HttpResponse execute = client.execute(get);
            String content = IOUtils.toString(execute.getEntity().getContent(), "UTF-8");
            JSONObject resultJson = JSONObject.parseObject(content);
            String title = resultJson.getString("title");
            JSONArray commentList = resultJson.getJSONObject("comment_list").getJSONArray("comment");
            // 获取文章标题
            System.out.println("文章标题 = " + title);
            updateTextArea(ta, "文章标题 = " + title);
            commendIdAndName.put(id, title);
            // 获取评论数
            int totalCount = resultJson.getJSONObject("comment_list_count").getIntValue("total_count");
            System.out.println("评论数 = " + totalCount);
            updateTextArea(ta, "评论数 = " + totalCount);
//                System.out.println("commentList = " + commentList.toString());
            // 遍历评论
            for (int j = 0; j < commentList.size(); j++) {
                JSONObject jsonObject = commentList.getJSONObject(j);
                // 获取评论人昵称
                String nickName = jsonObject.getString("nick_name");
                // 获取评论内容
                String commentContent = jsonObject.getString("content");
                NickNameAndComment nc = new NickNameAndComment(nickName, commentContent);
                if (CollectionUtils.isEmpty(commendIdAndStr.get(id))) {
                    List<NickNameAndComment> contentList = new ArrayList<>();
                    contentList.add(nc);
                    commendIdAndStr.put(id, contentList);
                } else {
                    commendIdAndStr.get(id).add(nc);
                }

                System.out.println(nickName + " = " + commentContent);
            }
            return totalCount;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放
            get.releaseConnection();
        }
        return 0;
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
