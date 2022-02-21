package org.example.interfaces.impl;

import com.mavenr.encrypt.MD5;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.example.button.BatchButton;
import org.example.common.Common;
import org.example.interfaces.Function;
import org.example.util.Unit;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author mavenr
 * @Classname FileCompare
 * @Description 文件比较，实现text格式、word格式
 * @Date 2022/1/6 9:25
 */
public class FileCompare  implements Function {
    /**
     * 生成选择框的统一工具
     */
    private Unit unit = new Unit();

    @Override
    public String tabName() {
        return Common.TOP_BUTTON_3;
    }

    @Override
    public String tabStyle() {
        String style = "-fx-font-weight: bold; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-focus-color: transparent; -fx-text-base-color: white; " +
                "-fx-background-color: #23B5AF;  -fx-pref-height: 30; ";
        return style;
    }

    @Override
    public AnchorPane tabPane(Stage stage, double width, double h) {
        AnchorPane root = new AnchorPane();
        // 列方向
        HBox columns = new HBox();
        columns.setPadding(new Insets(10));
        columns.setSpacing(10);

        // 左列，新文件
        VBox vBoxLeft = new VBox();
        vBoxLeft.setPrefWidth(550);
        vBoxLeft.setPrefHeight(700);
        vBoxLeft.setSpacing(10);

        // 选择新文件
        HBox chooseFile = new HBox();
        chooseFile.setSpacing(10);
        List<Node> newChoose = unit.chooseFile(stage, width / 2 + 150, "新文件");
        for (Node n : newChoose) {
            chooseFile.getChildren().add(n);
        }
        vBoxLeft.getChildren().add(chooseFile);

        // 左侧显示文本内容区域
        InlineCssTextArea newTextArea = new InlineCssTextArea();
        newTextArea.setPrefHeight(600);
        vBoxLeft.getChildren().add(newTextArea);

        // 右列，旧文件
        VBox vBoxRight = new VBox();
        vBoxRight.setPrefWidth(550);
        vBoxRight.setPrefHeight(700);
        vBoxRight.setSpacing(10);

        // 选择旧文件
        HBox chooseOldFile = new HBox();
        chooseOldFile.setSpacing(10);
        List<Node> oldChoose = unit.chooseFile(stage, width / 2 + 150, "旧文件");
        for (Node n : oldChoose) {
            chooseOldFile.getChildren().add(n);
        }
        vBoxRight.getChildren().add(chooseOldFile);

        // 右侧显示文本内容区域
        InlineCssTextArea oldTextArea = new InlineCssTextArea();
        oldTextArea.setPrefHeight(600);
        vBoxRight.getChildren().add(oldTextArea);

        // 底部的比较按钮
        BatchButton batchButton = new BatchButton("比较", 1110.0, 60.0);
        Button compare = batchButton.createInstance();

        // 将列添加到根布局中
        columns.getChildren().addAll(vBoxLeft, vBoxRight);
        AnchorPane.setLeftAnchor(compare, 10.0);
        AnchorPane.setBottomAnchor(compare, 20.0);
        root.getChildren().addAll(columns, compare);

        // 获取选择文件的按钮
        TextField newField = (TextField) newChoose.get(1);
        TextField oldField = (TextField) oldChoose.get(1);
        // 添加文本改变的监听事件
        newField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                getContent(newValue, newTextArea);
            }
        });

        oldField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                getContent(newValue, oldTextArea);
            }
        });

        // 比较按钮添加点击事件
        compare.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String newContent = getContent(newField.getText(), newTextArea);
                String oldContent = getContent(oldField.getText(), oldTextArea);

                // 比较两边的文本
                compareString(newContent, oldContent, newTextArea, oldTextArea);
            }
        });

        return root;
    }

    /**
     * 获取指定路径的文件内容，并打印出来
     * @param filePath 文件的路径
     * @param textArea 文本内容区域
     * @return 文件内容
     */
    public String getContent(String filePath, InlineCssTextArea textArea) {
        // 判断新文件格式
        if (!filePath.endsWith(".txt") && !filePath.endsWith(".doc") && !filePath.endsWith(".docx")) {
            textArea.clear();
            textArea.appendText("当前只能匹配txt格式或doc、docx格式文件");
            return "";
        }

        File file = new File(filePath);
        if (filePath.endsWith(".txt")) {
            // txt文件
            FileReader fileReader = null;
            BufferedReader br = null;
            try {
                fileReader = new FileReader(file);
                br = new BufferedReader(fileReader);
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    System.out.println(line);
                    sb.append(line).append("\n");
                    line = br.readLine();
                }
//                textArea.setText(sb.toString());
                textArea.clear();
                textArea.appendText(sb.toString());
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                    if (fileReader != null) {
                        fileReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (filePath.endsWith(".doc")){
            // word 文档
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                WordExtractor word = new WordExtractor(fis);
                String content = word.getText();
//                textArea.setText(content);
                textArea.clear();
                textArea.appendText(content);
                return content;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (filePath.endsWith(".docx")) {
            OPCPackage opcPackage = null;
            POIXMLTextExtractor extractor = null;
            try {
                opcPackage = POIXMLDocument.openPackage(filePath);
                extractor = new XWPFWordExtractor(opcPackage);
                String content = extractor.getText();
                textArea.clear();
                textArea.appendText(content);
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (extractor != null) {
                        extractor.close();
                    }
                    if (opcPackage != null) {
                        opcPackage.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    /**
     * 比较不同的行，并在文本区域显示出来
     * @param newContent
     * @param oldContent
     * @param newTextArea
     */
    public void compareString(String newContent, String oldContent, InlineCssTextArea newTextArea, InlineCssTextArea oldTextArea) {
        List<List<String>> newList = getMD5AndResource(newContent);
        List<List<String>> oldList = getMD5AndResource(oldContent);

        if (CollectionUtils.isNotEmpty(newList) && CollectionUtils.isNotEmpty(oldList)) {

            List<String> newMD5List = newList.get(0);
            List<String> oldMD5List = oldList.get(0);
            int newSize = newMD5List.size();
            int oldSize = oldMD5List.size();
            int flag = -1;

            int newStartLine = -1;
            int oldStartLine = -1;

            // 正向遍历，找出不同的起始位置
            for (int i = 0; i < newSize; i++) {
                if (i > oldSize - 1) {
                    break;
                }
                String newMD5 = newMD5List.get(i);
                String oldMD5 = oldMD5List.get(i);
                if (!newMD5.equals(oldMD5)) {
                    flag = i;
                    break;
                }
            }
            newStartLine = flag;
            oldStartLine = flag;

            int start = 0;
            int oldStart = 0;
            for (int i = 0; i < flag; i++) {
                start += newList.get(1).get(i).length() + 1;
                oldStart += oldList.get(1).get(i).length() + 1;
            }
            // 判断具体的开始位置
            String newLine = newList.get(1).get(flag);
            String oldLine = oldList.get(1).get(flag);
            for (int i = 0; i < Math.min(newLine.length(), oldLine.length()); i++) {
                if (newLine.charAt(i) == oldLine.charAt(i)) {
                    start += 1;
                    oldStart += 1;
                } else {
                    break;
                }
            }

            flag = -1;
            int descFlag = -1;
            // 反向遍历，获取结束位置
            for (int i = 0; i < newSize ; i++) {
                int newIndex = newSize - i - 1;
                int oldIndex = oldSize - i - 1;
                if (oldIndex < 0) {
                    break;
                }
                String newMD5 = newMD5List.get(newIndex);
                String oldMD5 = oldMD5List.get(oldIndex);
                if (!newMD5.equals(oldMD5)) {
                    flag = newIndex;
                    descFlag = oldIndex;
                    System.out.println("new: " + newList.get(1).get(newIndex));
                    System.out.println("old: " + oldList.get(1).get(oldIndex));
                    System.out.println("----------------------------------------");
                    break;
                }
            }

            int end = 0;
            int oldEnd = 0;
            for (int i = 0; i < flag; i++) {
                end += newList.get(1).get(i).length() + 1;
            }
            for (int i = 0; i < descFlag; i++) {
                oldEnd += oldList.get(1).get(i).length() + 1;
            }
            // 判断具体的开始位置
            String newLine1 = newList.get(1).get(flag);
            String oldLine1 = oldList.get(1).get(descFlag);

            int sum = 0;
            for (int i = 0; i < Math.min(newLine1.length(), oldLine1.length()); i++) {
                if (newLine1.charAt(newLine1.length() - i - 1) != oldLine1.charAt(oldLine1.length() - i - 1)) {
                    sum += 1;
                } else {
                    break;
                }
            }
            end += newLine1.length() - sum + 1;
            oldEnd += oldLine1.length() - sum + 1;
            System.out.println("start = " + start  + "; end = " + end);

            newTextArea.setStyle(start, end, "-fx-fill: red");
            oldTextArea.setStyle(oldStart, oldEnd, "-fx-fill: red");

            // 从开始行到结束行，判断新文件中在旧文件中相同的，把行颜色标为黑色
            List<Integer> sameNewLines = new ArrayList<>();
            List<Integer> sameOldLines = new ArrayList<>();
            List<String> strings = oldMD5List.subList(oldStartLine, descFlag + 1);
            List<String> haveLine = new ArrayList();
            for (int i = newStartLine; i <= flag; i++) {
                if (haveLine.contains(newMD5List.get(i))) {
                    continue;
                }
                int i1 = strings.indexOf(newMD5List.get(i));
                if (i1 != -1) {
                    haveLine.add(newMD5List.get(i));
                    sameNewLines.add(i);
                    sameOldLines.add(oldStartLine + i1);
                }
            }
            int num = 0;
            for (int i = 0; i < newSize; i++) {
                int lineSize = newList.get(1).get(i).length() + 1;
                if (sameNewLines.contains(i)) {
                    newTextArea.setStyle(num, num + lineSize, "-fx-fill: black");
                }
                num += lineSize;
            }

            num = 0;
            for (int i = 0; i < oldSize; i++) {
                int lineSize = oldList.get(1).get(i).length() + 1;
                if (sameOldLines.contains(i)) {
                    oldTextArea.setStyle(num, num + lineSize, "-fx-fill: black");
                }
                num += lineSize;
            }
        }
    }


    /**
     * 获取每行数据对应的md5码，以及每行数据的原数据
     * @param content
     * @return
     */
    private List<List<String>> getMD5AndResource(String content) {
        List<List<String>> result = new ArrayList<>();
        if (StringUtils.isEmpty(content)) {
            return result;
        }
        ByteArrayInputStream byteArrayInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        // 读取每行数据
        try {
            byteArrayInputStream = new ByteArrayInputStream(content.getBytes(Charset.forName("utf-8")));
            inputStreamReader = new InputStreamReader(byteArrayInputStream);
            br = new BufferedReader(inputStreamReader);

            List<String> md5List = new ArrayList<>();
            List<String> resource = new ArrayList<>();
            String line = br.readLine();
            while (line != null) {
                md5List.add(MD5.encode(line));
                resource.add(line);
                line = br.readLine();
            }
            result.add(md5List);
            result.add(resource);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
