package org.example;

import com.mavenr.enums.SortType;
import com.mavenr.file.FileContentSort;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.button.BatchButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mavenr
 * @Classname SortTextLine
 * @Description TODO 对文本文件中的文本行排序（根据文本行中的某个字符或某段文本排序）
 * @Date 2021/9/26 15:13
 */
public class SortTextLine {

    private Unit unit = new Unit();
    // 排序
    public AnchorPane sort(Stage stage, double width) {
        AnchorPane ap = new AnchorPane();

        // 文件夹获取
        List<Node> folderNodes = unit.chooseFolder(stage, width, null);
        // 文件获取
        List<Node> fileNodes = unit.chooseFile(stage, width, "获取文件");

        HBox line1 = new HBox();
        line1.setSpacing(10);
        for (Node n : folderNodes) {
            line1.getChildren().add(n);
        }

        // 单选框
        HBox line2 = new HBox();
        line2.setSpacing(10);
        for (Node n : fileNodes) {
            line2.getChildren().add(n);
        }

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        RadioButton rb1 = new RadioButton("单个文件内容排序");
        RadioButton rb2 = new RadioButton("文件夹下所有文件的内容排序");
        ToggleGroup tg = new ToggleGroup();
        rb1.setToggleGroup(tg);
        rb1.setSelected(true);
        rb2.setToggleGroup(tg);
        top.getChildren().addAll(rb1, rb2);
        top.setSpacing(10);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(top, line2);
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);

        tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                RadioButton selectedToggle = (RadioButton) tg.getSelectedToggle();
                if (rb1.getText().equals(selectedToggle.getText())) {
                    int index = vBox.getChildren().indexOf(line1);
                    vBox.getChildren().remove(line1);
                    vBox.getChildren().add(index, line2);
                } else {
                    int index = vBox.getChildren().indexOf(line2);
                    vBox.getChildren().remove(line2);
                    vBox.getChildren().add(index, line1);
                }
            }
        });

        // 查找行中排序关键字的方式
        HBox hBoxFindType = new HBox();
        hBoxFindType.setAlignment(Pos.CENTER_LEFT);
        hBoxFindType.setSpacing(10);
        Label hBoxFindTypeLabel = new Label("查找行中排序关键字的方式: ");

        RadioButton rb3 = new RadioButton("分隔符的方式查找");
        RadioButton rb4 = new RadioButton("字符范围查找");
        ToggleGroup findTg = new ToggleGroup();
        rb3.setToggleGroup(findTg);
        rb4.setToggleGroup(findTg);
        rb3.setSelected(true);

        hBoxFindType.getChildren().addAll(hBoxFindTypeLabel, rb3, rb4);
        vBox.getChildren().add(hBoxFindType);

        HBox line3 = new HBox();
        line3.setAlignment(Pos.CENTER_LEFT);
        line3.setSpacing(10);
        Label labelSplit = new Label("分割符: ");
        TextField split = new TextField();
        split.setPromptText("请输入文本行的分割符");
        split.setPrefWidth(200);
        Label labelSplitNum = new Label("关键字位置: ");
        TextField splitNum = new TextField();
        splitNum.setPromptText("关键字位置");

        // 鼠标放到文本框，提示信息
        Tooltip tip = new Tooltip("根据分割符分隔后，关键字在第几组\n例如：fgr|wrg|wg|gwg 使用|分割后，wg在第3组");
        tip.setFont(Font.font(16));
        splitNum.setTooltip(tip);
        splitNum.setPrefWidth(200);
        line3.getChildren().addAll(labelSplit, split, labelSplitNum, splitNum);
        vBox.getChildren().add(line3);

        HBox line4 = new HBox();
        line4.setSpacing(10);
        line4.setAlignment(Pos.CENTER_LEFT);
        Tooltip wordTip = new Tooltip("如果关键字为单个字符，则起始位置和结束位置相同");
        wordTip.setFont(Font.font(16));
        Label startLabel = new Label("关键字起始字符的位置: ");
        TextField start = new TextField();
        start.setTooltip(wordTip);
        start.setPrefWidth(200);
        Label endLabel = new Label("关键字结束字符的位置: ");
        TextField end = new TextField();
        end.setTooltip(wordTip);
        end.setPrefWidth(200);
        line4.getChildren().addAll(startLabel, start, endLabel, end);

        findTg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                RadioButton selectedToggle = (RadioButton) findTg.getSelectedToggle();
                if (rb3.getText().equals(selectedToggle.getText())) {
                    int index = vBox.getChildren().indexOf(line4);
                    vBox.getChildren().remove(line4);
                    vBox.getChildren().add(index, line3);
                } else {
                    int index = vBox.getChildren().indexOf(line3);
                    vBox.getChildren().remove(line3);
                    vBox.getChildren().add(index, line4);
                }
            }
        });

        // 排序方式
        HBox sortType = new HBox();
        sortType.setSpacing(10);
        sortType.setAlignment(Pos.CENTER_LEFT);
        Label sortLabel = new Label("排序方式: ");
        RadioButton asc = new RadioButton("升序");
        RadioButton desc = new RadioButton("降序");
        ToggleGroup sortToggle = new ToggleGroup();
        asc.setToggleGroup(sortToggle);
        desc.setToggleGroup(sortToggle);
        asc.setSelected(true);
        sortType.getChildren().addAll(sortLabel, asc, desc);
        vBox.getChildren().add(sortType);

        // 按钮
        BatchButton batchButton = new BatchButton();
        batchButton.setText("开始处理");
        Button begin = batchButton.createInstance();
//        Button begin = new Button("开始处理");
//        begin.setPrefWidth(100);
//        begin.setAlignment(Pos.CENTER);
        vBox.getChildren().add(begin);


        begin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String filePath = "";
                if (rb1.getText().equals(((RadioButton) tg.getSelectedToggle()).getText())) {
                    filePath = ((TextField) fileNodes.get(1)).getText();
                } else {
                    filePath = ((TextField) folderNodes.get(1)).getText();
                }

                // 获取文件集合
                File file = new File(filePath);
                List<File> fs = new ArrayList<>();
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    for (File f : files) {
                        fs.add(f);
                    }
                } else {
                    fs.add(file);
                }

                // 判断是分割符，还是获取字符位置范围
                RadioButton selectedToggle = (RadioButton) findTg.getSelectedToggle();
                if (rb3.getText().equals(selectedToggle.getText())) {
                    // 分割符的方式查找
                    // 分割符
                    String separator = split.getText();
                    if ("|".equals(separator)) {
                        separator = "\\" + separator;
                    }
                    // 关键字位置
                    String keyIndex = splitNum.getText();
                    if (((RadioButton) sortToggle.getSelectedToggle()).getText().equals(asc.getText())) {
                        // 升序
                        FileContentSort.sortText(fs, separator, Integer.parseInt(keyIndex), SortType.ASC.getCode());
                    } else {
                        // 降序
                        FileContentSort.sortText(fs, separator, Integer.parseInt(keyIndex), SortType.DESC.getCode());
                    }

                } else {
                    // 获取字符位置范围
                    // 起始位置
                    String startNum = start.getText();
                    // 结束位置
                    String endNum = end.getText();
                    if (((RadioButton) sortToggle.getSelectedToggle()).getText().equals(asc.getText())) {
                        // 升序
                        FileContentSort.sortText(fs, Integer.parseInt(startNum), Integer.parseInt(endNum), SortType.ASC.getCode());
                    } else {
                        // 降序
                        FileContentSort.sortText(fs, Integer.parseInt(startNum), Integer.parseInt(endNum), SortType.DESC.getCode());
                    }
                }
            }
        });

        TextArea ta = new TextArea();
        ta.setEditable(false);
        vBox.getChildren().add(ta);

        ap.getChildren().add(vBox);
        return ap;
    }
}
