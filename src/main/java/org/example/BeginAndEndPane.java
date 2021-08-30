package org.example;

import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * @author mavenr
 * @Classname BeginAndEndPane
 * @Description 在文件名前或后添加字符的布局类配置
 * @Date 2021/8/30 15:52
 */
public class BeginAndEndPane {

    public AnchorPane beginAndEndPane(Stage primaryStage, double width) {
        Button button = new Button("测试第二个布局");
        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(button);

        return root;
    }
}
