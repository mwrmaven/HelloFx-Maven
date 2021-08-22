package org.example;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


/**
 * @Author mavenr
 * @Classname  AlertWindow
 * @Description 弹出框类
 * @Date 2021/8/22 11:54 下午
 */
public class AlertWindow {

    public static void alter(String msg) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        Label label = new Label();
        label.setText(msg);

        VBox vBox = new VBox();
        vBox.getChildren().add(label);
        // 设置居中
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox, 200, 200);
        stage.setScene(scene);
        stage.setTitle("提示信息");
        stage.show();

    }
}
