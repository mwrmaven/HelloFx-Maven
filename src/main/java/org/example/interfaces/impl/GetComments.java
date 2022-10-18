package org.example.interfaces.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
		ta.setPrefHeight(200);
		ta.setEditable(false);
		line6.getChildren().add(ta);

		// 获取cookie中的参数名
		String url = "https://mp.weixin.qq.com/misc/appmsgcomment?action=get_unread_appmsg_comment&has_comment=0&sendtype=MASSSEND&lang=zh_CN&f=json&ajax=1&token=";
		HttpClient client = HttpClients.createDefault();
		Map<String, Integer> commentsMap = new HashMap<>();
		// 按钮点击触发事件
		batchButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// 获取数据文件中的有效条数
				String dataFilePath = ((TextField) getDataFile.get(1)).getText();

				Workbook dataWb = null;
				try {
					if (dataFilePath.endsWith(".xlsx")) {
						dataWb = new XSSFWorkbook(dataFilePath);
					} else {
						dataWb = new HSSFWorkbook(new FileInputStream(dataFilePath));
					}

					// 获取第一个sheet页的文章条数
					Sheet sheet = dataWb.getSheetAt(0);
					int articleNum = sheet.getLastRowNum();
					System.out.println("articleNum = " + articleNum);
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

							return;
						}

						// 遍历评论，并将文章id和评论数放到map中
						for (int j = 0; j < item.size(); j++) {
							JSONObject commentInfo = item.getJSONObject(j);
							commentsMap.put(commentInfo.getString("comment_id"), commentInfo.getIntValue("total_count"));
						}
						httpGet.releaseConnection();
					}

					Font font = dataWb.createFont();
					// excel字体加粗
					font.setBold(true);

					// 在sheet页最后插入一列
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
//						System.out.println("请求url为" + contentUrl);
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
//					System.out.println("评论信息为：" + commentInfoMap);

					// 获取模板文件
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
								pushDate = cell4Value;
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

					// 获取结果模板文件
					dataWb = new XSSFWorkbook(ClassLoader.getSystemResourceAsStream("template/dataResultTemplate.xlsx"));
					Sheet resultSheet = dataWb.getSheetAt(0);
					Set<Integer> pushPeopleKeys = commentInfoMap.keySet();
					for (int i = 0; i < baseList.size(); i++) {
						int rowNum = i + 1;
						Row resultRow = resultSheet.createRow(rowNum);
						TemplateInfo templateInfo = baseList.get(i);
						Cell cell0 = resultRow.createCell(0);
						String tTitle = templateInfo.getTitle();
						cell0.setCellValue(tTitle);
						Cell cell1 = resultRow.createCell(1);
						cell1.setCellValue(templateInfo.getTitleType());
						Cell cell2 = resultRow.createCell(2);
						cell2.setCellValue(templateInfo.getPushDate());
						Cell cell3 = resultRow.createCell(3);
						cell3.setCellValue(templateInfo.getGroup());
						Cell cell11 = resultRow.createCell(11);
						cell11.setCellValue(templateInfo.getPosition());

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

						int excelRowNum = rowNum + 1;
						// 获取map中的评论信息
						CommentInfo commentInfo = commentInfoMap.get(key).get(tTitle);
						Cell cell5 = resultRow.createCell(5);
						cell5.setCellValue(commentInfo.getAllReadPeople());
						Cell cell6 = resultRow.createCell(6);
						cell6.setCellValue(commentInfo.getAllSharePeople());
						Cell cell7 = resultRow.createCell(7);
						String cell7Formula = "=F" + excelRowNum + "/E" + excelRowNum;
						cell7.setCellFormula(cell7Formula);
						Cell cell8 = resultRow.createCell(8);
						String cell8Formula = "=G" + excelRowNum + "/F" + excelRowNum;
						cell8.setCellFormula(cell8Formula);
						Cell cell9 = resultRow.createCell(9);
						cell9.setCellValue(commentInfo.getCompleteReadRate());
						Cell cell10 = resultRow.createCell(10);
						cell10.setCellValue(commentInfo.getCommentNum());
					}

					// 将信息写入到文件
					String extend = dataFilePath.substring(dataFilePath.lastIndexOf("."));
					String newDataFilePath = dataFilePath.substring(0, dataFilePath.lastIndexOf(".")) + "_" + sdf.format(new Date()) + extend;
					File file = new File(newDataFilePath);
					FileOutputStream fos = new FileOutputStream(file);
					dataWb.write(fos);
					fos.close();
					ta.setText("结果文件路径：" + newDataFilePath);
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
				}
				System.out.println("数据处理完成");
			}
		});

		all.getChildren().addAll(line1, line2, line3, line4, line5, line6);
		return ap;
	}
}
