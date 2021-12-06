package org.example;


import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;


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
        tp.setStyle("-fx-font-size: 14;");
        tp.setTabMinHeight(50);

        // 页面1、2、3、4、5、6
        Tab tab1 = new Tab(Common.TOP_BUTTON_1);
        Tab tab2 = new Tab(Common.TOP_BUTTON_2);
        Tab tab3 = new Tab(Common.TOP_BUTTON_3);
        Tab tab4 = new Tab(Common.TOP_BUTTON_4);
        Tab tab5 = new Tab(Common.TOP_BUTTON_5);
        Tab tab6 = new Tab(Common.TOP_BUTTON_6);
        Tab tab7 = new Tab("数据库");
        // 设置切换tab的样式
        String style = "-fx-font-weight: bold; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-focus-color: transparent; -fx-text-base-color: white; ";

        tab1.setStyle(style + "-fx-background-color: CornflowerBlue; -fx-pref-height: 30; ");
        tab2.setStyle(style + "-fx-background-color: Orange;  -fx-pref-height: 30; ");
        tab3.setStyle(style + "-fx-background-color: LightSeaGreen;  -fx-pref-height: 30; ");
        tab4.setStyle(style + "-fx-background-color: SandyBrown;  -fx-pref-height: 30; ");
        tab5.setStyle(style + "-fx-background-color: Pink;  -fx-pref-height: 40; ");
        tab6.setStyle(style + "-fx-background-color: MediumPurple;  -fx-pref-height: 30; ");
        tab7.setStyle(style + "-fx-background-color: MediumPurple;  -fx-pref-height: 30; ");

        // 不可关闭
        tab1.setClosable(false);
        tab2.setClosable(false);
        tab3.setClosable(false);
        tab4.setClosable(false);
        tab5.setClosable(false);
        tab6.setClosable(false);
        tab7.setClosable(false);

        // 将tab页面添加到tabpane面板
        tp.getTabs().addAll(tab1, tab2, tab3, tab4, tab5, tab6, tab7);

        // 设置切换tab时高度的设置
        tp.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                // 改变标签的状态
                String newStyle = newValue.getStyle();
                newValue.setStyle(newStyle.replace(" -fx-pref-height: 30; ", " -fx-pref-height: 40; "));

                String oldStyle = oldValue.getStyle();
                oldValue.setStyle(oldStyle.replace(" -fx-pref-height: 40; ", " -fx-pref-height: 30; "));
            }
        });

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
        double h = height / 2 + 300;
        primaryStage.setWidth(w);
        primaryStage.setHeight(h);
        primaryStage.setMinWidth(w);
        primaryStage.setMinHeight(h);
        primaryStage.setMaxWidth(width);
        primaryStage.setMaxHeight(height);
        primaryStage.show();

        // 默认选中url编码批量转换页
        tp.getSelectionModel().select(tab5);

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
        tab7.setContent(new Database().handle(primaryStage, width, h));
    }


}
