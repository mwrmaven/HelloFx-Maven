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
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
import org.example.button.BatchButton;
import org.example.entity.CommentInfo;
import org.example.entity.TemplateInfo;
import org.example.init.Config;
import org.example.interfaces.Function;
import org.example.util.Unit;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Classname GetComments
 * @Description 获取公众号文章的评论数
 * @Date 2022/10/16 20:03
 * @author mavenr
 */
public class GetComments implements Function {

	private final Unit unit = new Unit();

	private static final String COOKIE = "WECHAT_COOKIE";

	private static final String TOKEN = "WECHAT_TOKEN";

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");

	@Override
	public String tabName() {
		return "微信文章评论数";
	}

	@Override
	public String tabStyle() {
		String style = "-fx-font-weight: bold; " +
				"-fx-background-radius: 10 10 0 0; " +
				"-fx-focus-color: transparent; -fx-text-base-color: white; " +
				"-fx-background-color: #9068be;  -fx-pref-height: 30; ";
		return style;
	}

	@Override
	public AnchorPane tabPane(Stage stage, double width, double h) {

		AnchorPane ap = new AnchorPane();

		VBox all = new VBox();
		all.setPadding(new Insets(10));
		all.setSpacing(10);
		ap.getChildren().add(all);

		// 第一行 详情模板
		HBox line1 = new HBox();
		line1.setAlignment(Pos.CENTER_LEFT);
		line1.setSpacing(10);
		List<Node> getDetailsTemplate = unit.chooseFile(stage, width, "详情模板");
		line1.getChildren().addAll(getDetailsTemplate.get(0), getDetailsTemplate.get(1));

		// 第二行 要整理的数据文件
		HBox line2 = new HBox();
		line2.setAlignment(Pos.CENTER_LEFT);
		line2.setSpacing(10);
		List<Node> getDataFile = unit.chooseFile(stage, width, "数据文件");
		line2.getChildren().addAll(getDataFile.get(0), getDataFile.get(1));

		// 汇总表行，汇总结果的文件
		HBox summaryLine = new HBox();
		summaryLine.setAlignment(Pos.CENTER_LEFT);
		summaryLine.setSpacing(10);
		List<Node> summaryDataFile = unit.chooseFile(stage, width, "汇总文件");
		((TextField) summaryDataFile.get(1)).setPromptText("此项非必填");
		summaryLine.getChildren().addAll(summaryDataFile.get(0), summaryDataFile.get(1));

		// 第三行 输入 cookie
		HBox line3 = new HBox();
		line3.setAlignment(Pos.CENTER_LEFT);
		line3.setSpacing(10);
		List<Node> getCookie = unit.newInputText(width, "请输入cookie : ", 100);
		TextField cookieTextField = (TextField) getCookie.get(1);
		line3.getChildren().addAll(getCookie.get(0), cookieTextField);
		// 加载配置文件中的cookie信息
		String preCookie = Config.get(COOKIE);
		if (StringUtils.isNotBlank(preCookie)) {
			cookieTextField.setText(preCookie);
		}
		// 监听输入框的输入变化
		cookieTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				String newText = cookieTextField.getText();
				if (!newText.equals(preCookie)) {
					Config.set(COOKIE, newText);
				}
			}
		});

		// 第四行 输入 token
		HBox line4 = new HBox();
		line4.setAlignment(Pos.CENTER_LEFT);
		line4.setSpacing(10);
		List<Node> getToken = unit.newInputText(width, "请输入token : ", 100);
		TextField tokenTextField = (TextField) getToken.get(1);
		line4.getChildren().addAll(getToken.get(0), getToken.get(1));
		// 加载配置文件中的token信息
		String preToken = Config.get(TOKEN);
		if (StringUtils.isNotBlank(preToken)) {
			tokenTextField.setText(preToken);
		}
		// 监听输入框的输入变化
		tokenTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				String newText = tokenTextField.getText();
				if (!newText.equals(preToken)) {
					Config.set(TOKEN, newText);
				}
			}
		});

		// 第五行 处理按钮
		HBox line5 = new HBox();
		line5.setAlignment(Pos.CENTER_LEFT);
		line5.setSpacing(10);
		BatchButton batchButtonBuilder = new BatchButton();
		Button batchButton = batchButtonBuilder.createInstance();
		line5.getChildren().add(batchButton);

		// 第六行 多行文本框
		HBox line6 = new HBox();
		line6.setAlignment(Pos.CENTER_LEFT);
		line6.setSpacing(10);
		TextArea ta = new TextArea();
		ta.setPrefWidth(width / 2 - 90);
		ta.setPrefHeight(stage.getHeight() - 400);
		ta.setEditable(false);
		line6.getChildren().add(ta);

		// 按钮点击触发事件
		batchButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				executeButton(ta, getDataFile, cookieTextField, tokenTextField, getDetailsTemplate, summaryDataFile);
			}
		});

		all.getChildren().addAll(line1, line2, summaryLine, line3, line4, line5, line6);
		return ap;
	}

	public void executeButton(TextArea ta, List<Node> getDataFile, TextField cookieTextField, TextField tokenTextField, List<Node> getDetailsTemplate, List<Node> summaryDataFile) {
		ta.setText("");
		String url = "https://mp.weixin.qq.com/misc/appmsgcomment?action=get_unread_appmsg_comment&has_comment=0&sendtype=MASSSEND&lang=zh_CN&f=json&ajax=1&token=";
		HttpClient client = HttpClients.createDefault();
		Map<String, Integer> commentsMap = new HashMap<>();
		Runnable task = new Runnable() {
			@Override
			public void run() {
				// 获取数据文件中的有效条数
				String dataFilePath = ((TextField) getDataFile.get(1)).getText();

				Workbook dataWb = null;
				FileOutputStream fos = null;
				try {
					if (dataFilePath.endsWith(".xlsx")) {
						dataWb = new XSSFWorkbook(dataFilePath);
					} else {
						dataWb = new HSSFWorkbook(new FileInputStream(dataFilePath));
					}
					updateTextArea(ta, "开始读取数据文件！");

					// 获取第一个sheet页的文章条数
					Sheet sheet = dataWb.getSheetAt(0);
					int articleNum = sheet.getLastRowNum();
					System.out.println("articleNum = " + articleNum);
					updateTextArea(ta, "数据文件中的文章条数为：" + articleNum);
					// 获取cookie
					String realCookie = cookieTextField.getText();
					// 获取token
					String realToken = tokenTextField.getText();

					// 请求评论数信息，这里的评论总数按数据文件excel中的文章条数计算
					int count = articleNum;
					int divisor = 30;

					int num = count / divisor;
					if (count % divisor > 0) {
						num++;
					}
					updateTextArea(ta, "开始请求所有文章的评论数！");
					for (int i = 0; i < num; i++) {
						String uri = url + realToken + "&begin=" + i * divisor + "&count=" + divisor;
						HttpGet httpGet = new HttpGet(uri);
						httpGet.setHeader("cookie", realCookie);

						HttpResponse response = client.execute(httpGet);
						String content = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
						JSONObject jsonObject = JSONObject.parseObject(content);
						// 获取评论数组
						JSONArray item = jsonObject.getJSONArray("item");
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
						if ("内容url".equals(titleRow.getCell(i).getStringCellValue())) {
							lastNum = i;
							titleCellStyle = titleRow.getCell(i).getCellStyle();
							break;
						}
					}

					// 存储数据文件中每行的数据的map，先以送达人数为key，再以文章标题为key
					Map<Integer, Map<String, CommentInfo>> commentInfoMap = new HashMap<>();
					for (int i = 0; i <= sheet.getLastRowNum(); i++) {
//						System.out.println("查询下标 " + i + "行");
						Row row = sheet.getRow(i);
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
						String contentUrl = row.getCell(lastNum).getStringCellValue();
						CellStyle preCellStyle = row.getCell(lastNum).getCellStyle();
						cell.setCellStyle(preCellStyle);
						System.out.println("请求url为" + contentUrl);
						updateTextArea(ta, "请求第" + (newLastNum + 1) + "行的url");
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
								commentNum = integer;
								cell.setCellValue(integer);
								break;
							}
						}
						updateTextArea(ta, "获取到公众号文章ID，并插入对应的评论数");
						httpGet.releaseConnection();
						// 将行数据放入到map中
						String articleTitle = row.getCell(0).getStringCellValue();
						int pushPeople = Integer.parseInt(row.getCell(7).getStringCellValue());
						CommentInfo info = CommentInfo.builder()
								.title(articleTitle.trim())
								.pushDate(row.getCell(1).getStringCellValue())
								.allReadPeople(Integer.valueOf(row.getCell(2).getStringCellValue()))
								.allSharePeople(Integer.valueOf(row.getCell(4).getStringCellValue()))
								.pushPeople(pushPeople)
								.completeReadRate(row.getCell(14).getStringCellValue())
								.commentNum(commentNum)
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
					updateTextArea(ta, "数据文件的最后插入一列插入完成！");

					// 获取模板文件
					updateTextArea(ta, "开始读取模板文件！");
					String templateFilePath = ((TextField) getDetailsTemplate.get(1)).getText();
					if (templateFilePath.endsWith(".xlsx")) {
						dataWb = new XSSFWorkbook(templateFilePath);
					} else {
						dataWb = new HSSFWorkbook(new FileInputStream(templateFilePath));
					}
					// 遍历行
					Sheet templateSheet = dataWb.getSheetAt(0);
					List<TemplateInfo> baseList = new ArrayList<>();
					String group = "";
					String pushDate = "";
					int pushPeople = -1;
					boolean flag = false;
					for (int i = 2; i <= templateSheet.getLastRowNum(); i++) {
						Row row = templateSheet.getRow(i);
						if (row == null || row.getCell(0) == null
								|| StringUtils.isBlank(row.getCell(0).getStringCellValue())) {
							continue;
						} else {
							TemplateInfo info = TemplateInfo.builder()
									.title(row.getCell(0).getStringCellValue().trim())
									.titleType(row.getCell(1).getStringCellValue())
									.position(BigDecimal.valueOf(row.getCell(2).getNumericCellValue()).intValue())
									.build();
							String cell3Value = row.getCell(3).getStringCellValue();
							if (StringUtils.isNotBlank(cell3Value)) {
								group = cell3Value;
								flag = true;
							}
							String cell4Value = row.getCell(4).getStringCellValue();
							if (StringUtils.isNotBlank(cell4Value)) {
								pushDate = cell4Value.trim();
								if (pushDate.contains(".")) {
									pushDate = pushDate.substring(0, pushDate.indexOf(".")) + "月" + pushDate.substring(pushDate.indexOf(".") + 1) + "日";
								}
							}
							if (flag) {
								int cell5Value = BigDecimal.valueOf(row.getCell(5).getNumericCellValue()).intValue();
								pushPeople = cell5Value;
								flag = false;
							}
							info.setGroup(group);
							info.setPushDate(pushDate);
							info.setPushPeople(pushPeople);
							baseList.add(info);
						}
					}
					updateTextArea(ta, "获取到模板文件中所有数据！");

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
						Row resultRow = resultSheet.createRow(rowNum);
						// 设置行高 22
						resultRow.setHeightInPoints(22);
						TemplateInfo templateInfo = baseList.get(i);
						Cell cell0 = resultRow.createCell(0);
						CellStyle leftCellStyle = dataWb.createCellStyle();
						leftCellStyle.cloneStyleFrom(cloneCellStyle);
						leftCellStyle.setBorderLeft(BorderStyle.MEDIUM);
						String tTitle = templateInfo.getTitle();
						cell0.setCellValue(tTitle);
						cell0.setCellStyle(leftCellStyle);
						Cell cell1 = resultRow.createCell(1);
						cell1.setCellValue(templateInfo.getTitleType());
						cell1.setCellStyle(cloneCellStyle);
						Cell cell2 = resultRow.createCell(2);
						cell2.setCellValue(templateInfo.getPushDate());
						cell2.setCellStyle(cloneCellStyle);
						Cell cell3 = resultRow.createCell(3);
						String groupValue = templateInfo.getGroup();
						if (!groupStandard.equals(groupValue)) {
							beginAndEnd.add(rowNum);
							groupStandard = groupValue;
						}
						cell3.setCellValue(templateInfo.getGroup());
						cell3.setCellStyle(cloneCellStyle);
						Cell cell11 = resultRow.createCell(11);
						cell11.setCellValue(templateInfo.getPosition());
						CellStyle rightCellStyle = dataWb.createCellStyle();
						rightCellStyle.cloneStyleFrom(cloneCellStyle);
						rightCellStyle.setBorderRight(BorderStyle.MEDIUM);
						cell11.setCellStyle(rightCellStyle);

						int tpush = templateInfo.getPushPeople();
						int key = -1;
						int min = 1000000000;
						for (Integer k : pushPeopleKeys) {
							if (Math.abs(tpush - k) < min) {
								key = k;
								min = Math.abs(tpush - k);
							}
						}
						Cell cell4 = resultRow.createCell(4);
						cell4.setCellValue(key);
						cell4.setCellStyle(cloneCellStyle);

						int excelRowNum = rowNum + 1;
						// 获取map中的评论信息
						CommentInfo commentInfo = commentInfoMap.get(key).get(tTitle);
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
						Workbook summaryWb;
						if (summaryFilePath.endsWith(".xlsx")) {
							summaryWb = new XSSFWorkbook(summaryFilePath);
						} else {
							summaryWb = new HSSFWorkbook(new FileInputStream(summaryFilePath));
						}
						// 获取文件的起始行(行为空或单元格为空或单元格数据为空字符串)
						String sheetName = pushDate.substring(0, pushDate.indexOf("月") + 1).trim();
						Sheet summarySheet = summaryWb.getSheet(sheetName);
						// 设置公式自动计算
						summarySheet.setForceFormulaRecalculation(true);
						int startLine = 0;
						for (int i = 0; i < summarySheet.getLastRowNum(); i++) {
							Row summaryRow = summarySheet.getRow(i);
							if (summaryRow != null && summaryRow.getCell(0) != null
									&& StringUtils.isNotBlank(summaryRow.getCell(0).getStringCellValue())) {
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
							for (int j = 0; j <= 11; j++) {
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
									} else if (j == 0 || j == 1 || j == 2 || j == 3) {
										newCell.setCellValue(dataCell.getStringCellValue());
									} else if (j == 4 || j == 5 || j == 6 || j == 9 || j == 10 || j == 11) {
										newCell.setCellValue(dataCell.getNumericCellValue());
									}
								} else {
									newCell.setCellValue(dataCell.getStringCellValue());
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
							if (!pushDate.equals(resultSheet.getRow(oldFirstRow).getCell(cra.getFirstColumn()).getStringCellValue())) {
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
							Cell cell3 = row.getCell(3);
							if (!countFlag && (cell3 == null || !cell3.getCellType().equals(CellType.STRING))) {
								continue;
							}
							if (cell3.getCellType().equals(CellType.STRING) && "推送时间".equals(cell3.getStringCellValue().trim())) {
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
						updateTextArea(ta, "结果汇总文件导出完成！结果汇总文件路径：" + newDataFilePath);
					}
				} catch (Exception e) {
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

	public void updateTextArea(TextArea ta, String message) {
		ta.appendText("\n" + message);
//		if (Platform.isFxApplicationThread()) {
//			ta.appendText("\n" + message);
//		} else {
//			Platform.runLater(() -> ta.appendText("\n" + message));
//		}
	}
}
