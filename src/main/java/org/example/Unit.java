package org.example;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mavenr
 * @Classname Unit
 * @Description 通用的组件单元
 * @Date 2021/8/30 16:28
 */
public class Unit {

    /**
     * 选择文件夹组件集合
     * @param primaryStage
     * @param width
     * @return
     */
    public List<Node> chooseFolder(Stage primaryStage, double width) {
        // 选择文件夹/文件路径，并输入到文本框
        TextField text = new TextField();
        // 输入框中禁止编辑
        text.setDisable(true);
        text.setPrefWidth(width / 2 - 200);

        // 点击按钮，选择文件夹
        Button buttonFileChoose = new Button("选择文件夹路径");
        buttonFileChoose.setPrefWidth(100);
        buttonFileChoose.setAlignment(Pos.CENTER);
        buttonFileChoose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser dc = new DirectoryChooser();
                File file = dc.showDialog(primaryStage);
                String path = file.getPath();
                text.setText(path);
            }
        });

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(buttonFileChoose);
        nodes.add(text);

        return nodes;
    }

    /**
     * 选择文件组件集合
     * @param primaryStage
     * @param width
     * @return
     */
    public List<Node> chooseFile(Stage primaryStage, double width, String buttonText) {
        // 选择文件夹/文件路径，并输入到文本框
        TextField text = new TextField();
        // 输入框中禁止编辑
        text.setDisable(true);
        text.setPrefWidth(width / 2 - 200);

        // 点击按钮，选择文件夹
        Button buttonFileChoose = new Button("选择文件路径");
        if (buttonText != null && !"".equals(buttonText.replaceAll(" ", ""))) {
            buttonFileChoose.setText(buttonText);
        }
        buttonFileChoose.setPrefWidth(100);
        buttonFileChoose.setAlignment(Pos.CENTER);
        buttonFileChoose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fc = new FileChooser();
                File file = fc.showOpenDialog(primaryStage);
                if (file == null) {
                    return;
                }
                String path = file.getPath();
                text.setText(path);
            }
        });

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(buttonFileChoose);
        nodes.add(text);

        return nodes;
    }

}
