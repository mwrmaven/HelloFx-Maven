package org.example;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

        // 设置顶部区域
        Button topB1 = new Button(Common.TOP_BUTTON_1);
        Button topB2 = new Button(Common.TOP_BUTTON_2);
        Button topB3 = new Button(Common.TOP_BUTTON_3);
        // 设置按钮属性
        topB1.setPrefHeight(30);
        topB2.setPrefHeight(30);
        topB3.setPrefHeight(30);
        // 横向布局的创建以及配置
        HBox hb = new HBox();
        hb.setPrefHeight(50);
        hb.setStyle("-fx-background-color: LightGrey");
        hb.setSpacing(20);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setPadding(new Insets(10));
        // 将按钮组件添加到横向布局中
        hb.getChildren().addAll(topB1, topB2, topB3);

        // 方位布局，并设置方位布局的顶部和中心区域
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

        // 基本的文件名字符替换
        AnchorPane replacePane = new ReplacePane().replacePane(primaryStage, width);
        // 根据步长修改文件名
        FlowPane increaseIdentification =
                new IncreaseIdentificationPane().increaseIdentification(primaryStage, width);
        // 比较两个文件的内容
        AnchorPane ctf = new CompareTwoFiles().compare(primaryStage, width, h);

        bor.setCenter(ctf);
        Map<Button, Pane> buttonAndPane = new HashMap<>();
        buttonAndPane.put(topB1, replacePane);
        buttonAndPane.put(topB2, increaseIdentification);
        buttonAndPane.put(topB3, ctf);

        // 配置方位布局类的中心位置触发事件
        setBorderPaneCenter(buttonAndPane, bor);
    }

    private void setBorderPaneCenter(Map<Button, Pane> buttonAndPane, BorderPane bor) {
        // 遍历配置点击按钮触发对应的事件，切换中心区域
        buttonAndPane.forEach((k, v) -> {
            k.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (!v.equals(bor.getCenter())) {
                        System.out.println("切换到: " + k.getText());
                        bor.setCenter(v);
                    }
                }
            });
        });
    }




}
