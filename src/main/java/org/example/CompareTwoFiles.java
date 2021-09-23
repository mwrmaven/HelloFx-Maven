package org.example;

import com.mavenr.encrypt.MD5;
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
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        double leftWidth = (width / 2 + 100) / 2;
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



        // 右边部分
        VBox right = new VBox();
        right.setPrefWidth((width / 2 + 100) / 2);
        right.setSpacing(10);
        HBox rightTop = new HBox();
        rightTop.setSpacing(10);
        rightTop.setAlignment(Pos.TOP_LEFT);
        List<Node> oldChoose = unit.chooseFile(primaryStage, width / 2, "选择旧文件");
        for (Node node : oldChoose) {
            rightTop.getChildren().addAll(node);
        }
        right.getChildren().addAll(rightTop);

        // 为左按钮添加点击事件
        ((TextField) newChoose.get(1)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // 文本区域
                String newFilePath = ((TextField) newChoose.get(1)).getText();
                List<int[]> newIndex = new ArrayList<>();
                List<String> newMd5s = new ArrayList<>();
                StringBuilder newContents = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(new File(newFilePath)));
                    String temp;
                    int num = 0;
                    while ((temp = br.readLine()) != null) {
                        newContents.append(temp).append("\n");
                        String md5 = MD5.encode(temp);
                        int[] range = new int[2];
                        range[0] = num;
                        num += temp.length() + 1;
                        range[1] = num;
                        newIndex.add(range);
                        newMd5s.add(md5);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                InlineCssTextArea newArea = new InlineCssTextArea(newContents.toString());
                newArea.setPrefHeight(height - 120);
                left.getChildren().addAll(newArea);

                // 获取新旧两个文件内容
                String oldFilePath = ((TextField) oldChoose.get(1)).getText();
                StringBuilder oldContents = new StringBuilder();
                List<int[]> oldIndex = new ArrayList<>();
                List<String> oldMd5s = new ArrayList<>();
                if (!StringUtils.isEmpty(oldFilePath)) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(new File(oldFilePath)));
                        String temp;
                        int num = 0;
                        while ((temp = br.readLine()) != null) {
                            oldContents.append(temp).append("\n");
                            String md5 = MD5.encode(temp);
                            int[] range = new int[2];
                            range[0] = num;
                            num += temp.length() + 1;
                            range[1] = num;
                            oldIndex.add(range);
                            oldMd5s.add(md5);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 匹配右文件，设置左文件的文本颜色
                int newFlag = 0;
                int oldFlag = 0;
                for (int i = 0; i < newMd5s.size(); i++) {
                    if (i >= oldMd5s.size()) {
                        // 新文件中大于i的都是新增的
                        newArea.setStyle(newIndex.get(i)[0], newIndex.get(newIndex.size() - 1)[1], "-fx-fill: green");
                        break;
                    }
                    String newMd5 = newMd5s.get(i);
                    if (oldMd5s.lastIndexOf(newMd5) != -1 && oldMd5s.lastIndexOf(newMd5) > oldFlag) {
                        newArea.setStyle(newIndex.get(newFlag)[0], newIndex.get(i)[1], "-fx-fill: red");
                        newFlag = i;
                        oldFlag = oldMd5s.lastIndexOf(newMd5);
                        continue;
                    }
                    if (oldMd5s.lastIndexOf(newMd5) == i) {
                        newFlag = i;
                        oldFlag = i;
                    }
                }
//                for (int i = 0; i < newMd5s.size(); i++) {
//                    if (i >= oldMd5s.size()) {
//                        // 新增的
//                        continue;
//                    }
//                    String newMd5 = newMd5s.get(i);
//
//                }
            }
        });

        // 为右按钮添加点击事件
        ((TextField) oldChoose.get(1)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // 文本区域
                String oldFilePath = ((TextField) oldChoose.get(1)).getText();
                StringBuilder oldContents = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(new File(oldFilePath)));
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
