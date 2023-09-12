package org.example.interfaces.impl;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.button.BatchButton;
import org.example.interfaces.Function;
import org.example.util.Unit;

import java.util.List;

/**
 * @author mavenr
 * @Classname WechatWebview
 * @Description TODO
 * @Date 2022/10/20 9:07
 */
public class WechatWebview implements Function {

    private Unit unit = new Unit();

    @Override
    public String tabName() {
        return "微信公众号自动抓取url";
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
        // 创建窗口
        AnchorPane ap = new AnchorPane();

        // 竖向排列的布局
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        ap.getChildren().add(vBox);

        // 选择模板文件
        HBox line1 = new HBox();
        line1.setSpacing(10);
        List<Node> getFile = unit.chooseFile(stage, width, "选择模板文件");
        line1.getChildren().addAll(getFile.get(0), getFile.get(1));
        ((Button) getFile.get(0)).setPrefWidth(150);

        // 浏览器
        HBox line2 = new HBox();
        line2.setPrefWidth(stage.getWidth() - 20);
        line2.setPrefHeight(stage.getHeight() - 200);
        WebView webView = new WebView();
        webView.setPrefWidth(stage.getWidth() - 30);
        webView.setPrefHeight(stage.getHeight() - 200);
        WebEngine webEngine = webView.getEngine();
        webEngine.load("https://mp.weixin.qq.com/");
        line2.getChildren().add(webView);

        // 处理按钮
        BatchButton batchButton = new BatchButton();
        Button button = batchButton.createInstance();
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // 获取当前网页的地址
                System.out.println("当前网页的地址：" + webEngine.getLocation());
            }
        });
        // 后退按钮
        BatchButton backButton = new BatchButton();
        Button back = backButton.createInstance();
        back.setText("后退");
        back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                goBack(webEngine);
            }
        });
        HBox line3 = new HBox();
        line3.setSpacing(10);
        line3.setAlignment(Pos.CENTER_LEFT);
        line3.getChildren().addAll(button, back);

        vBox.getChildren().addAll(line1, line2, line3);
        return ap;
    }

    /**
     * 后退
     * @param webEngine
     */
    public void goBack(WebEngine webEngine) {
        final WebHistory history = webEngine.getHistory();
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();

        Platform.runLater(() ->
        {
            // 这里进行边界处理，防止回退到最开始的页面后再回退导致超出边界
            history.go(entryList.size() > 1 && currentIndex > 0 ? -1 : 0);

            // 或者执行 javascript语句
//            webEngine.executeScript("history.back()");
        });
    }

    /**
     * 前进
     * @param webEngine
     */
    public void goForward(WebEngine webEngine) {
        final WebHistory history = webEngine.getHistory();
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();

        Platform.runLater(() ->
        {
            history.go(entryList.size() > 1 && currentIndex < entryList.size() - 1 ? 1 : 0);
            // 或者执行 javascript语句
//            webEngine.executeScript("history.forward()");
        });
    }
}
