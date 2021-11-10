package org.example;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname App
 * @Description 启动类
 * @Date 2021/8/29 10:02
 * @author mavenr
 */
public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // 使用TabPane，页面切换
        TabPane tp = new TabPane();

        // 页面1、2、3、4、5、6
        Tab tab1 = new Tab(Common.TOP_BUTTON_1);
        Tab tab2 = new Tab(Common.TOP_BUTTON_2);
        Tab tab3 = new Tab(Common.TOP_BUTTON_3);
        Tab tab4 = new Tab(Common.TOP_BUTTON_4);
        Tab tab5 = new Tab(Common.TOP_BUTTON_5);
        Tab tab6 = new Tab(Common.TOP_BUTTON_6);

        // 不可关闭
        tab1.setClosable(false);
        tab2.setClosable(false);
        tab3.setClosable(false);
        tab4.setClosable(false);
        tab5.setClosable(false);
        tab6.setClosable(false);

        tp.getTabs().addAll(tab1, tab2, tab3, tab4, tab5, tab6);

        // 获取屏幕的宽、高
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        // 场景配置
        Scene scene = new Scene(tp);
        primaryStage.setScene(scene);

        primaryStage.setTitle(Common.STAGE_TITLE);
        primaryStage.getIcons().add(new Image("image/folder.png"));
        double w = width / 2 + 200;
        double h = height / 2 + 200;
        primaryStage.setWidth(w);
        primaryStage.setHeight(h);
        primaryStage.setMinWidth(w);
        primaryStage.setMinHeight(h);
        primaryStage.setMaxWidth(width);
        primaryStage.setMaxHeight(height);
        primaryStage.show();

        // 默认选中第一页
        tp.getSelectionModel().select(tab1);

        // 基本的文件名字符替换
        AnchorPane replacePane = new ReplacePane().replacePane(primaryStage, width);
        // 根据步长修改文件名
        FlowPane increaseIdentification =
                new IncreaseIdentificationPane().increaseIdentification(primaryStage, width);
        // 比较两个文件的内容
        AnchorPane ctf = new CompareTwoFiles().compare(primaryStage, width, h);
        // 文本行排序
        AnchorPane stl = new SortTextLine().sort(primaryStage, width);
        // URL编码批量转换
        AnchorPane urlConvert = new UrlConvert().convert(primaryStage, width, h);
        // 公众号文章中音视频下载
        AnchorPane downloadDialog = new DownLoadMediaFromArticle().download(primaryStage, width, h);

        tab1.setContent(replacePane);
        tab2.setContent(increaseIdentification);
        tab3.setContent(ctf);
        tab4.setContent(stl);
        tab5.setContent(urlConvert);
        tab6.setContent(downloadDialog);


    }


}
