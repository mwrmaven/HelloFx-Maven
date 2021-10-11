package org.example;

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
import javafx.stage.Stage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author mavenr
 * @Classname UrlConvert
 * @Description URL 编码转换，即编码和解码
 * @Date 2021/10/11 16:59
 */
public class UrlConvert {

    private Unit unit = new Unit();

    public AnchorPane convert(Stage primaryStage, double width, double height) {
        AnchorPane anchorPane = new AnchorPane();
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));

        // url数据源选择，默认为单个文件中url批量转换
        RadioButton onlyTextLineRadio = new RadioButton("单个url转换");
        RadioButton oneFileRadio = new RadioButton("单个文件中url批量转换");
        RadioButton folderRadio = new RadioButton("文件夹下所有文件中url批量转换");
        ToggleGroup toggleGroup = new ToggleGroup();
        onlyTextLineRadio.setToggleGroup(toggleGroup);
        oneFileRadio.setToggleGroup(toggleGroup);
        folderRadio.setToggleGroup(toggleGroup);
        oneFileRadio.setSelected(true);
        HBox line1 = new HBox();
        line1.setSpacing(10);
        line1.setAlignment(Pos.CENTER_LEFT);
        line1.getChildren().addAll(onlyTextLineRadio, oneFileRadio, folderRadio);

        // 第二行信息
        List<Node> aList = unit.inputText(primaryStage, width, "请输入单个url");
        List<Node> bList = unit.chooseFile(primaryStage, width, "获取文件");
        List<Node> cList = unit.chooseFolder(primaryStage, width);

        HBox line2 = new HBox();
        line2.setAlignment(Pos.CENTER_LEFT);
        line2.setSpacing(10);
        for (Node node : bList) {
            line2.getChildren().add(node);
        }

        // 文件格式选择
        RadioButton txtRadio = new RadioButton("TXT格式文件");
        RadioButton excelRadio = new RadioButton("EXCEL格式文件");
        txtRadio.setSelected(true);
        ToggleGroup fileType = new ToggleGroup();
        txtRadio.setToggleGroup(fileType);
        excelRadio.setToggleGroup(fileType);
        TextField tf = new TextField();
        tf.setPrefWidth(200);
        tf.setVisible(false);
        tf.setPromptText("请输入url在excel文件中的第几列");

        HBox line3 = new HBox();
        line3.setSpacing(10);
        line3.getChildren().addAll(txtRadio, excelRadio, tf);
        line3.setAlignment(Pos.CENTER_LEFT);

        HBox line4 = new HBox();
        line4.setSpacing(10);
        line4.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("编译前在url上追加的字符：");
        Label preLabel = new Label("前置字符");
        TextField preTf = new TextField();
        preTf.setPromptText("没有则不输入");
        Label endLine = new Label("后置字符");
        TextField endTf = new TextField();
        endTf.setPromptText("没有则不输入");
        line4.getChildren().addAll(label, preLabel, preTf, endLine, endTf);

        HBox line5 = new HBox();
        line5.setSpacing(10);
        line5.setAlignment(Pos.CENTER_LEFT);
        Label afterlabel = new Label("编译后在url上追加的字符：");
        Label afterPreLabel = new Label("前置字符");
        TextField afterPreTf = new TextField();
        afterPreTf.setPromptText("没有则不输入");
        Label afterEndLine = new Label("后置字符");
        TextField afterEndTf = new TextField();
        afterEndTf.setPromptText("没有则不输入");
        line5.getChildren().addAll(afterlabel, afterPreLabel, afterPreTf, afterEndLine, afterEndTf);

        HBox line6 = new HBox();
        line6.setSpacing(10);
        line6.setAlignment(Pos.CENTER_LEFT);
        RadioButton encodeRadio = new RadioButton("URL编码");
        RadioButton decodeRadio = new RadioButton("URL解码");
        ToggleGroup encodeGroup = new ToggleGroup();
        encodeRadio.setSelected(true);
        encodeRadio.setToggleGroup(encodeGroup);
        decodeRadio.setToggleGroup(encodeGroup);
        line6.getChildren().addAll(encodeRadio, decodeRadio);

        Button execute = new Button("批量处理");
        TextArea ta = new TextArea();
        ta.setWrapText(true);

        // 配置选择不同数据源时，第二行显示的不同信息
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                String radioText = ((RadioButton) newValue).getText();
                line2.getChildren().clear();
                if (radioText.equals(onlyTextLineRadio.getText())) {
                    for (Node node : aList) {
                        line2.getChildren().add(node);
                    }
                    vBox.getChildren().remove(line3);
                } else if (radioText.equals(folderRadio.getText())) {
                    for (Node node : cList) {
                        line2.getChildren().add(node);
                    }
                    if (((RadioButton) oldValue).getText().equals(onlyTextLineRadio.getText())) {
                        vBox.getChildren().add(2, line3);
                    }
                } else {
                    for (Node node : bList) {
                        line2.getChildren().add(node);
                    }
                    if (((RadioButton) oldValue).getText().equals(onlyTextLineRadio.getText())) {
                        vBox.getChildren().add(2, line3);
                    }
                }

            }
        });

        // 配置选择不同文件格式时的触发事件
        fileType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                String text = ((RadioButton) newValue).getText();
                if (text.equals(excelRadio.getText())) {
                    // 显示输入框
                    tf.setVisible(true);
                } else {
                    // 不显示输入框
                    tf.setVisible(false);
                    tf.setText("");
                }
            }
        });

        // 批量处理按钮触发事件
        execute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ta.setText("");
                // 首行的数据源类型
                String toggleText = ((RadioButton) toggleGroup.getSelectedToggle()).getText();
                // 编译前的前置字符和后置字符
                String startPre = preTf.getText();
                String startEnd = endTf.getText();
                // 编译后的前置字符和后置字符
                String afterPre = afterPreTf.getText();
                String afterEnd = afterEndTf.getText();
                // 编译类型
                String encodeType = ((RadioButton) encodeGroup.getSelectedToggle()).getText();

                if (toggleText.equals(onlyTextLineRadio.getText())) {
                    // 如果为单个url转换
                    // 获取单个url
                    String oneUrl = startPre + ((TextField) aList.get(1)).getText() + startEnd;
                    String convertUrl = "";
                    if (encodeType.equals(encodeRadio.getText())) {
                        // 编码
                        try {
                            convertUrl = URLEncoder.encode(oneUrl, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 解码
                        try {
                            convertUrl = URLDecoder.decode(oneUrl, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    convertUrl = afterPre + convertUrl + afterEnd;
                    ta.setText(convertUrl);
                } else if (toggleText.equals(folderRadio.getText())) {
                    // 如果为文件夹下所有文件中url批量转换
                } else {

                }
            }
        });

        vBox.getChildren().addAll(line1, line2, line3, line4, line5, line6, execute, ta);
        anchorPane.getChildren().add(vBox);
        return anchorPane;
    }

    /**
     * url编码
     */
    private void encode(String sourceUrl) {
//        File file = new File("");
//        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
//        XSSFSheet sheetAt = workbook.getSheetAt(0);
//        int lastRowNum = sheetAt.getLastRowNum();
//        for (int i = 1; i <= lastRowNum; i++) {
//            XSSFRow row = sheetAt.getRow(i);
//            String stringCellValue = row.getCell(7).getStringCellValue();
//            System.out.println(stringCellValue);
//            System.out.println(URLEncoder.encode(stringCellValue, "utf-8"));
//        }
    }

    /**
     * 解码
     */
    private void decode() {

    }

}
