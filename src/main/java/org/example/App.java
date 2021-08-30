package org.example;


import com.mavenr.file.ReName;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;

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

        // 设置顶部区域
        Button topB1 = new Button("替换文件名");
        Button topB2 = new Button("文件名前添加字符");

        topB1.setPrefHeight(30);
        topB2.setPrefHeight(30);

        HBox hb = new HBox();
        hb.setPrefHeight(50);
        hb.setStyle("-fx-background-color: LightGrey");
        hb.getChildren().addAll(topB1, topB2);
        hb.setSpacing(20);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setPadding(new Insets(10));

        BorderPane bor = new BorderPane();
        bor.setTop(hb);


        // 获取屏幕的宽、高
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        // 场景配置
        Scene scene = new Scene(bor);
        primaryStage.setScene(scene);

        primaryStage.setTitle("文件批处理工具");
        primaryStage.getIcons().add(new Image("image/folder.png"));
        primaryStage.setWidth(width / 2);
        primaryStage.setHeight(height / 2);
        primaryStage.setMinWidth(width / 2);
        primaryStage.setMinHeight(height / 2);
        primaryStage.setMaxWidth(width);
        primaryStage.setMaxHeight(height);
        primaryStage.show();

        AnchorPane replacePane = new ReplacePane().replacePane(primaryStage, width);
        AnchorPane beginAndEndPane = new BeginAndEndPane().beginAndEndPane(primaryStage, width);
        bor.setCenter(replacePane);
        // 点击按钮触发事件，切换中心区域
        topB1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!replacePane.equals(bor.getCenter())) {
                    System.out.println("切换到字符替换的布局");
                    bor.setCenter(replacePane);
                }
            }
        });
        topB2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!beginAndEndPane.equals(bor.getCenter())) {
                    System.out.println("切换到文件名前后添加字符的布局");
                    bor.setCenter(beginAndEndPane);
                }
            }
        });


    }




}
