package org.example.interfaces.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.TypeMapTitle;
import org.example.util.Unit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author mavenr
 * @Classname CreateArticle
 * @Description TODO
 * @Date 2023/3/10 14:25
 */
public class CreateArticle {

    private static Unit unit = new Unit();

    public static void main(String[] args) {
        // 获取文件工具的路径
        String currentPath = System.getProperty("user.dir");
        String driverPath = currentPath + File.separator + "chromedriver";
        // 判断系统类型
        String osName = System.getProperty("os.name");
        System.out.println(osName);
        if (osName.startsWith("Windows")) {
            driverPath = currentPath + File.separator + "chromedriver.exe";
        }
        System.out.println("driverPath = " + driverPath);

        // 在jvm运行环境中添加驱动配置
        System.setProperty("webdriver.chrome.driver", driverPath);
        System.setProperty("webdriver.http.factory", "jdk-http-client");

        ChromeOptions chromeOptions = new ChromeOptions();

        chromeOptions.setExperimentalOption("debuggerAddress", "127.0.0.1:9527");
        chromeOptions.addArguments("--remote-allow-origins=*");
        // # driver就是当前浏览器窗口
        WebDriver driver = null;
        try {
            driver = new ChromeDriver(chromeOptions);
            List<WebElement> aList = driver.findElements(By.tagName("a"));
            for (WebElement w : aList) {
                if ("展开".equals(w.getText())) {
                    System.out.println("查找到展开");
                    w.click();
                    Thread.sleep(500);
                    break;
                }
            }
            boolean flag = true;
            for (WebElement w : aList) {
                if ("草稿箱".equals(w.getAttribute("title"))) {
                    System.out.println("查找到草稿箱");
                    flag = false;
                    w.click();
                    Thread.sleep(3000);
                    break;
                }
            }

            // 分组 & 分组中的标题
            List<String> groupList = new ArrayList<>();
            groupList.add("2女装");

            String replaceTitle = "普通女孩穿出高级感的搭配思路！！";

            String draftUrl = driver.getCurrentUrl();
            System.out.println("第一页草稿箱地址：" + draftUrl);
            String currentWindowHandle = driver.getWindowHandle();

            // 处理草稿箱元素
            List<WebElement> publishCardContainer = driver.findElements(By.className("publish_card_container"));
            System.out.println(publishCardContainer.size());
            Actions action = new Actions(driver);
            for (WebElement item : publishCardContainer) {

                WebElement element = item.findElement(By.className("weui-desktop-publish__cover__title"));
                String title = element.getText();
                System.out.println("文章头条标题：" + title);
                if (groupList.contains(title)) {
                    // 替换文章
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

        } catch (Exception e) {

        } finally {
//            if (driver != null) {
//                driver.quit();
//            }
        }
    }

    private static Map<String, List<TypeMapTitle>> getMapFromFile() {
        String mapFilePath = "/Users/mawenrui/Downloads/品类和标题对照表.xlsx";
        String filePath = "/Users/mawenrui/Downloads/测试文章.xlsx";
        Workbook mapTitle = null;
        Workbook workbook = null;
        Map<String, List<TypeMapTitle>> result = new HashMap<>();
        try {
            // 获取品类、文章对照表
            mapTitle = new XSSFWorkbook(mapFilePath);
            Sheet mapSheet = mapTitle.getSheetAt(0);
            Map<String, String> typeAndTitle = new HashMap<>();
            for (int i = 1; i <= mapSheet.getLastRowNum(); i++) {
                Row row = mapSheet.getRow(i);
                if (row == null) {
                    break;
                }
                Cell first = row.getCell(0);
                if (first == null) {
                    break;
                }
                Cell second = row.getCell(1);
                String firstValue = unit.getCellValue(first);
                String secondValue = second == null ? "" : unit.getCellValue(second);
                typeAndTitle.put(firstValue, secondValue);
            }

            workbook = new XSSFWorkbook(filePath);
            Sheet sheet = workbook.getSheetAt(0);
            String groupName = "";
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    break;
                }
                Cell first = row.getCell(0);
                Cell second = row.getCell(1);
                if (second == null) {
                    break;
                }
                String firstValue = unit.getCellValue(first);
                String secondValue = unit.getCellValue(second);
                if (StringUtils.isBlank(secondValue)) {
                    break;
                }
                if (StringUtils.isNotBlank(firstValue) && !groupName.equals(firstValue)) {
                    groupName = firstValue;
                }
                System.out.println(groupName + " : " + secondValue + " : " + typeAndTitle.get(secondValue));
                // 开始点击草稿箱中的文章
                TypeMapTitle typeMapTitle = TypeMapTitle.builder()
                        .type(secondValue)
                        .title(typeAndTitle.get(secondValue))
                        .build();
                List<TypeMapTitle> typeMapTitles = result.get(groupName);
                if (CollectionUtils.isEmpty(typeMapTitles)) {
                    List<TypeMapTitle> tt = new ArrayList<>();
                    tt.add(typeMapTitle);
                    result.put(groupName, tt);
                } else {
                    typeMapTitles.add(typeMapTitle);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mapTitle != null) {
                try {
                    mapTitle.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
