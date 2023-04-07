package org.example.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.example.init.Config;

import java.io.File;
import java.io.IOException;
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
    public List<Node> chooseFolder(Stage primaryStage, double width, String buttonText) {
        // 选择文件夹/文件路径，并输入到文本框
        TextField text = new TextField();
        // 输入框中禁止编辑
        text.setDisable(true);
        text.setPrefWidth(width / 2 - 200);

        // 点击按钮，选择文件夹
        Button buttonFileChoose = new Button("选择文件夹路径");
        if (buttonText != null && !"".equals(buttonText.replaceAll(" ", ""))) {
            buttonFileChoose.setText(buttonText);
        }
        buttonFileChoose.setPrefWidth(120);
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

        // 设置button样式
        String style = "-fx-background-color: #FFD700; -fx-background-radius: 4";
        buttonFileChoose.setStyle(style);

        return nodes;
    }

    /**
     * 选择文件组件集合
     * @param primaryStage
     * @param width
     * @param buttonText
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
                String baseDir = Config.get("baseDir");
                FileChooser fc = new FileChooser();
                if (StringUtils.isNotBlank(baseDir)) {
                    File baseFile = new File(baseDir);
                    if (baseFile.exists()) {
                        fc.setInitialDirectory(baseFile);
                    } else {
                        fc.setInitialDirectory(new File("./"));
                    }
                }
                File file = fc.showOpenDialog(primaryStage);
                if (file == null) {
                    return;
                }
                String path = file.getPath();
                text.setText(path);
                Config.set("baseDir", file.getParent());
            }
        });

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(buttonFileChoose);
        nodes.add(text);

        // 设置button样式
        String style = "-fx-background-color: #ffcc33; -fx-background-radius: 4";
        buttonFileChoose.setStyle(style);

        return nodes;
    }

    /**
     * 输入文本的组件集合
     * @param primaryStage
     * @param width
     * @param buttonText
     * @return
     */
    @Deprecated
    public List<Node> inputText(Stage primaryStage, double width, String buttonText) {
        Label label = new Label();
        if (StringUtils.isNotEmpty(buttonText)) {
            label.setText(buttonText);
        } else {
            label.setText("请输入文本");
        }

        // 输入文本
        TextField text = new TextField();
        text.setPrefWidth(width / 2 - 200);

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(label);
        nodes.add(text);

        return nodes;
    }

    /**
     * 输入文本的组件的集合
     * @param width 框体的宽度
     * @param lText label的文本
     * @param labelWidth label的宽度
     * @return
     */
    public List<Node> newInputText(double width, String lText, double labelWidth) {
        Label label = new Label();
        label.setPrefWidth(labelWidth);
        if (StringUtils.isNotEmpty(lText)) {
            label.setText(lText);
        } else {
            label.setText("请输入文本");
        }

        // 输入文本
        TextField text = new TextField();
        text.setPrefWidth(width / 2 - 200);

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(label);
        nodes.add(text);

        return nodes;
    }

    /**
     * 文本框失去焦点的保存操作
     * @param field
     * @param configText
     * @param configKey
     */
    public void loseFocuseSave(TextField field, String configText, String configKey) {
        field.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // 判断内容改变，则保存内容
                if (!configText.equals(field.getText())) {
                    // 设置配置文件
                    Config.set(configKey, field.getText());
                }
            }
        });
    }

    /**
     * 启动浏览器
     * @param chromePath
     * @param ta
     */
    public void startChrome(String chromePath, TextArea ta) {
        if (StringUtils.isBlank(chromePath)) {
            ta.setText("请输入chrome浏览器的启动器类的文件路径！");
            return;
        }
        // 获取文件工具的路径
        String currentPath = System.getProperty("user.dir");
        // chrome测试数据存放路径
        String chromeTestPath= currentPath + File.separator + "chromeTest";
        // 启动chrome调试
        ta.appendText("\nchromePath = " + chromePath);
        // 查看端口是否被占用，如果被占用则先停掉端口再启动 区分 windows 和 mac ，命令行也是
        boolean alive = SocketUtil.isAlive("127.0.0.1", 9527);
        if (alive) {
            ta.setText("测试浏览器已启动，请在任务栏中点击浏览器图标创建新窗口！");
            return;
        }
        String[] cmd = new String[3];
        cmd[0] = chromePath;
        cmd[1] = "--remote-debugging-port=9527";
        cmd[2] = "--user-data-dir=" + chromeTestPath;
        try {
            Runtime.getRuntime().exec(cmd);
            ta.appendText("\nchrome浏览器远程调试模式启动成功！");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            ta.appendText("\nchrome浏览器远程调试模式启动失败，请联系技术人员！");
        }
    }

}
