package org.example.interfaces.impl;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.button.BatchButton;
import org.example.common.ExcelContent;
import org.example.entity.ArticleLink;
import org.example.init.Config;
import org.example.interfaces.Function;
import org.example.util.SocketUtil;
import org.example.util.Unit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Classname ArticleLinkInDrafts
 * @Description 草稿箱中的文章链接
 * @Date 2022/12/3 15:58
 * @author mavenr
 */
public class ArticleLinkInDrafts implements Function {

	private Unit unit = new Unit();

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");

	/**
	 * chrome浏览器启动器的文件路径
	 */
	private static final String CHROMESTARTPATH = "CHROMESTARTPATH";

	/**
	 * 公众号草稿箱网页地址
	 */
	private static final String DRAFTSURL = "DRAFTSURL";

	@Override
	public String tabName() {
		return "草稿箱链接";
	}

	@Override
	public String tabStyle() {
		String style = "-fx-font-weight: bold; " +
				"-fx-background-radius: 10 10 0 0; " +
				"-fx-focus-color: transparent; -fx-text-base-color: white; " +
				"-fx-background-color: #fa6e57;  -fx-pref-height: 30; ";
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

		// 第一行，获取模板文件
		HBox line1 = new HBox();
		line1.setAlignment(Pos.CENTER_LEFT);
		line1.setSpacing(10);
		List<Node> template = unit.chooseFile(stage, width, "模板文件");
		for (Node n : template) {
			line1.getChildren().add(n);
		}

		// 第二行，请输入草稿箱页面的网页地址
		HBox line2 = new HBox();
		line2.setAlignment(Pos.CENTER_LEFT);
		line2.setSpacing(10);
		List<Node> drafts = unit.newInputText(width, "请输入草稿箱页面的网页地址：", 200);
		for (Node n : drafts) {
			line2.getChildren().add(n);
		}

		TextField draftsPathTf = (TextField) drafts.get(1);
		// 设置样式为下划线
		String draftsUrl = Config.get(DRAFTSURL);
		// 加载配置文件中的参数
		if (StringUtils.isNotEmpty(draftsUrl)) {
			draftsPathTf.setText(draftsUrl);
		}
		String draftsText = draftsPathTf.getText();
		// 失去焦点触发保存事件
		draftsPathTf.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// 判断内容改变，则保存内容
				if (!draftsText.equals(draftsPathTf.getText())) {
					// 设置配置文件
					Config.set(DRAFTSURL, draftsPathTf.getText());
				}
			}
		});

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
		ta.setPrefWidth(width / 2 - 90);
		ta.setPrefHeight(stage.getHeight() - 400);
		ta.setEditable(false);
		line4.getChildren().add(ta);

		vBox.getChildren().addAll(line1Pre, tips2, tips, line1, line2, line3, line4);

		// 启动测试浏览器按钮事件
		startButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// 判断chrome启动器类的路径
				String chromePath = ((TextField) chrome.get(1)).getText();
				if (StringUtils.isBlank(chromePath)) {
					ta.setText("请输入chrome浏览器的启动器类的文件路径！");
					return;
				}
				// 获取文件工具的路径
				String currentPath = System.getProperty("user.dir");
				// chrome测试数据存放路径
				String chromeTestPath= currentPath + File.separator + "chromeTest";
				// 启动chrome调试
				updateTextArea(ta, "chromePath = " + chromePath);
				// 查看端口是否被占用，如果被占用则先停掉端口再启动 区分 windows 和 mac ，命令行也是
				boolean alive = SocketUtil.isAlive("127.0.0.1", 9527);
				if (alive) {
					ta.setText("测试浏览器已启动，请在任务栏中点击浏览器图标创建新窗口！");
					return;
				}
				String[] cmd = new String[3];
				cmd[0] = chromePath;
				cmd[1] = "--remote-debugging-port=9527";
				cmd[2] = "--user-data-dir=" + chromeTestPath;
				try {
					Runtime.getRuntime().exec(cmd);
					ta.setText("chrome浏览器远程调试模式启动成功！");
				} catch (IOException e) {
					System.out.println(e.getMessage());
					ta.setText("chrome浏览器远程调试模式启动失败，请联系技术人员！");
				}
			}
		});

		// 添加处理事件
		batchButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						// 查看端口是否被占用，如果被占用则先停掉端口再启动 区分 windows 和 mac ，命令行也是
						boolean alive = SocketUtil.isAlive("127.0.0.1", 9527);
						if (!alive) {
							ta.setText("测试浏览器未启动，请点击上面的\"启动测试浏览器\"按钮，启动测试浏览器！");
							return;
						}

						ta.setText("");
						long start = System.currentTimeMillis();
						// 获取模板文件路径
						String templatePath = ((TextField) template.get(1)).getText();
						if (StringUtils.isBlank(templatePath)) {
							ta.setText("请选择模板文件！");
							return;
						}
						// 获取草稿箱网页地址
						String draftsUrl = ((TextField) drafts.get(1)).getText();
						if (StringUtils.isBlank(draftsUrl)) {
							ta.setText("请输入草稿箱页面的网页地址！");
							return;
						}

						try {
							debugChrome(templatePath, draftsUrl, ta);
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
		WebDriver driver = new ChromeDriver(chromeOptions);

		updateTextArea(ta, "创建驱动");
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
		driver.get(draftsUrl);
		updateTextArea(ta, "请求草稿箱：" + draftsUrl);

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
				System.out.println("========" + key + "========");
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
						System.out.println(title + " : " + articleUrl);
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
			return;
		}
		if (linkIndex == -1) {
			updateTextArea(ta, "请确认模板文件中是否包含文章链接列！");
			return;
		}

		// 遍历文章链接结果
		for (String key : groupAndArticleList.keySet()) {
			// 链接结果
			List<ArticleLink> articleLinks = groupAndArticleList.get(key);
			// excel组别起始和结束行
			int[] startAndEnd = groupAndPosition.get(key);
			if ((startAndEnd[1] - startAndEnd[0] + 1) < articleLinks.size()) {
				updateTextArea(ta, key + " 组别的草稿箱文章个数与Excel模板文件中的个数不同，跳过！");
				continue;
			}
			// 写入到excel中
			int start = startAndEnd[0];
			for (ArticleLink aLink : articleLinks) {
				Row row = sheet.getRow(start);
				Cell titleCell = row.getCell(titleIndex);
				titleCell.setCellValue(aLink.getTitle());
				Cell linkCell = row.getCell(linkIndex);
				linkCell.setCellValue(aLink.getUrl());
				start += 1;
			}
		}
//		// 获取token
//		Pattern compile = Pattern.compile("token=(\\d+)");
//		Matcher matcher = compile.matcher(draftsUrl);
//		String token = "";
//		while (matcher.find()) {
//			token = matcher.group(1);
//		}
//		// 获取用户管理页面中的分组用户人数
//		String userManageUrl = "https://mp.weixin.qq.com/cgi-bin/user_tag?action=get_all_data&lang=zh_CN&token=" + token;
//		driver.get(userManageUrl);
//		Thread.sleep(5000);
//		// 获取excel中的新系统分组人数列
//		int newSystemGroupIndex = -1;
//		for (int i = 0; i <= titleRow.getLastCellNum(); i++) {
//			Cell cell = titleRow.getCell(i);
//			if (cell == null) {
//				continue;
//			}
//			if ("新系统分组人数".equals(cell.getStringCellValue().trim())
//					|| cell.getStringCellValue().trim().contains("分组人数")) {
//				newSystemGroupIndex = i;
//			}
//		}
//		// 获取用户分组所有数据
//		WebElement groupsList = driver.findElement(By.id("groupsList"));
//		List<WebElement> aList = groupsList.findElements(By.tagName("a"));
//		for (WebElement we : aList) {
//			String group = we.findElement(By.tagName("strong")).getText();
//			String num = we.findElement(By.tagName("em")).getText();
//			int[] se = groupAndPosition.get(group);
//			if (null != se) {
//				// 获取起始行
//				num = num.substring(1, num.length() - 1);
//				updateTextArea(ta, "group: " + group + "; num: " + num);
//				int start = se[0];
//				Row row = sheet.getRow(start);
//				Cell cell = row.getCell(newSystemGroupIndex);
//
//				if (cell.getCellType().equals(CellType.BLANK) || StringUtils.isBlank(cell.getStringCellValue())) {
//					cell.setCellValue(Integer.parseInt(num));
//				}
//			}
//		}


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


}
