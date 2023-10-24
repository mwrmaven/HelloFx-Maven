package org.example.interfaces.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mavenr.email.MavenrQQEmail;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.button.BatchButton;
import org.example.common.ExcelContent;
import org.example.entity.ArticleLink;
import org.example.entity.CommentInfo;
import org.example.entity.TemplateInfo;
import org.example.init.Config;
import org.example.interfaces.Function;
import org.example.service.GetCommentNumService;
import org.example.util.SocketUtil;
import org.example.util.TimeUtil;
import org.example.util.Unit;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Classname GetCommentsNew
 * @Description 获取公众号文章的评论数-新版
 * @Date 2022/10/16 20:03
 * @author mavenr
 */
public class GetCommentsNew implements Function {

	/**
	 * 工具类
	 */
	private Unit unit = new Unit();

	/**
	 * 时间工具类
	 */
	private TimeUtil timeUtil = new TimeUtil();

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");

	/**
	 * chrome浏览器启动器的文件路径
	 */
	private static final String CHROMESTARTPATH = "CHROMESTARTPATH";

	/**
	 * 请求评论的页数
	 */
	private static final String COMMENTPAGENUM = "COMMENTPAGENUM";

	/**
	 * 公众号草稿箱网页地址
	 */
	private static final String DRAFTSURL = "DRAFTSURL";

	/**
	 * 目标邮件的地址
	 */
	private static final String TO = "TO";

	/**
	 * 邮箱用户名
	 */
	private static final String EMAILUSERNAME = "EMAILUSERNAME";

	/**
	 * 邮箱用户授权码
	 */
	private static final String EMAILPASSWORD = "EMAILPASSWORD";

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter timestampDtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * 标签名称
	 * @return
	 */
	@Override
	public String tabName() {
		return "自动化处理";
	}

	/**
	 * 标签颜色
	 * @return
	 */
	@Override
	public String tabStyle() {
		String style = "-fx-font-weight: bold; " +
				"-fx-background-radius: 10 10 0 0; " +
				"-fx-focus-color: transparent; -fx-text-base-color: white; " +
				"-fx-background-color: #87CEFA;  -fx-pref-height: 30; ";
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
		Label label = new Label("请点击按钮启动测试浏览器，然后登陆公众号平台（注意：只能登陆一个公众号平台）");
		label.setStyle("-fx-text-fill: red; -fx-font-size: 20");
		tips.getChildren().add(label);

		HBox tips2 = new HBox();
		Label label2 = new Label("请确认当前路径下的chromedriver驱动版本与浏览器的版本一致");
		label2.setStyle("-fx-text-fill: red; -fx-font-size: 20");
		tips2.getChildren().add(label);

		// 以下添加单选
		RadioButton draft = new RadioButton("草稿箱链接");
		RadioButton timeComments = new RadioButton("定时获取微信文章评论数");
		ToggleGroup conditon = new ToggleGroup();
		// 单选设为同组
		draft.setToggleGroup(conditon);
		draft.setSelected(true);
		timeComments.setToggleGroup(conditon);

		HBox radio = new HBox();
		radio.setAlignment(Pos.CENTER_LEFT);
		radio.setSpacing(10);
		radio.getChildren().addAll(draft, timeComments);

		// 第一行，获取模板文件
		HBox line1 = new HBox();
		line1.setAlignment(Pos.CENTER_LEFT);
		line1.setSpacing(10);
		List<Node> template = unit.chooseFile(stage, width, "模板文件");
		for (Node n : template) {
			line1.getChildren().add(n);
		}

		HBox line1Next = new HBox();
		line1Next.setAlignment(Pos.CENTER_LEFT);
		line1Next.setSpacing(10);
		List<Node> dataFile = unit.chooseFile(stage, width, "数据文件");
		for (int i = 0; i < dataFile.size(); i++) {
			Node n = dataFile.get(i);
			line1Next.getChildren().add(n);
			if (i == 1) {
				((TextField) n).setPromptText("使用自动下载数据文件时，该项可以不选择");
			}
		}


		HBox line1Next1 = new HBox();
		line1Next1.setAlignment(Pos.CENTER_LEFT);
		line1Next1.setSpacing(10);
		List<Node> summary = unit.chooseFile(stage, width, "汇总文件");
		for (Node n : summary) {
			line1Next1.getChildren().add(n);
		}

		// 第二行，请输入草稿箱页面的网页地址
//		HBox line2 = new HBox();
//		line2.setAlignment(Pos.CENTER_LEFT);
//		line2.setSpacing(10);
//		List<Node> drafts = unit.newInputText(width, "请输入草稿箱页面的网页地址(可不填)：", 250);
//		for (Node n : drafts) {
//			line2.getChildren().add(n);
//		}

		HBox commentPageNumLine = new HBox();
		commentPageNumLine.setAlignment(Pos.CENTER_LEFT);
		commentPageNumLine.setSpacing(10);
		List<Node> commentNodes = unit.newInputText(width, "请输入查询评论的页数(可不填)：", 250);
		for (Node n : commentNodes) {
			commentPageNumLine.getChildren().add(n);
		}
		TextField commentTf = (TextField) commentNodes.get(1);
		commentTf.setPromptText("默认请求按文章发表时间排序的前5页！");
		// 查询配置文件中请求评论页数
		String configCommentPageNum = Config.get(COMMENTPAGENUM);
		if (StringUtils.isNotBlank(configCommentPageNum)) {
			commentTf.setText(configCommentPageNum);
		}
		// 失去焦点触发保存事件
		commentTf.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// 判断内容改变，则保存内容
				if (!text.equals(commentTf.getText())) {
					// 设置配置文件
					Config.set(COMMENTPAGENUM, commentTf.getText());
				}
			}
		});

		// 当天日期
		LocalDate nowDay = LocalDate.now();
		// 当前时间
		LocalTime nowTime = LocalTime.now();

		// 下载文件的开始日期和结束日期
		HBox dataTimeHbox = new HBox();
		dataTimeHbox.setAlignment(Pos.CENTER_LEFT);
		dataTimeHbox.setSpacing(10);
		Label labelDataTime = new Label("请选择自动下载数据文件的开始和结束时间（注意开始时间要在发文之前）：");
		Label labelStartTime = new Label("开始时间：");
		DatePicker startDatePicker = new DatePicker();
		Label labelEndTime = new Label("结束时间：");
		DatePicker endDatePicker = new DatePicker();
		endDatePicker.setValue(nowDay);
		dataTimeHbox.getChildren().addAll(labelStartTime, startDatePicker, labelEndTime, endDatePicker);

		// 定时时间设置
		HBox timeHbox = new HBox();
		timeHbox.setAlignment(Pos.CENTER_LEFT);
		timeHbox.setSpacing(10);
		Label labelTime = new Label("请选择定时任务执行时间（如果设置为当前时间之前，则立即执行）：日期：");
		DatePicker timeFirst = new DatePicker();
		timeFirst.setValue(nowDay);
		Label labelTimeAppend = new Label("时间：");
		ComboBox<String> hComb = new ComboBox<>();
		for (int i = 0; i < 25; i++) {
			String hTime = String.valueOf(i);
			if (i < 10) {
				hTime = "0" + hTime;
			}
			hComb.getItems().add(hTime);
		}
		hComb.getSelectionModel().select(nowTime.getHour());
		ComboBox<String> mComb = new ComboBox<>();
		for (int i = 0; i < 60; i++) {
			String mTime = String.valueOf(i);
			if (i < 10) {
				mTime = "0" + mTime;
			}
			mComb.getItems().add(mTime);
		}
		mComb.getSelectionModel().select(nowTime.getMinute());
		timeHbox.getChildren().addAll(labelTime, timeFirst, labelTimeAppend, hComb, mComb);

		// 发送的目标邮件的地址，多个以英文;分隔
		HBox toAddressHbox = new HBox();
		toAddressHbox.setAlignment(Pos.CENTER_LEFT);
		toAddressHbox.setSpacing(10);
		List<Node> toNodes = unit.newInputText(width - 80, "请输入目标邮件的地址(多个以英文;分隔)：", 290);
		for (Node n : toNodes) {
			toAddressHbox.getChildren().add(n);
		}
		String to = Config.get(TO);
		TextField n1 = (TextField) toNodes.get(1);
		if (StringUtils.isNotBlank(to)) {
			n1.setText(to);
		}
		String n1Text = n1.getText();
		n1.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// 判断内容改变，则保存内容
				if (!n1Text.equals(n1.getText())) {
					// 设置配置文件
					Config.set(TO, n1.getText());
				}
			}
		});

		// 草稿箱页面的网页路径
//		TextField draftsPathTf = (TextField) drafts.get(1);
		// 设置样式为下划线
//		String draftsUrl = Config.get(DRAFTSURL);
		// 加载配置文件中的参数
//		if (StringUtils.isNotEmpty(draftsUrl)) {
//			draftsPathTf.setText(draftsUrl);
//		}
//		String draftsText = draftsPathTf.getText();
		// 失去焦点触发保存事件
//		draftsPathTf.focusedProperty().addListener(new ChangeListener<Boolean>() {
//			@Override
//			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//				// 判断内容改变，则保存内容
//				if (!draftsText.equals(draftsPathTf.getText())) {
//					// 设置配置文件
//					Config.set(DRAFTSURL, draftsPathTf.getText());
//				}
//			}
//		});

		// 在切换到草稿箱链接和微信文章评论数时，隐藏该部分
		VBox hiddenVbox = new VBox();
		hiddenVbox.setSpacing(10);
		hiddenVbox.getChildren().addAll(labelDataTime, dataTimeHbox, timeHbox, toAddressHbox);
		// 在切换到草稿箱链接时，隐藏该部分
		VBox hiddenFilePath = new VBox();
		hiddenFilePath.setSpacing(10);
		hiddenFilePath.getChildren().addAll(line1Next1, line1Next, commentPageNumLine);

		HBox line3 = new HBox();
		line3.setAlignment(Pos.CENTER_LEFT);
		line3.setSpacing(10);
		// 第三行，批量处理按钮
		BatchButton batchButtonBuilder = new BatchButton();
		Button batchButton = batchButtonBuilder.createInstance();
		line3.getChildren().add(batchButton);

		// 第四行，多行文本
		HBox line4 = new HBox();
		line4.setAlignment(Pos.CENTER_LEFT);
		line4.setSpacing(10);
		TextArea ta = new TextArea();
		ta.setPrefWidth(width / 2);
		ta.setPrefHeight(stage.getHeight() - 650);
		ta.setEditable(false);
		line4.getChildren().add(ta);

		vBox.getChildren().addAll(line1Pre, tips2, tips, radio, line1, line3, line4);

		// 当分别选择“草稿箱链接”、“微信文章评论数”、“定时获取微信文章评论数”时触发的事件
		conditon.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				String text = ((RadioButton) newValue).getText();
				ta.setText("");
				if (text.equals(draft.getText())) {
					// 草稿箱链接、微信文章评论数，移除 hiddenVbox
					if (vBox.getChildren().contains(hiddenVbox)) {
						vBox.getChildren().remove(hiddenVbox);
					}
				} else if (text.equals(timeComments.getText())) {
					// 定时获取微信文章评论数，添加 hiddenVbox
					if (!vBox.getChildren().contains(hiddenVbox)) {
						vBox.getChildren().add(vBox.getChildren().indexOf(line3), hiddenVbox);
					}
				}

				if (text.equals(draft.getText())) {
					// 草稿箱链接，移除 hiddenFilePath
					if (vBox.getChildren().contains(hiddenFilePath)) {
						vBox.getChildren().remove(hiddenFilePath);
					}
				} else if (text.equals(timeComments.getText())) {
					// 微信文章评论数、定时获取微信文章评论数，添加 hiddenFilePath
					if (!vBox.getChildren().contains(hiddenFilePath)) {
						vBox.getChildren().add(vBox.getChildren().indexOf(line1) + 1, hiddenFilePath);
					}
				}

//				if (text.equals(timeComments.getText())) {
//					// 定时获取微信文章评论数中移除 数据文件选项
//					if (hiddenFilePath.getChildren().contains(line1Next)) {
//						hiddenFilePath.getChildren().remove(line1Next);
//					}
//				}
			}
		});

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

		// 添加处理事件
		batchButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// 查看端口是否被占用，如果被占用则先停掉端口再启动 区分 windows 和 mac ，命令行也是
				boolean alive = SocketUtil.isAlive("127.0.0.1", 9527);
				if (!alive) {
					ta.setText("测试浏览器未启动，请点击上面的\"启动测试浏览器\"按钮，启动测试浏览器！");
					return;
				}

				// 判断是草稿箱还是微信文章评论数
				String functionName = ((RadioButton) conditon.getSelectedToggle()).getText();
				Alert alert = new Alert(Alert.AlertType.NONE,
						"确认要获取：" + functionName + " ?",
						new ButtonType("取消", ButtonBar.ButtonData.NO),
						new ButtonType("确定", ButtonBar.ButtonData.YES));
				alert.setTitle("请确认功能");
				// 等待选择，在对话框消失前不会执行之后的代码
				Optional<ButtonType> buttonType = alert.showAndWait();
				if (ButtonBar.ButtonData.NO.equals(buttonType.get().getButtonData())) {
					return;
				}

				// 获取请求评论页数，默认为5页
				int commentPageNum = 5;
				String cpnText = commentTf.getText().trim();
				if (StringUtils.isNotBlank(cpnText) && cpnText.matches("\\d+")) {
					commentPageNum = Integer.parseInt(cpnText);
				}
				int pageNum = commentPageNum;

				if ("草稿箱链接".equals(functionName)) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							ta.setText("");
							long start = System.currentTimeMillis();
							// 获取模板文件路径
							String templatePath = ((TextField) template.get(1)).getText();
							if (StringUtils.isBlank(templatePath)) {
								ta.setText("请选择模板文件！");
								return;
							}
							// 获取草稿箱网页地址
//							String draftsUrl = ((TextField) drafts.get(1)).getText();
							try {
								debugChrome(templatePath, "", ta);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}

							long end = System.currentTimeMillis();
							System.out.println("程序执行耗时：" + (end - start) + " ms");
							BigDecimal bd = BigDecimal.valueOf((double) (end - start) / 60000);
							updateTextArea(ta, "程序执行耗时约：" + bd.setScale(2, RoundingMode.HALF_UP).doubleValue() + " 分钟");

							// 关闭chrome测试浏览器端口
							SocketUtil.kill(9527);
						}
					}).start();
				} else if ("微信文章评论数".equals(functionName)) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							executeButton(ta, dataFile, template, summary, pageNum);
						}
					}).start();
				} else if ("定时获取微信文章评论数".equals(functionName)) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							ta.setText("");

							if (StringUtils.isNotBlank(n1.getText())) {
								// 发送邮件
								String username = Config.get(EMAILUSERNAME);
								String password = Config.get(EMAILPASSWORD);
								password = "en" + password;
								// base64解密
								String realPassword = new String(Base64.getDecoder().decode(password.getBytes()));
								MavenrQQEmail email = new MavenrQQEmail(username, realPassword);
								// 测试接口连接
								boolean emailFlag = email.checkMailUserBySmtp();
								if (!emailFlag) {
									updateTextArea(ta, "未配置邮箱相关参数，或配置错误，请确认！");
									return;
								}
							}

							// 获取模板文件路径
							String templatePath = ((TextField) template.get(1)).getText();
							if (StringUtils.isBlank(templatePath)) {
								updateTextArea(ta, "请选择模板文件");
								return;
							}
							// 获取汇总文件路径
							String summaryPath = ((TextField) summary.get(1)).getText();
							if (StringUtils.isBlank(summaryPath)) {
								updateTextArea(ta, "请选择汇总文件");
								return;
							}
							// 获取数据文件路径
							String dataFilePath = ((TextField) dataFile.get(1)).getText();
							String start = null;
							String end = null;
							if (StringUtils.isNotBlank(dataFilePath)) {
								updateTextArea(ta, "数据文件路径：" + dataFilePath);
							} else {
								// 获取开始时间和结束时间
								LocalDate startDate = startDatePicker.getValue();
								LocalDate endDate = endDatePicker.getValue();
								if (startDate == null || endDate == null) {
									updateTextArea(ta, "请选择下载数据文件的开始时间和结束时间");
									return;
								}
								if (startDate.isAfter(endDate)) {
									updateTextArea(ta, "结束时间需大于等于开始时间");
									return;
								}
								start = startDate.format(dtf);
								end = endDate.format(dtf);
								System.out.println("开始时间：" + start + "; 结束时间：" + end);
								updateTextArea(ta, "开始时间：" + start + "; 结束时间：" + end);
							}

							// 定时时间
							LocalDate timeDate = timeFirst.getValue();
							String hString = hComb.getSelectionModel().getSelectedItem();
							String mString = mComb.getSelectionModel().getSelectedItem();
							String timeStamp = timeDate + " " + hString + ":" + mString + ":00";
							System.out.println("定时时间：" + timeStamp);
							updateTextArea(ta, "定时时间：" + timeStamp);

							LocalDateTime timeFlag = LocalDateTime.parse(timeStamp, timestampDtf);
							if (timeFlag.isBefore(LocalDateTime.now())) {
								// 直接执行
								GetCommentNumService service = new GetCommentNumService();
								if (StringUtils.isNotBlank(dataFilePath)) {
									service.executeButton(ta, dataFile, template, summary, pageNum,
											false, start, end, true, n1.getText());
								} else {
									service.executeButton(ta, dataFile, template, summary, pageNum,
											true, start, end, true, n1.getText());
								}
							} else {
								// 生成定时任务
								// 1、创建调度器
								SchedulerFactory schedulerFactory = new StdSchedulerFactory();
								Scheduler scheduler = null;
								try {
									scheduler = schedulerFactory.getScheduler();
									// 添加定时任务使用到的参数
									JobDataMap jobDataMap = new JobDataMap();
									jobDataMap.put("ta", ta);
									jobDataMap.put("dataFile", dataFile);
									jobDataMap.put("template", template);
									jobDataMap.put("summary", summary);
									jobDataMap.put("pageNum", pageNum);
									jobDataMap.put("dataStartTime", start);
									jobDataMap.put("dataEndTime", end);
									jobDataMap.put("sendMail", true);
									jobDataMap.put("to", n1.getText());
									// 添加参数
									JobDetail jobDetail = JobBuilder.newJob(GetCommentNumService.class)
											.withIdentity("myJob", "jobGroup")
											.usingJobData(jobDataMap).build();
									// 创建触发器
									Trigger trigger = TriggerBuilder.newTrigger()
											.withIdentity("myTrigger", "triggerGroup")
											.withSchedule(CronScheduleBuilder.cronSchedule(timeUtil.cron(timeFlag)))
											.build();
									// 执行
									scheduler.scheduleJob(jobDetail, trigger);
									System.out.println("定时任务开始执行！");
									updateTextArea(ta, "定时任务开始执行！");
									scheduler.start();

									// 计算定时任务时间到现在的时间，再加3分钟
									LocalDateTime now = LocalDateTime.now();
									Duration duration = Duration.between(now, timeFlag);
									long millis = duration.toMillis();

									// 睡眠等待
									Thread.sleep(millis + (60 * 1000 * 3));


								} catch (Exception e) {
									throw new RuntimeException(e);
								} finally {
									if (scheduler != null) {
										// 停止
										try {
											scheduler.shutdown(true);
											System.out.println("定时任务执行结束！");
										} catch (Exception e) {
											throw new RuntimeException(e);
										}
									}
								}
							}
						}
					}).start();



				} else {
					Alert alertError = new Alert(Alert.AlertType.ERROR,
							"当前暂无：" + functionName + " 功能",
							new ButtonType("确认", ButtonBar.ButtonData.YES));
				}


			}
		});

		return root;
	}

	/**
	 * 解析模板excel文件
	 * @param workbook
	 * @return
	 */
	public Map<String, int[]> parseExcel(Workbook workbook) {
		try {
			Sheet sheet = workbook.getSheetAt(0);
			// 标题行
			Row titleRow = sheet.getRow(1);
			// 获取组别列
			int groupIndex = -1;
			int positionIndex = -1;
			for (int i = 0; i <= titleRow.getLastCellNum(); i++) {
				Cell cell = titleRow.getCell(i);
				if (cell == null) {
					continue;
				}
				if ("组别".equals(unit.getCellValue(cell).trim())) {
					groupIndex = i;
				}
				if ("位置".equals(unit.getCellValue(cell).trim())) {
					positionIndex = i;
				}
			}
			if (groupIndex == -1) {
				System.out.println("未查询到\"组别\"列");
				return null;
			}
			if (positionIndex == -1) {
				System.out.println("未查询到\"位置\"列");
				return null;
			}
			// 遍历行
			// 组别类以及组别的开始行和结束行
			Map<String, int[]> startAndEnd = new HashMap<>();
			String name = null;
			int end = -1;
			for (int i = 2; i <= sheet.getLastRowNum(); i++) {
				if (i == sheet.getLastRowNum()) {
					// 给上一个组别结束行赋值
					if (name != null) {
						startAndEnd.get(name)[1] = i;
					}
					break;
				}
				Row row = sheet.getRow(i);
				if (row == null) {
					// 空行表示读取结束
					break;
				}
				// 判断位置列是否为空，为空则也结束
				Cell positionCell = row.getCell(positionIndex);
				if (positionCell == null || positionCell.getCellType().equals(CellType.BLANK)) {
					// 给上一个组别结束行赋值
					if (name != null) {
						startAndEnd.get(name)[1] = end;
					}
					break;
				}

				Cell cell = row.getCell(groupIndex);
				if (cell == null) {
					break;
				}
				String cellValue = unit.getCellValue(cell).trim();
				if (StringUtils.isNotBlank(cellValue)) {
					// 给上一个组别结束行赋值
					if (name != null) {
						startAndEnd.get(name)[1] = end;
					}
					name = cellValue;
					int[] se = new int[2];
					se[0] = i;
					startAndEnd.put(cellValue, se);
				} else {
					end = i;
				}
			}
			return startAndEnd;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 解析获取公众号草稿箱网页
	 * @param templateFilePath
	 * @param draftsUrl
	 * @param ta
	 * @throws Exception
	 */
	public void debugChrome(String templateFilePath, String draftsUrl, TextArea ta) throws Exception{
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
		updateTextArea(ta, "创建驱动");

		if (StringUtils.isBlank(draftsUrl)) {
			List<WebElement> aList = driver.findElements(By.tagName("a"));
			// 获取草稿箱的父目录名称
			String draftsParentMenu = Config.get("DRAFTSPARENTMENU");
			updateTextArea(ta, "查询到配置文件中草稿箱的父目录名称为：" + draftsParentMenu);
			System.out.println("查询到配置文件中草稿箱的父目录名称为：" + draftsParentMenu);
			for (WebElement w : aList) {
				if (draftsParentMenu.equals(w.getText())) {
					updateTextArea(ta, "点击“" + draftsParentMenu + "”目录");
					w.click();
					Thread.sleep(500);
					break;
				}
			}
			boolean flag = true;
			for (WebElement w : aList) {
				if ("草稿箱".equals(w.getAttribute("title"))) {
					flag = false;
					updateTextArea(ta, "获取到草稿箱链接：" + w.getAttribute("href"));
					w.click();
					updateTextArea(ta, "跳转到草稿箱，等待5s");
					Thread.sleep(5000);
					break;
				}
			}

			if (flag) {
				updateTextArea(ta, "未查询到草稿箱，请手动补充");
				return;
			} else {
				updateTextArea(ta, "跳转草稿箱成功");
				draftsUrl = driver.getCurrentUrl();
			}
		} else {
			updateTextArea(ta, "请求填写的草稿箱地址为：" + draftsUrl);
			driver.get(draftsUrl);
		}

		File file = new File(templateFilePath);
		FileInputStream fis = new FileInputStream(file);
		Workbook workbook;
		if (templateFilePath.endsWith(ExcelContent.XLSX)) {
			workbook = new XSSFWorkbook(fis);
		} else if (templateFilePath.endsWith(ExcelContent.XLS)) {
			workbook = new HSSFWorkbook(fis);
		} else {
			workbook = new XSSFWorkbook(fis);
		}
		updateTextArea(ta, "访问文件：" + templateFilePath);
		// 获取组别
		Map<String, int[]> groupAndPosition = parseExcel(workbook);
		Set<String> groupSet = groupAndPosition.keySet();
		for (String g : groupSet) {
			updateTextArea(ta, "获取到组别：" + g);
		}
		// 文章链接集合
		Map<String, List<ArticleLink>> groupAndArticleList = new HashMap<>();
		for (int i = 0; i < 21; i+=10) {
			String oldStr = "begin=0";
			if (draftsUrl.contains("begin=10")) {
				oldStr = "begin=10";
			} else if (draftsUrl.contains("begin=20")) {
				oldStr = "begin=20";
			} else if (draftsUrl.contains("begin=30")) {
				oldStr = "begin=30";
			}
			draftsUrl = draftsUrl.replace(oldStr, "begin=" + i);
			driver.get(draftsUrl);
			updateTextArea(ta, "请求草稿箱第 " + (i / 10 + 1) + " 页");
			String currentWindowHandle = driver.getWindowHandle();
			Thread.sleep(5000);
			System.out.println("======================");

			// 以组为单位获取元素
			List<WebElement> parentElements = driver.findElements(By.className("weui-desktop-card__bd"));
			Map<String, WebElement> groupAndParentElement = new HashMap<>();
			updateTextArea(ta, "开始获取草稿箱中文章组信息");
			for (WebElement we : parentElements) {
				// 获取主条
				List<WebElement> elements = we.findElements(By.className("weui-desktop-publish__cover__title"));
				if (CollectionUtils.isEmpty(elements)) {
					continue;
				}
				// 判断主条是否匹配组别
				for (String group : groupSet) {
					if (elements.get(0).getText().contains(group)) {
						groupAndParentElement.put(group, we);
						break;
					}
				}
			}
			updateTextArea(ta, "草稿箱中文章组信息获取完成");

			updateTextArea(ta, "开始循环点击文章");
			// 循环组元素
			for (String key : groupAndParentElement.keySet()) {
				updateTextArea(ta, "========" + key + "========");
				// 如果map中根据key获取的value为空，则创建一个空的集合
				List<ArticleLink> articleList = groupAndArticleList.computeIfAbsent(key, k -> new ArrayList<>());
				WebElement parent = groupAndParentElement.get(key);
				// 获取组中的所有链接
				List<WebElement> elements = new ArrayList<>();
				elements.addAll(parent.findElements(By.className("weui-desktop-publish__cover__title")));
				elements.addAll(parent.findElements(By.className("weui-desktop-publish__title")));

				for (WebElement we : elements) {
//					System.out.println(we.getText());
					we.click();
					Thread.sleep(5000);

					Set<String> windowHandles = driver.getWindowHandles();
					for (String wh : windowHandles) {
						driver.switchTo().window(wh);
						if (driver.getTitle().equals(currentWindowHandle) || driver.getTitle().equals("公众号")
								|| driver.getTitle().equals("Official Accounts")) {
							continue;
						}
						Thread.sleep(2000);
						String title = driver.getTitle();
						String articleUrl = driver.getCurrentUrl();
						updateTextArea(ta, title + " : " + articleUrl);
						ArticleLink articleLink = ArticleLink.builder()
								.title(title)
								.url(articleUrl)
								.build();
						articleList.add(articleLink);
						// 关闭当前标签页
						driver.close();
					}
					driver.switchTo().window(currentWindowHandle);
				}
			}
			updateTextArea(ta, "循环点击文章结束");
		}

		if (groupAndArticleList.size() == 0) {
			updateTextArea(ta, "未查询到公众号文章的链接地址！");
			return;
		}

		// 创建一个新结果文件
		String newPath = templateFilePath.substring(0, templateFilePath.lastIndexOf("."))
				+ "_" + sdf.format(new Date()) + templateFilePath.substring(templateFilePath.lastIndexOf("."));
		File newFile = new File(newPath);

		// 在新结果文件中写入数据
		FileOutputStream fos = new FileOutputStream(newFile);
		// 读取sheet页
		Sheet sheet = workbook.getSheetAt(0);
		// 获取文章标题列和文章链接列的位置
		Row titleRow = sheet.getRow(1);
		int titleIndex = -1;
		int linkIndex = -1;
		for (int i = 0; i <= titleRow.getLastCellNum(); i++) {
			Cell cell = titleRow.getCell(i);
			if (cell == null || cell.getCellType().equals(CellType.BLANK)
					|| StringUtils.isBlank(unit.getCellValue(cell))) {
				continue;
			}
			if ("文章标题".equals(unit.getCellValue(cell).trim())) {
				titleIndex = i;
				continue;
			}
			if ("文章链接".equals(unit.getCellValue(cell).trim())) {
				linkIndex = i;
			}
		}
		if (titleIndex == -1) {
			updateTextArea(ta, "请确认模板文件中是否包含文章标题列！");
		}
		if (linkIndex == -1) {
			updateTextArea(ta, "请确认模板文件中是否包含文章链接列！");
		}

		// 遍历文章链接结果
		for (String key : groupAndArticleList.keySet()) {
			// 链接结果
			List<ArticleLink> articleLinks = groupAndArticleList.get(key);
			// excel组别起始和结束行
			int[] startAndEnd = groupAndPosition.get(key);
			if ((startAndEnd[1] - startAndEnd[0] + 1) < articleLinks.size()) {
				updateTextArea(ta, key + " 组别的草稿箱文章个数与Excel模板文件中的个数不同，请确认！");
			}
			// 写入到excel中
			int start = startAndEnd[0];
			for (ArticleLink aLink : articleLinks) {
				Row row = sheet.getRow(start);
				if (titleIndex != -1) {
					Cell titleCell = row.getCell(titleIndex);
					titleCell.setCellValue(aLink.getTitle());
				}
				if (linkIndex != -1) {
					Cell linkCell = row.getCell(linkIndex);
					linkCell.setCellValue(aLink.getUrl());
				}
				start += 1;
			}
		}
		// 获取token
		Pattern compile = Pattern.compile("token=(\\d+)");
		Matcher matcher = compile.matcher(draftsUrl);
		String token = "";
		while (matcher.find()) {
			token = matcher.group(1);
		}
		// 获取用户管理页面中的分组用户人数
		String userManageUrl = "https://mp.weixin.qq.com/cgi-bin/user_tag?action=get_all_data&lang=zh_CN&token=" + token;
		driver.get(userManageUrl);
		Thread.sleep(5000);
		// 获取excel中的新系统分组人数列
		int newSystemGroupIndex = -1;
		for (int i = 0; i <= titleRow.getLastCellNum(); i++) {
			Cell cell = titleRow.getCell(i);
			if (cell == null) {
				continue;
			}
			if ("新系统分组人数".equals(unit.getCellValue(cell).trim())
					|| unit.getCellValue(cell).trim().contains("分组人数")) {
				newSystemGroupIndex = i;
			}
		}
		// 获取用户分组所有数据
		WebElement groupsList = driver.findElement(By.id("groupsList"));
		List<WebElement> aList = groupsList.findElements(By.tagName("a"));
		for (WebElement we : aList) {
			String group = we.findElement(By.tagName("strong")).getText();
			String num = we.findElement(By.tagName("em")).getText();
			int[] se = groupAndPosition.get(group);
			if (null != se) {
				// 获取起始行
				num = num.substring(1, num.length() - 1);
				updateTextArea(ta, "group: " + group + "; num: " + num);
				int start = se[0];
				Row row = sheet.getRow(start);
				Cell cell = row.getCell(newSystemGroupIndex);
				// 将微信后台的新系统分组人数加到单元格
				cell.setCellValue(Integer.parseInt(num));
			}
		}

		// 将workbook写入文件
		workbook.write(fos);
		updateTextArea(ta, "程序执行完成，结果文件路径为：" + newPath);

		fos.close();
		workbook.close();
		fis.close();
//		driver.close();
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

	/**
	 * 微信文章评论数获取
	 * @param ta 多行文本显示区域
	 * @param getDataFile 数据文件
	 * @param getDetailsTemplate 模板文件
	 * @param summaryDataFile 汇总文件
	 * @param commentPageNum 请求评论页数
	 */
	public void executeButton(TextArea ta, List<Node> getDataFile, List<Node> getDetailsTemplate, List<Node> summaryDataFile, int commentPageNum) {

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

		String url = "https://mp.weixin.qq.com/misc/appmsgcomment?action=get_unread_appmsg_comment&has_comment=0&sort_type=1&sendtype=MASSSEND&lang=zh_CN&f=json&ajax=1&token=";
		HttpClient client = HttpClients.createDefault();
		Map<String, Integer> commentsMap = new HashMap<>();
		Runnable task = new Runnable() {
			@Override
			public void run() {
				// 获取数据文件中的有效条数
				String dataFilePath = ((TextField) getDataFile.get(1)).getText();
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

					// 请求评论数信息，这里的评论总数按数据文件excel中的文章条数计算
					int count = articleNum;
					int divisor = 10;

//					int num = count / divisor;
//					if (count % divisor > 0) {
//						num++;
//					}
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
						// 将行数据放入到map中rigin
						String articleTitle = unit.getCellValue(row.getCell(0));
						int pushPeople = -1;
						try {
							pushPeople = Integer.parseInt(unit.getCellValue(row.getCell(7)));
						} catch (Exception e) {
							updateTextArea(ta, "第" + (i + 1) + "行的送达人数需要改为文本类型");
							return;
						}

						CommentInfo info = CommentInfo.builder()
								.title(articleTitle.trim())
								.pushDate(unit.getCellValue(row.getCell(1)).trim())
								.allReadPeople(Integer.valueOf(unit.getCellValue(row.getCell(2)).trim()))
								.allSharePeople(Integer.valueOf(unit.getCellValue(row.getCell(4)).trim()))
								.pushPeople(pushPeople)
								.completeReadRate(unit.getCellValue(row.getCell(14)).trim())
								.commentNum(commentNum)
								.url(contentUrl)
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
					updateTextArea(ta, "实际共获取到公众号文章 " + urlCount + " 条！");
					updateTextArea(ta, "数据文件的最后插入一列插入完成！");

					dataWb.close();
					// 获取模板文件
					updateTextArea(ta, "开始读取模板文件！");
					String templateFilePath = ((TextField) getDetailsTemplate.get(1)).getText();
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
					System.out.println("map: " + commentInfoMap);
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
						// 险判断推送人数
						int tpush = templateInfo.getPushPeople();
						int key = -1;
						int min = 1000000000;
						for (Integer k : pushPeopleKeys) {
							if (Math.abs(tpush - k) < min) {
								key = k;
								min = Math.abs(tpush - k);
							}
						}

						if (key < 1000000 && templateInfo.isFirst()) {
							cloneCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
							cloneCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
						}

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
						CommentInfo commentInfo = commentInfoMap.get(key).get(tTitle);
						System.out.println(commentInfo);
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
							if (dataRow == null) {
								continue;
							}
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
					}
				} catch (Exception e) {
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

}
