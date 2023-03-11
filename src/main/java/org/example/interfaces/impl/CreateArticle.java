package org.example.interfaces.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.ArticleLink;
import org.example.entity.TypeMapTitle;
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

        ChromeOptions chromeOptions = new ChromeOptions();

        chromeOptions.setExperimentalOption("debuggerAddress", "127.0.0.1:9527");
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
            Map<String, List<TypeMapTitle>> mapFromFile = getMapFromFile();

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
                if (mapFromFile.containsKey(title)) {
                    // 鼠标移动到分组上，以显示工具行
                    action.moveToElement(item).perform();
                    Thread.sleep(5000);
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
                    for (String wh : windowHandles) {
                        driver.switchTo().window(wh);
                        if (driver.getTitle().equals(currentWindowHandle) || driver.getTitle().equals("公众号")
                                || driver.getTitle().equals("Official Accounts")) {
                            continue;
                        }
                        // 实际的文章处理逻辑
                        // 查询新建消息的元素
                        List<WebElement> addWordElements = driver.findElements(By.className("preview_media_add_word"));
                        // 鼠标移动
                        action.moveToElement(addWordElements.get(0)).perform();
                        // 点击元素

                        // 关闭当前标签页
                        driver.close();
                    }
                    // 切换回总页
                    driver.switchTo().window(currentWindowHandle);
                }
            }

        } catch (Exception e) {

        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private static Map<String, List<TypeMapTitle>> getMapFromFile() {
        String mapFilePath = "C:\\Users\\mawen\\Desktop\\品类和标题对照表.xlsx";
        String filePath = "C:\\Users\\mawen\\Desktop\\测试文章.xlsx";
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
                String firstValue = first.getStringCellValue();
                String secondValue = second == null ? "" : second.getStringCellValue();
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
                String firstValue = first.getStringCellValue();
                String secondValue = second.getStringCellValue();
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
