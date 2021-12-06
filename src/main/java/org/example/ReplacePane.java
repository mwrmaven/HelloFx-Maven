package org.example;

import com.mavenr.file.ReName;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.button.BatchButton;

import java.util.List;

/**
 * @author mavenr
 * @Classname ReplacePane
 * @Description 修改文件名的布局配置
 * @Date 2021/8/30 15:50
 */
public class ReplacePane {

    private Unit unit = new Unit();

    public AnchorPane replacePane(Stage primaryStage, double width) {
        // 创建选择文件夹的组件
        List<Node> nodes = unit.chooseFolder(primaryStage, width, null);

        // 横向布局，将选择文件夹的组件添加到横向布局中
        HBox hBox = new HBox();
        for (Node node : nodes) {
            hBox.getChildren().add(node);
        }
        // 设置横向布局中组件的间隔，以及padding
        hBox.setSpacing(20);

        Label label1 = new Label();
        label1.setText("旧字符");
        TextField oldFiled = new TextField();

        Label label2 = new Label();
        label2.setText("新字符");
        label2.setStyle("");
        TextField newFiled = new TextField();

        HBox hBox1 = new HBox();
        hBox1.setLayoutX(10);
        hBox1.setLayoutY(30);
        hBox1.getChildren().addAll(label1, oldFiled, label2, newFiled);
        hBox1.setSpacing(10);
        hBox1.setAlignment(Pos.CENTER_LEFT);

        BatchButton batchButton = new BatchButton();
        batchButton.setText("批量修改");
        Button edit = batchButton.createInstance();
        TextArea ta = new TextArea();
        ta.setEditable(false);
        VBox vBox = new VBox();
        vBox.setLayoutX(0);
        vBox.setLayoutY(60);
        vBox.getChildren().addAll(edit, ta);
        vBox.setSpacing(10);

        TextField text = (TextField) nodes.get(1);
        edit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!"".equals(text.getText()) && !"".equals(oldFiled.getText()) && !"".equals(newFiled.getText())) {
                    System.out.println("text = " + text.getText());
                    System.out.println("oldFiled = " + oldFiled.getText());
                    System.out.println("newFiled = " + newFiled.getText());
                    ReName.replaceFileName(text.getText(), oldFiled.getText(), newFiled.getText());
                    ta.setText("批量修改完成!");
                } else {
                    ta.setText("请确认已选择文件夹路径，并填写了旧字符和新字符！");
                }
            }
        });


        // 布局类
        AnchorPane root = new AnchorPane();
        VBox back = new VBox();
        AnchorPane.setLeftAnchor(back, 10.0);
        AnchorPane.setTopAnchor(back, 10.0);
        back.setSpacing(10);
        back.getChildren().addAll(hBox, hBox1, vBox);
        root.getChildren().addAll(back);

        return root;
    }
}
