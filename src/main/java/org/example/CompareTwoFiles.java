package org.example;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

/**
 * @author mavenr
 * @Classname CompareTwoFiles
 * @Description 比较两个文件的不同
 * @Date 2021/9/16 17:06
 */
public class CompareTwoFiles {

    private Unit unit = new Unit();

    public AnchorPane compare(Stage primaryStage, double width, double height) {
        System.out.println("宽度：" + width);
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(10));
        hBox.setSpacing(10);
        // 左边部分
        double leftWidth = (width / 2 + 200) / 2;
        VBox left = new VBox();
        left.setPrefWidth(leftWidth);
        left.setSpacing(10);
        // 左上选择框
        HBox leftTop = new HBox();
        leftTop.setSpacing(10);
        leftTop.setAlignment(Pos.TOP_LEFT);
        List<Node> newChoose = unit.chooseFile(primaryStage, width / 2, "选择新文件");
        for (Node node : newChoose) {
            leftTop.getChildren().addAll(node);
        }
        left.getChildren().addAll(leftTop);

        // 为按钮添加点击事件
        ((TextField) newChoose.get(1)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // 文本区域
                String newFilePath = ((TextField) newChoose.get(1)).getText();
                StringBuilder newContents = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(new File(newFilePath)));
                    String temp;
                    while ((temp = br.readLine()) != null) {
                        newContents.append(temp).append("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                InlineCssTextArea newArea = new InlineCssTextArea(newContents.toString());
                newArea.setPrefHeight(height - 120);
                left.getChildren().addAll(newArea);
            }
        });

        // 右边部分
        VBox right = new VBox();
        right.setPrefWidth((width / 2 + 200) / 2);
        right.setSpacing(10);
        HBox rightTop = new HBox();
        rightTop.setSpacing(10);
        rightTop.setAlignment(Pos.TOP_LEFT);
        List<Node> oldChoose = unit.chooseFile(primaryStage, width / 2, "选择旧文件");
        for (Node node : oldChoose) {
            rightTop.getChildren().addAll(node);
        }
        right.getChildren().addAll(rightTop);

        // 为按钮添加点击事件
        ((TextField) oldChoose.get(1)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // 文本区域
                String newFilePath = ((TextField) oldChoose.get(1)).getText();
                StringBuilder oldContents = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(new File(newFilePath)));
                    String temp;
                    while ((temp = br.readLine()) != null) {
                        oldContents.append(temp).append("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                InlineCssTextArea oldArea = new InlineCssTextArea(oldContents.toString());
                oldArea.setPrefHeight(height - 120);
                right.getChildren().addAll(oldArea);
            }
        });






        // 添加左右布局
        hBox.getChildren().addAll(left, right);
        // 布局类
        AnchorPane ap = new AnchorPane();
        ap.getChildren().addAll(hBox);
        ap.setPadding(new Insets(10));
        return ap;
    }
}
