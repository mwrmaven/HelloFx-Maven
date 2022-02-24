package org.example.interfaces.impl;

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
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.button.BatchButton;
import org.example.interfaces.Function;
import org.example.util.Unit;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mavenr
 * @Classname GenerateText
 * @Description TODO
 * @Date 2022/1/20 10:32
 */
public class GenerateText implements Function {

    private static final Pattern pattern = Pattern.compile("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}");

    private final Unit unit = new Unit();

    @Override
    public String tabName() {
        return "文本批量生成";
    }

    @Override
    public String tabStyle() {
        String style = "-fx-font-weight: bold; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-focus-color: transparent; -fx-text-base-color: white; " +
                "-fx-background-color: #9068be;  -fx-pref-height: 30; ";
        return style;
    }

    @Override
    public AnchorPane tabPane(Stage stage, double width, double h) {
        AnchorPane ap = new AnchorPane();

        double areaLength = width / 2;

        // 竖向布局
        VBox all = new VBox();
        all.setPadding(new Insets(10));
        all.setSpacing(10);

        // 第一行， 文本模板
        VBox line1 = new VBox();
        line1.setSpacing(5);
        Label labelOfLine1 = new Label("文本模板:");
        // 输入文本模板，参数使用
        TextArea textArea = new TextArea();
        textArea.setPrefWidth(areaLength);
        textArea.setPrefHeight(300);
        StringBuilder prompt = new StringBuilder("请在此输入文本模板，参数使用 ${para} 的方式表示，其中para为参数变量，可以按习惯修改，例如：\r\n");
        prompt.append("输入————————————————————————————————————————————\r\n" +
                "这个文本内容是用来测试文本模板的使用，请注意：\r\n" +
                "1、如果使用固定的文本则无需配置参数信息；\r\n" +
                "2、如果使用参数信息，请使用 ${para1} 、 ${text} 的方式传入动态参数；\r\n" +
                "3、暂时只支持根据文本模板循环打印数据；\r\n" +
                "赋值————————————————————————————————————————————\r\n" +
                "para1 = 其他\r\n" +
                "text = 这个\r\n" +
                "输出————————————————————————————————————————————\r\n" +
                "这个文本内容是用来测试文本模板的使用，请注意：\r\n" +
                "1、如果使用固定的文本则无需配置参数信息；\r\n" +
                "2、如果使用参数信息，请使用 其他 、 这个 的方式传入动态参数；\r\n" +
                "3、暂时只支持根据文本模板循环打印数据；");
        textArea.setPromptText(prompt.toString());

        // 配置文本参数
        HBox editParamButton = new HBox();
        editParamButton.setAlignment(Pos.CENTER_LEFT);
        editParamButton.setSpacing(10);
        Button button = new Button();
        button.setText("点击配置文本参数");
        button.setAlignment(Pos.CENTER);
        button.setPrefWidth(200);
        editParamButton.getChildren().add(button);
        line1.getChildren().addAll(labelOfLine1, textArea, editParamButton);

        // 第二行，文本模板中的参数
        VBox line2 = new VBox();
        // 点击按钮事件
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // 获取文本模板
                line2.getChildren().clear();
                String textTemplate = textArea.getText();
                Matcher matcher = pattern.matcher(textTemplate);
                List<HBox> hs = new ArrayList<>();
                List<String> params = new ArrayList<>();
                while (matcher.find()) {
                    String param = matcher.group();
                    if (params.contains(param)) {
                        continue;
                    }
                    params.add(param);
                    HBox hBoxOfLine2 = new HBox();
                    hBoxOfLine2.setAlignment(Pos.CENTER_LEFT);
                    hBoxOfLine2.setSpacing(10);
                    Label label1OfLine2 = new Label("文本参数: ");
                    TextField tf1OfLine2 = new TextField(param);
                    tf1OfLine2.setEditable(false);
                    Label label2OfLine2 = new Label("参数值（多个以英文逗号分隔）: ");
                    TextField tf2OfLine2 = new TextField();
                    tf2OfLine2.setPrefWidth(400);
                    tf2OfLine2.setPromptText("注意，每个文本参数对应的参数值个数必须一致");

                    hBoxOfLine2.getChildren().addAll(label1OfLine2, tf1OfLine2, label2OfLine2, tf2OfLine2);
                    hs.add(hBoxOfLine2);
                }

                if (CollectionUtils.isNotEmpty(hs)) {
                    editParamButton.getChildren().clear();
                    editParamButton.getChildren().add(button);
                    line2.setSpacing(5);
                    line2.getChildren().addAll(hs);
                    all.getChildren().add(1, line2);
                } else {
                    all.getChildren().remove(line2);
                    editParamButton.getChildren().clear();
                    editParamButton.getChildren().addAll(button, new Label("文本模板中未查询到参数"));
                }
            }
        });

        // 分隔线
        HBox line3 = new HBox();
        line3.setAlignment(Pos.CENTER_LEFT);
        Line line = new Line();
        line.setStroke(Color.DIMGRAY);
        line.setStartX(0);
        line.setStartY(0);
        line.setStartX(areaLength);
        line.setStartY(0);
        line3.getChildren().add(line);

        // 文本输出路径
        List<Node> nodes = unit.chooseFolder(stage, width, "请选择结果路径");
        HBox line4 = new HBox();
        line4.setSpacing(10);
        line4.setAlignment(Pos.CENTER_LEFT);
        for (Node n : nodes) {
            line4.getChildren().add(n);
        }

        // 批量生成文本按钮
        BatchButton batchButton = new BatchButton();
        batchButton.setText("批量生成文本");
        batchButton.setWight(150);
        Button line5 = batchButton.createInstance();

        // 结果文本框
        TextArea line6 = new TextArea();
        line6.setPrefWidth(areaLength);
        line6.setPrefHeight(50);

        all.getChildren().addAll(line1, line3, line4, line5, line6);
        ap.getChildren().add(all);

        line5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // 批量按钮点击事件
                StringBuilder sb = new StringBuilder();

                List<Node> hBoxs = line2.getChildren();
                // 获取每行信息
                Map<String, String[]> replaces = new HashMap<>();
                Set<Integer> valueNums = new HashSet<>();
                for (Node node : hBoxs) {
                    HBox paramAndValue = (HBox) node;
                    List<Node> nodes = paramAndValue.getChildren();
                    // 参数
                    String oldV = ((TextField) nodes.get(1)).getText();
                    // 值
                    String newV = ((TextField) nodes.get(3)).getText();
                    if (StringUtils.isNotEmpty(newV) && StringUtils.isNotBlank(newV)) {
                        // 放到map集合中，之后进行替换
                        String[] split = newV.split(",");
                        replaces.put(oldV, split);
                        valueNums.add(split.length);
                    }
                }
                // 判断参数值的个数是否相同
                if (valueNums.size() != 1) {
                    line6.setText("每个文本参数对应的参数值个数不一致");
                    return;
                }

                int count = valueNums.iterator().next();
                // 遍历替换参数
                for (int i = 0; i < count; i++) {
                    String text = textArea.getText();
                    for (String key : replaces.keySet()) {
                        String tmp = key.substring(2, key.length() - 1);
                        text = text.replaceAll("\\$\\{" + tmp + "\\}", replaces.get(key)[i]);
                    }
                    sb.append(text).append("\n\n");
                }
                // 去掉末尾的\n
                sb.substring(0, sb.length() - 1);

                // 将生成的文本写入到文本文件中
                String path = ((TextField) nodes.get(1)).getText();
                if (StringUtils.isEmpty(path)) {
                    line6.setText("请选择结果路径");
                    return;
                }
                String separator = File.separator;
                if (!path.endsWith(separator)) {
                    path += separator;
                }
                path += "批量生成的文本.txt";
                File file = new File(path);
                try {
                    FileUtils.writeByteArrayToFile(file, sb.toString().getBytes());
                } catch (IOException e) {
                    line6.setText(path + "文件生成出错：" + e.getMessage());
                    e.printStackTrace();
                }
                line6.setText("结果文件路径：" + path);
            }
        });
        return ap;
    }
}
