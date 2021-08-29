package org.example;


import com.mavenr.file.ReName;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
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

        // 获取屏幕的宽、高
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        // 选择文件夹/文件路径，并输入到文本框
        TextField text = new TextField();
        // 输入框中禁止编辑
        text.setDisable(true);
        text.setPrefWidth(width / 2 - 200);

        // 点击按钮，选择文件夹
        Button buttonFileChoose = new Button("选择文件夹路径");
        buttonFileChoose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser dc = new DirectoryChooser();
                File file = dc.showDialog(primaryStage);
                String path = file.getPath();
                text.setText(path);
            }
        });

        // 横向布局
        HBox hBox = new HBox();
        hBox.getChildren().addAll(buttonFileChoose, text);
        hBox.setSpacing(20);


        Label label1 = new Label();
        label1.setText("旧字符");
        TextField oldFiled = new TextField();

        Label label2 = new Label();
        label2.setText("新字符");
        label2.setStyle("");
        TextField newFiled = new TextField();

        HBox hBox1 = new HBox();
        hBox1.setLayoutX(0);
        hBox1.setLayoutY(30);
        hBox1.getChildren().addAll(label1, oldFiled, label2, newFiled);
        hBox1.setSpacing(10);
        hBox1.setAlignment(Pos.CENTER_LEFT);

        Button edit = new Button("批量修改");
        TextArea ta = new TextArea();
        ta.setEditable(false);
        VBox hBox2 = new VBox();
        hBox2.setLayoutX(0);
        hBox2.setLayoutY(60);
        hBox2.getChildren().addAll(edit, ta);
        hBox2.setSpacing(10);

        edit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (text.getText() != "" && oldFiled.getText() != "" && newFiled.getText() != "") {
                    ReName.replaceFileName(text.getText(), oldFiled.getText(), newFiled.getText());
                    ta.setText("批量修改完成!");
                }
            }
        });


        // 布局类
        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(hBox, hBox1, hBox2);

        // 场景配置
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);


        primaryStage.setTitle("文件批处理工具");
        primaryStage.setWidth(width / 2);
        primaryStage.setHeight(height / 2);
        primaryStage.setMinWidth(width / 2);
        primaryStage.setMinHeight(height / 2);
        primaryStage.setMaxWidth(width);
        primaryStage.setMaxHeight(height);
        primaryStage.show();


    }
}
