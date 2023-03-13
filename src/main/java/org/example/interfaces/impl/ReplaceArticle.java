package org.example.interfaces.impl;

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
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.button.BatchButton;
import org.example.init.Config;
import org.example.interfaces.Function;
import org.example.util.SocketUtil;
import org.example.util.Unit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author mavenr
 * @Classname ReplaceArticle
 * @Description 替换草稿箱中文章的功能
 * @Date 2023/3/13 14:31
 */
public class ReplaceArticle implements Function {

    /**
     * 草稿箱页数
     */
    private static final String DRAFT_PAGE_NUM = "DRAFT_PAGE_NUM";
    /**
     * chrome浏览器启动器的文件路径
     */
    private static final String CHROMESTARTPATH = "CHROMESTARTPATH";


    private Unit unit = new Unit();

    @Override
    public String tabName() {
        return "更新草稿箱文章";
    }

    @Override
    public String tabStyle() {
        String style = "-fx-font-weight: bold; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-focus-color: transparent; -fx-text-base-color: white; " +
                "-fx-background-color: #4695d6;  -fx-pref-height: 30; ";
        return style;
    }

    @Override
    public AnchorPane tabPane(Stage stage, double width, double h) {
        AnchorPane ap = new AnchorPane();

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

        // 获取配置文件中的配置信息
        String draftPageNum = Config.get(DRAFT_PAGE_NUM);
        // 要查询的草稿箱的前几页
        List<Node> nodes1 = unit.newInputText(width, "查询草稿箱的前几页：", 150);
        // 文本框中提示信息
        TextField draftPageNumField = ((TextField) nodes1.get(1));
        draftPageNumField.setPromptText("如果不填，则默认查询前2页");
        if (StringUtils.isNotBlank(draftPageNum)) {
            draftPageNumField.setText(draftPageNum);
        }
        // 失去焦点触发保存事件
        unit.loseFocuseSave(draftPageNumField, draftPageNum, DRAFT_PAGE_NUM);
        HBox line1 = new HBox();
        line1.setAlignment(Pos.CENTER_LEFT);
        line1.setSpacing(10);
        line1.getChildren().addAll(nodes1);

        // 需要替换的分组
        List<Node> nodes2 = unit.newInputText(width, "需要替换的分组名称：", 150);
        // 文本框中提示信息
        ((TextField) nodes2.get(1)).setPromptText("多个分组名称以英文“;”分隔");
        HBox line2 = new HBox();
        line2.setAlignment(Pos.CENTER_LEFT);
        line2.setSpacing(10);
        line2.getChildren().addAll(nodes2);

        // 需要替换的文章名称
        List<Node> nodes3 = unit.newInputText(width, "需要替换的文章名称：", 150);
        // 文本框中提示信息
        ((TextField) nodes3.get(1)).setPromptText("多个文章名称以英文“;”分隔");
        HBox line3 = new HBox();
        line3.setAlignment(Pos.CENTER_LEFT);
        line3.setSpacing(10);
        line3.getChildren().addAll(nodes3);

        // 处理按钮
        BatchButton batchButtonBuilder = new BatchButton();
        Button batchButton = batchButtonBuilder.createInstance();
        batchButton.setText("更新");
        HBox buttonLine = new HBox();
        buttonLine.setAlignment(Pos.CENTER_LEFT);
        buttonLine.getChildren().add(batchButton);

        // 多行文本框，打印信息
        TextArea area = new TextArea();
        area.setPrefHeight(600);

        // 添加按钮处理事件
        startHandler(startButton, chromePathTf, area);
        try {
            buttonHandler(batchButton, (TextField) nodes1.get(1), (TextField) nodes2.get(1), (TextField) nodes3.get(1), area);
        } catch (Exception e) {
            unit.updateTextArea(area, "更新异常：" + e.getMessage());
        }

        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);
        root.getChildren().addAll(line1Pre, line1, line2, line3, buttonLine, area);
        ap.getChildren().add(root);
        return ap;
    }

    /**
     * 按钮点击后的实际处理事件
     * @param batchButton 按钮
     * @param draftPageNumTf 草稿箱的前几页
     * @param groupNameTf 需要替换的分组名称
     * @param articleNameTf 需要替换的文章的名称
     * @param area 多行文本区域
     */
    public void buttonHandler(Button batchButton, TextField draftPageNumTf,
                              TextField groupNameTf, TextField articleNameTf, TextArea area) throws Exception{
        batchButton.setOnAction(new EventHandler<ActionEvent>() {
            @SneakyThrows
            @Override
            public void handle(ActionEvent event){
                area.setText("");
                // 判断分组名称和文章名称
                String groupNameList = groupNameTf.getText().trim();
                String articleNameList = articleNameTf.getText().trim();
                if (StringUtils.isBlank(groupNameList) || StringUtils.isBlank(articleNameList)) {
                    area.setText("分组名称或文章名称不可为空");
                    return;
                }
                if (groupNameList.contains("；")) {
                    area.setText("分组名称或文章名称中包含中文格式的“；”，请处理");
                    return;
                }


                // 判断草稿箱前几页，默认为前2页
                String pageText = draftPageNumTf.getText().trim();
                int pageNum = 2;
                if (StringUtils.isNotBlank(pageText)) {
                    try {
                        pageNum = Integer.parseInt(pageText);
                    } catch (Exception e) {
                        area.setText("请正确填写草稿箱的前几页，只能为空或数字");
                        return;
                    }
                }

                // 获取草稿箱链接
                // 获取文件工具的路径
                String currentPath = System.getProperty("user.dir");
                String driverPath = currentPath + File.separator + "chromedriver";
                // 判断系统类型
                String osName = System.getProperty("os.name");
                unit.updateTextArea(area, "系统类型：" + osName);
                if (osName.startsWith("Windows")) {
                    driverPath = currentPath + File.separator + "chromedriver.exe";
                }
                unit.updateTextArea(area, "驱动器路径：" + driverPath);
                // 在jvm运行环境中添加驱动配置
                System.setProperty("webdriver.chrome.driver", driverPath);

                ChromeOptions chromeOptions = new ChromeOptions();

                chromeOptions.setExperimentalOption("debuggerAddress", "127.0.0.1:9527");
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
                    unit.updateTextArea(area, sb.toString());
                    return;
                }
                unit.updateTextArea(area, "创建驱动");

                // 获取草稿箱链接
                String draftUrl = null;
                try {
                    draftUrl = getDraftHomeUrl(driver, area);
                } catch (Exception e) {
                    e.printStackTrace();
                    unit.updateTextArea(area, "获取草稿箱链接失败");
                    unit.updateTextArea(area, e.getMessage());
                    return;
                }
                if (StringUtils.isBlank(draftUrl)) {
                    unit.updateTextArea(area, "未获取到草稿箱链接");
                    return;
                }

                // 分组
                String[] groupNameArray = groupNameList.split(";");
                // 文章
                String[] articleNameArray = articleNameList.split(";");

                // 三层循环，草稿箱分页——分组——文章
                for (int i = 0; i < pageNum; i++) {
                    if (i != 0) {
                        String oldStr = "begin=0";
                        if (draftUrl.contains("begin=10")) {
                            oldStr = "begin=10";
                        } else if (draftUrl.contains("begin=20")) {
                            oldStr = "begin=20";
                        } else if (draftUrl.contains("begin=30")) {
                            oldStr = "begin=30";
                        }
                        draftUrl = draftUrl.replace(oldStr, "begin=" + (i * 10));
                    }

                    // 如果草稿箱目标页的url与当前页的url不同，则点击
                    if (!driver.getCurrentUrl().equals(draftUrl)) {
                        driver.get(draftUrl);
                    }
                    unit.updateTextArea(area, "请求草稿箱第 " + (i + 1) + " 页");

                    // 记录草稿箱页面当前的窗口信息
                    String currentWindowHandle = driver.getWindowHandle();
                    // 开始循环匹配页面中的分组
                    for (String groupName : groupNameArray) {
                        // 处理草稿箱元素
                        List<WebElement> publishCardContainer = driver.findElements(By.className("publish_card_container"));
                        System.out.println(publishCardContainer.size());
                        Actions action = new Actions(driver);
                        for (WebElement item : publishCardContainer) {
                            WebElement element = item.findElement(By.className("weui-desktop-publish__cover__title"));
                            String title = element.getText();
                            System.out.println("文章头条标题：" + title);
                            if (groupName.equals(title)) {
                                // 鼠标移动到分组上，以显示工具行
                                action.moveToElement(item).perform();
                                Thread.sleep(3000);
                                // 获取编辑的元素
                                List<WebElement> actionList = item.findElements(By.className("weui-desktop-card__action"));
                                System.out.println("工具行个数：" + actionList.size());
                                List<WebElement> editList = actionList.get(0).findElements(By.cssSelector("[class='weui-desktop-tooltip__wrp weui-desktop-link']"));
                                System.out.println("编辑个数：" + editList.size());
                                // 鼠标移动到编辑符号上，以可点击元素
                                action.moveToElement(editList.get(0)).perform();
                                editList.get(0).click();

                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Set<String> windowHandles = driver.getWindowHandles();
                                System.out.println("current = " + currentWindowHandle);
                                windowHandles.forEach(System.out::println);
                                for (String wh : windowHandles) {
                                    // 如果位草稿箱的首页，则跳过
                                    if (wh.equals(currentWindowHandle)) {
                                        continue;
                                    }
                                    // 切换到新打开的标签页
                                    driver.switchTo().window(wh);
                                    System.out.println("开始实际处理文章");

                                    // 获取原来的图文
                                    List<WebElement> appmsgItemList = driver.findElements(By.cssSelector(".card_appmsg_title.js_appmsg_title"));
                                    int x = -1;
                                    for (int i = 0; i < appmsgItemList.size(); i++) {
                                        WebElement webElement = appmsgItemList.get(i);
                                        System.out.println("原次条名称：" + webElement.getText());
                                        if (replaceTitle.equals(webElement.getText())) {
                                            System.out.println("找到原次条元素，点击");
                                            webElement.click();
                                            x = i;
                                            System.out.println("文章下标：" + i);
                                            break;
                                        }
                                    }
                                    if (x != -1) {
                                        // 获取删除元素
                                        List<WebElement> deleteButtonList = driver.findElements(By.cssSelector(".weui-desktop-icon-btn.weui-desktop-icon-btn__delete_icon"));
                                        System.out.println("删除元素个数：" + deleteButtonList.size());
                                        System.out.println("点击删除");
                                        deleteButtonList.get(0).click();
                                        // 查询  popover_inner
                                        List<WebElement> popoverInner = driver.findElements(By.className("popover_inner"));
                                        // 获取包含"是否确定删除此篇内容"的元素
                                        for (WebElement w : popoverInner) {
                                            if (w.getText().contains("是否确定删除此篇内容")) {
                                                System.out.println("查询到包含是否确定删除此篇内容的元素");
                                                // 查询确定按钮
                                                List<WebElement> elements = w.findElements(By.cssSelector(".btn.btn_primary.jsPopoverBt"));
                                                System.out.println("获取到确认元素个数：" + elements.size());
                                                System.out.println("点击确认");
                                                elements.get(0).click();
                                            }
                                        }
                                    }

                                    // 实际的文章处理逻辑
                                    // 查询新建消息的元素
                                    List<WebElement> addWordElements = driver.findElements(By.className("preview_media_add_word"));
                                    System.out.println("新建消息元素个数：" + addWordElements.size());
                                    // 鼠标移动
                                    action.moveToElement(addWordElements.get(0)).perform();
                                    // 点击元素
                                    List<WebElement> elements = driver.findElements(By.className("icon-svg-editor-insert-appmsg"));
                                    System.out.println("选择已有图文元素个数：" + elements.size());
                                    elements.get(0).click();
                                    // 获取左侧目录列表
                                    List<WebElement> leftTab = driver.findElements(By.className("left_tab_data"));
                                    System.out.println("左侧目录列个数：" + leftTab.size());
                                    List<WebElement> tabList = leftTab.get(0).findElements(By.className("tab"));
                                    System.out.println("目录列中目录个数：" + tabList.size());
                                    for (WebElement it : tabList) {
                                        String menuName = it.getText();
                                        System.out.println("目录名称：" + menuName);
                                        if ("草稿".equals(menuName)) {
                                            System.out.println("查找到草稿目录，点击");
                                            it.click();
                                            Thread.sleep(3000);
                                            break;
                                        }
                                    }

                                    // 获取所有分组
                                    List<WebElement> draftContainerList = driver.findElements(By.className("publish_card_container"));
                                    // 遍历不包含次条的分组
                                    for (WebElement w : draftContainerList) {
                                        List<WebElement> elements1 = w.findElements(By.className("weui-desktop-publish__title"));
                                        if (elements1.size() > 0) {
                                            continue;
                                        }
                                        List<WebElement> publishList = w.findElements(By.className("weui-desktop-publish__cover__title"));
                                        System.out.println("获取到主条个数：" + publishList.size());
                                        if (replaceTitle.equals(publishList.get(0).getText())) {
                                            System.out.println("查询到匹配【" + replaceTitle + "】的主条");
                                            System.out.println("点击主条");
                                            publishList.get(0).click();
                                            break;
                                        }
                                    }

                                    List<WebElement> bottomLineList = driver.findElements(By.className("weui-desktop-dialog__ft"));
                                    System.out.println("底部行个数：" + bottomLineList.size());
                                    for (int i = 0; i < bottomLineList.size(); i++) {
                                        WebElement bl = bottomLineList.get(i);
                                        System.out.println("循环第" + i + "行");
                                        List<WebElement> buttonList = bl.findElements(By.tagName("button"));
                                        System.out.println("底部行中的按钮个数：" + buttonList.size());
                                        if (CollectionUtils.isNotEmpty(buttonList)) {
                                            WebElement buttonElement = buttonList.get(0);
                                            if ("确定".equals(buttonElement.getText())) {
                                                System.out.println("查询到确定按钮，点击");
                                                buttonElement.click();
                                                break;
                                            }
                                        }
                                    }

                                    // 等待新导入的文章加载完成
                                    Thread.sleep(2000);

                                    // 调整位置
                                    List<WebElement> appmsgItemListName = driver.findElements(By.cssSelector(".card_appmsg_title.js_appmsg_title"));
                                    int newX = -1;

                                    WebElement newElement = null;
                                    for (int i = 0; i < appmsgItemListName.size(); i++) {
                                        WebElement webElement = appmsgItemListName.get(i);
                                        System.out.println("次条名称：" + webElement.getText());
                                        if (replaceTitle.equals(webElement.getText())) {
//                                System.out.println("找到次条元素，点击");
                                            newElement = webElement;
//                                webElement.click();
                                            newX = i;
                                            System.out.println("文章下标：" + i);
                                            break;
                                        }
                                    }
                                    if (newX != -1 && newX > x) {
                                        for (int i = newX; i > x; i--) {
                                            System.out.println("找到次条元素，点击");
                                            newElement.click();
                                            // 获取上移元素
                                            List<WebElement> upButtonList = driver.findElements(By.cssSelector(".weui-desktop-icon-btn.weui-desktop-icon-btn__up_icon"));
                                            System.out.println("上移元素个数：" + upButtonList.size());
                                            System.out.println("点击上移");
                                            upButtonList.get(0).click();
                                        }
                                    }

                                    // 获取保存为草稿按钮
                                    List<WebElement> jsSubmit = driver.findElements(By.id("js_submit"));
                                    System.out.println("获取到保存草稿按钮个数：" + jsSubmit.size());
                                    System.out.println("点击保存草稿按钮");
                                    jsSubmit.get(0).click();

                                    // 等待新导入的文章加载完成
                                    Thread.sleep(2000);

                                    // 关闭当前标签页
                                    driver.close();
                                }
                                // 切换回总页
//                    driver.switchTo().window(currentWindowHandle);
                            }
                        }

                    }
                }
            }
        });
    }


    /**
     * 启动浏览器的事件
     * @param startButton 启动按钮
     * @param chromePathTf 浏览器启动器的文件路径
     * @param ta
     */
    public void startHandler(Button startButton, TextField chromePathTf, TextArea ta) {
        // 启动测试浏览器按钮事件
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // 判断chrome启动器类的路径
                String chromePath = chromePathTf.getText();
                if (StringUtils.isBlank(chromePath)) {
                    ta.setText("请输入chrome浏览器的启动器类的文件路径！");
                    return;
                }
                // 获取文件工具的路径
                String currentPath = System.getProperty("user.dir");
                // chrome测试数据存放路径
                String chromeTestPath= currentPath + File.separator + "chromeTest";
                // 启动chrome调试
                unit.updateTextArea(ta, "chromePath = " + chromePath);
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
    }

    /**
     * 获取草稿箱首页链接
     * @param driver
     * @param area
     * @return
     * @throws Exception
     */
    public String getDraftHomeUrl(WebDriver driver, TextArea area) throws Exception{
        List<WebElement> aList = driver.findElements(By.tagName("a"));
        for (WebElement w : aList) {
            if ("展开".equals(w.getText())) {
                unit.updateTextArea(area, "点击展开目录");
                w.click();
                Thread.sleep(500);
                break;
            }
        }
        boolean flag = true;
        for (WebElement w : aList) {
            if ("草稿箱".equals(w.getAttribute("title"))) {
                flag = false;
                unit.updateTextArea(area, "获取到草稿箱链接：" + w.getAttribute("href"));
                w.click();
                unit.updateTextArea(area, "跳转到草稿箱，等待5s");
                Thread.sleep(5000);
                break;
            }
        }

        if (flag) {
            return null;
        } else {
            return driver.getCurrentUrl();
        }
    }
}
