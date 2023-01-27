package org.example;

import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.example.util.SocketUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		// 在jvm运行环境中添加驱动配置
		String driverPath = "/Users/mawenrui/IdeaProjects/HelloFx-Maven/chromedriver";
		System.setProperty("webdriver.chrome.driver", driverPath);

		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.setExperimentalOption("debuggerAddress", "127.0.0.1:9527");
		// # driver就是当前浏览器窗口
		WebDriver driver = new ChromeDriver(chromeOptions);
		String url = "https://mp.weixin.qq.com/cgi-bin/appmsg?begin=";
		String token = "383694980";

		for (int i = 0; i < 20; i+=10) {
			driver.get(url+ i + "&count=10&isFromOldMsg=&type=77&action=list&token=" + token + "&lang=zh_CN");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			List<WebElement> elements = driver.findElements(By.className("weui-desktop-card__bd"));
			for (WebElement webElement : elements) {
				List<WebElement> as = webElement.findElements(By.tagName("a"));
				as.forEach(item -> {
					System.out.println(item.getText());
				});
				System.out.println("-------------------------");
			}

		}
	}
}
