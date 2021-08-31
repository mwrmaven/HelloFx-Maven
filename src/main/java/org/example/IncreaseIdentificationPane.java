package org.example;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * @author mavenr
 * @Classname IncreaseIdentificationPane
 * @Description TODO
 * @Date 2021/8/31 14:32
 */
public class IncreaseIdentificationPane {

    public FlowPane increaseIdentification(Stage stage, double width) {
        Unit unit = new Unit();
        // 创建选择组件
        List<Node> nodes = unit.chooseFolder(stage, width);
        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(10));
        hBox1.setSpacing(20);
        for (Node node : nodes) {
            hBox1.getChildren().addAll(node);
        }

        HBox hBox2 = new HBox();
        hBox2.setPadding(new Insets(10));
        hBox2.setLayoutY(30);
        hBox2.setSpacing(20);
        hBox2.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("文件名中插入或替换: ");
        // 创建单选组和单选按钮
        ToggleGroup tg = new ToggleGroup();
        RadioButton button1 = new RadioButton("后置插入");
        RadioButton button2 = new RadioButton("前置插入");
        RadioButton button3 = new RadioButton("替换字符");
        // 将单选按钮添加到单选组
        button1.setToggleGroup(tg);
        button2.setToggleGroup(tg);
        button3.setToggleGroup(tg);
        // 默认选中后置
        button1.setSelected(true);
        // 选中替换字符后显示的文本框
        TextField textField = new TextField();
        textField.setPromptText("请输入旧字符");
        textField.setVisible(false);
        // 配置按钮的触发事件
        tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (button3.getText().equals(((RadioButton)tg.getSelectedToggle()).getText())) {
                    textField.setVisible(true);
                } else {
                    textField.setVisible(false);
                }
            }
        });
        // 将组件添加到横向布局中
        hBox2.getChildren().addAll(label, button1, button2, button3, textField);

        // 选择模板文件
        List<Node> nodes1 = unit.chooseFile(stage, width);
        HBox hBox4 = new HBox();
        hBox4.setSpacing(20);
        hBox4.setPadding(new Insets(10));
        nodes1.forEach(item -> {
            hBox4.getChildren().addAll(item);
        });

        HBox hBox3 = new HBox();
        Label labelOfHBox2 = new Label("根据模板文件创建文件或只替换文件名: ");
        ToggleGroup tgParent = new ToggleGroup();
        RadioButton create = new RadioButton("根据模板文件创建文件");
        RadioButton replace = new RadioButton("只替换文件名");
        create.setToggleGroup(tgParent);
        replace.setToggleGroup(tgParent);
        replace.setSelected(true);
        hBox3.getChildren().addAll(labelOfHBox2, create, replace);
        hBox3.setPadding(new Insets(10));
        hBox3.setSpacing(20);
        tgParent.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {

            }
        });








        FlowPane root = new FlowPane();
        root.setOrientation(Orientation.VERTICAL);
        root.getChildren().addAll(hBox1, hBox2, hBox3, hBox4);
        return root;
    }
}
