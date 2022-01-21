package org.example.interfaces.impl;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.example.common.Col;
import org.example.common.Common;
import org.example.common.MultiComboBox;
import org.example.util.Unit;
import org.example.button.BatchButton;
import org.example.interfaces.Function;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author mavenr
 * @Classname UrlConvret
 * @Description URL 编码转换，即编码和解码
 * @Date 2021/12/8 13:28
 */
public class UrlConvret implements Function {

    private Unit unit = new Unit();
    // 编译前在url后面追加的固定字符(在后置字符前)
    private static final String FIXEDAFTERFIRST = "fixedAfterFirst";
    // 编译后在url前面追加的固定字符(在前置字符前)
    private static final String AFTERFIXEDPRE = "afterFixedPre";

    @Override
    public String tabName() {
        return Common.TOP_BUTTON_5;
    }

    @Override
    public String tabStyle() {
        String style = "-fx-font-weight: bold; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-focus-color: transparent; -fx-text-base-color: white; " +
                "-fx-background-color: Pink;  -fx-pref-height: 40; ";
        return style;
    }

    @Override
    public AnchorPane tabPane(Stage primaryStage, double width, double h) {
        // 加载配置文件
        Properties properties = new Properties();
        String path = System.getProperty("user.dir");
        File configFile = new File(path + File.separator + "init.ini");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            properties.load(new FileInputStream(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        AnchorPane anchorPane = new AnchorPane();
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));

        // url数据源选择，默认为单个文件中url批量转换
        RadioButton onlyTextLineRadio = new RadioButton(Common.ONEURL);
        RadioButton oneFileRadio = new RadioButton(Common.ONEFILE);
        RadioButton folderRadio = new RadioButton(Common.ONEFOLDER);
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
        List<Node> cList = unit.chooseFolder(primaryStage, width, null);

        HBox line2 = new HBox();
        line2.setAlignment(Pos.CENTER_LEFT);
        line2.setSpacing(10);
        for (Node node : bList) {
            line2.getChildren().add(node);
        }

        // 文件格式选择
        RadioButton txtRadio = new RadioButton(Common.TXT);
        RadioButton excelRadio = new RadioButton(Common.EXCEL);
        excelRadio.setSelected(true);
        ToggleGroup fileType = new ToggleGroup();
        txtRadio.setToggleGroup(fileType);
        excelRadio.setToggleGroup(fileType);
        TextField tf = new TextField();
        tf.setPrefWidth(300);
        tf.setVisible(true);
        tf.setPromptText("请输入url在excel文件中的第几列");
        // 设置样式为下划线

        HBox line3 = new HBox();
        line3.setSpacing(10);
        line3.getChildren().addAll(txtRadio, excelRadio, tf);
        line3.setAlignment(Pos.CENTER_LEFT);

        // 选择要拷贝出来的列
        HBox line3After = new HBox();
        line3After.setSpacing(10);
        line3After.setAlignment(Pos.CENTER_LEFT);
        Label labelCopy = new Label("请选择需要拷贝出来的列（可多选）：");

        // 创建源文件中标题列的下拉列表
        MultiComboBox<Col> mcb = new MultiComboBox<>();
        // 设置样式

        ObservableList<Col> titles = FXCollections.observableArrayList();
        ComboBox<Col> comboBox = mcb.createComboBox(titles, width / 3);
        line3After.getChildren().addAll(labelCopy, comboBox);

        HBox line4Pre = new HBox();
        line4Pre.setSpacing(10);
        line4Pre.setAlignment(Pos.CENTER_LEFT);
        Label fixedPreAfterFirstLable = new Label("编译前在url后面追加的固定字符(在后置字符前)：");
        CheckBox fixedPreAfterFirstCb = new CheckBox("是否使用追加固定字符");
        fixedPreAfterFirstCb.setSelected(true);
        //---------------------------------------------------------
        String fixedAfterFirstParam = properties.getProperty(FIXEDAFTERFIRST);
        ComboBox<String> preFixes = new ComboBox<>();
        // 设置格式
        preFixes.setEditable(true);
        String[] splitParams = fixedAfterFirstParam.split(",");
        for (String p : splitParams) {
            if (StringUtils.isNotEmpty(p)) {
                preFixes.getItems().add(p);
            }
        }
        preFixes.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // combobox输入的值
                String newItem = preFixes.getEditor().getText();
                if (StringUtils.isNotEmpty(newItem) && !preFixes.getItems().contains(newItem)) {
                    preFixes.getItems().add(newItem);
                }

                // 将选项写入到配置文件
                String collect = preFixes.getItems().stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining(","));
                if (!collect.equals(fixedAfterFirstParam)) {
                    // 将参数写入到配置文件
                    try {
                        // 设置配置文件
                        properties.setProperty(FIXEDAFTERFIRST, collect);
                        properties.store(new FileOutputStream(configFile), "重写"+FIXEDAFTERFIRST+"参数");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // 删除按钮
        Button deleteButton = new Button("删除选中项");
        deleteButton.setStyle("-fx-background-radius: 4; -fx-background-color: #878787; -fx-text-fill: white;");
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // 移除选中的项
                String selected = preFixes.getEditor().getText();
                preFixes.getItems().remove(selected);
                // 将编辑中设置为空
                preFixes.getEditor().setText("");
                // 保存combobox中的选项写入到配置文件
                String collect = preFixes.getItems().stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining(","));
                if (!collect.equals(fixedAfterFirstParam)) {
                    // 将参数写入到配置文件
                    try {
                        // 设置配置文件
                        properties.setProperty(FIXEDAFTERFIRST, collect);
                        properties.store(new FileOutputStream(configFile), "重写"+FIXEDAFTERFIRST+"参数");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        line4Pre.getChildren().addAll(fixedPreAfterFirstLable, fixedPreAfterFirstCb, preFixes, deleteButton);

        HBox line4 = new HBox();
        line4.setSpacing(10);
        line4.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("编译前在url上追加的字符：");
        Label preLabel = new Label("前置字符");
        TextField preTf = new TextField();
        // 设置样式为下划线
        preTf.setPromptText("没有则不输入");
        Label endLine = new Label("后置字符");
        TextField endTf = new TextField();
        // 设置样式为下划线
        endTf.setPromptText("没有则不输入");
        line4.getChildren().addAll(label, preLabel, preTf, endLine, endTf);

        HBox line5Pre = new HBox();
        line5Pre.setSpacing(10);
        line5Pre.setAlignment(Pos.CENTER_LEFT);
        Label fixedAfterPreLabel = new Label("编译后在url前面追加的固定字符(在前置字符前)：");
        CheckBox fixedAfterPreCb = new CheckBox("是否使用追加固定字符");
        fixedAfterPreCb.setSelected(true);
        TextField fixedAfterPreTf = new TextField();
        // 设置样式为下划线
        String param = properties.getProperty(AFTERFIXEDPRE);
        // 加载配置文件中的参数
        if (StringUtils.isNotEmpty(param)) {
            fixedAfterPreTf.setText(param);
        }
        line5Pre.getChildren().addAll(fixedAfterPreLabel, fixedAfterPreCb, fixedAfterPreTf);
        String text = fixedAfterPreTf.getText();
        // 失去焦点触发保存事件
        fixedAfterPreTf.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // 判断内容改变，则保存内容
                if (!text.equals(fixedAfterPreTf.getText())) {
                    // 设置配置文件
                    properties.setProperty(AFTERFIXEDPRE, fixedAfterPreTf.getText());
                    try {
                        // 将参数写入到配置文件
                        properties.store(new FileOutputStream(configFile), "重写"+AFTERFIXEDPRE+"参数");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        HBox line5 = new HBox();
        line5.setSpacing(10);
        line5.setAlignment(Pos.CENTER_LEFT);
        Label afterlabel = new Label("编译后在url上追加的字符：");
        Label afterPreLabel = new Label("前置字符");
        TextField afterPreTf = new TextField();
        // 设置样式为下划线
        afterPreTf.setPromptText("没有则不输入");
        Label afterEndLine = new Label("后置字符");
        TextField afterEndTf = new TextField();
        // 设置样式为下划线
        afterEndTf.setPromptText("没有则不输入");
        line5.getChildren().addAll(afterlabel, afterPreLabel, afterPreTf, afterEndLine, afterEndTf);

        HBox line6 = new HBox();
        line6.setSpacing(10);
        line6.setAlignment(Pos.CENTER_LEFT);
        RadioButton encodeRadio = new RadioButton(Common.ENCODE);
        RadioButton decodeRadio = new RadioButton(Common.DECODE);
        ToggleGroup encodeGroup = new ToggleGroup();
        encodeRadio.setSelected(true);
        encodeRadio.setToggleGroup(encodeGroup);
        decodeRadio.setToggleGroup(encodeGroup);
        line6.getChildren().addAll(encodeRadio, decodeRadio);

        // 批量处理按钮
        BatchButton button = new BatchButton();
        Button execute = button.createInstance();

        // 页面中的文本域
        TextArea ta = new TextArea();
        ta.setWrapText(true);

        // 配置选择不同数据源时，第二行显示的不同信息
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                // 清理多选框
                comboBox.getItems().clear();

                // 清空文本框中的值
                ((TextField) bList.get(1)).clear();
                ((TextField) cList.get(1)).clear();

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
                // 清理多选框
                comboBox.getItems().clear();

                String text = ((RadioButton) newValue).getText();
                if (text.equals(excelRadio.getText())) {
                    // 显示输入框
                    tf.setVisible(true);

                    File file = null;
                    // 判断源文件格式
                    String sourceType = ((RadioButton) toggleGroup.getSelectedToggle()).getText();
                    if (Common.ONEFILE.equals(sourceType)) {
                        // 获取文件路径
                        String filePath = ((TextField) bList.get(1)).getText();
                        if (StringUtils.isEmpty(filePath)) {
                            ta.setText("请选择源文件");
                            return;
                        }
                        if (!filePath.endsWith(".xls") && !filePath.endsWith(".xlsx")) {
                            ta.setText("源文件格式错误");
                            return;
                        }
                        file = new File(filePath);
                    } else if (Common.ONEFOLDER.equals(sourceType)) {
                        String folderPath = ((TextField) cList.get(1)).getText();
                        if (StringUtils.isEmpty(folderPath)) {
                            ta.setText("请选择源文件夹");
                            return;
                        }
                        File[] files = new File(folderPath).listFiles();
                        if (files.length == 0) {
                            ta.setText("源文件夹下未查询到文件");
                            return;
                        }
                        file = files[0];
                        if (!file.getName().endsWith(".xls") && !file.getName().endsWith(".xlsx")) {
                            ta.setText("源文件夹中文件的格式错误");
                            return;
                        }
                    }

                    try {
                        // 获取excel的第一行表头
                        XSSFWorkbook workbook = new XSSFWorkbook(file);
                        XSSFSheet sheet = workbook.getSheetAt(0);
                        XSSFRow row = sheet.getRow(0);
                        int size = row.getLastCellNum();

                        ObservableList<Col> objects = FXCollections.observableArrayList();
                        for (int i = 0; i < size; i++) {
                            String cellValue = getCellValue(row, i);
                            if (StringUtils.isNotEmpty(cellValue)) {
                                // 添加到下拉框中
                                Col col = new Col();
                                col.setName(cellValue);
                                col.setIndex(i);
                                objects.add(col);
                            }
                        }

                        comboBox.getItems().clear();
                        comboBox.setItems(objects);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InvalidFormatException e) {
                        e.printStackTrace();
                    }


                } else {
                    // 不显示输入框
                    tf.setVisible(false);
                    tf.setText("");
                }
            }
        });

        // 监听文本输入框
        ((TextField) bList.get(1)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // 判断选中的数据源是单个文件，且文件格式选择的是excel格式
                if (Common.ONEFILE.equals(((RadioButton)toggleGroup.getSelectedToggle()).getText())
                        && Common.EXCEL.equals(((RadioButton)fileType.getSelectedToggle()).getText())) {
                    if (!newValue.endsWith(".xls") && !newValue.endsWith(".xlsx")) {
                        ta.setText("请选择xls或xlsx后缀格式的文件");
                        return;
                    } else {
                        File file = new File(newValue);
                        try {
                            // 获取excel的第一行表头
                            XSSFWorkbook workbook = new XSSFWorkbook(file);
                            XSSFSheet sheet = workbook.getSheetAt(0);
                            XSSFRow row = sheet.getRow(0);
                            int size = row.getLastCellNum();
                            ObservableList<Col> objects = FXCollections.observableArrayList();
                            for (int i = 0; i < size; i++) {
                                String cellValue = getCellValue(row, i);
                                if (StringUtils.isNotEmpty(cellValue)) {
                                    // 添加到下拉框中
                                    Col col = new Col();
                                    col.setName(cellValue);
                                    col.setIndex(i);
                                    objects.add(col);
                                }
                            }
                            comboBox.getItems().clear();
                            comboBox.setItems(objects);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InvalidFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        // 监听文本输入框
        ((TextField) cList.get(1)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // 判断选中的数据源是单个文件，且文件格式选择的是excel格式
                if (Common.ONEFOLDER.equals(((RadioButton)toggleGroup.getSelectedToggle()).getText())
                        && Common.EXCEL.equals(((RadioButton)fileType.getSelectedToggle()).getText())) {
                    File file;
                    File[] files = new File(newValue).listFiles();
                    if (files.length == 0) {
                        ta.setText("源文件夹下未查询到文件");
                        return;
                    }
                    file = files[0];
                    if (!file.getName().endsWith(".xls") && !file.getName().endsWith(".xlsx")) {
                        ta.setText("源文件夹中文件的格式错误");
                        return;
                    }
                    try {
                        // 获取excel的第一行表头
                        XSSFWorkbook workbook = new XSSFWorkbook(file);
                        XSSFSheet sheet = workbook.getSheetAt(0);
                        XSSFRow row = sheet.getRow(0);
                        int size = row.getLastCellNum();
                        ObservableList<Col> objects = FXCollections.observableArrayList();
                        for (int i = 0; i < size; i++) {
                            String cellValue = getCellValue(row, i);
                            if (StringUtils.isNotEmpty(cellValue)) {
                                // 添加到下拉框中
                                Col col = new Col();
                                col.setName(cellValue);
                                col.setIndex(i);
                            }
                        }
                        comboBox.getItems().clear();
                        comboBox.setItems(objects);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InvalidFormatException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        // 批量处理按钮触发事件
        execute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // 获取下拉框中的选项
                String fixedPreAfterFirst = preFixes.getEditor().getText();
                System.out.println("下拉框中输入的值: " + fixedPreAfterFirst);

                // 获取要拷贝出来的列
                System.out.println("获取到了选中的列名：" + comboBox.getButtonCell().getText());
                ObservableList<Col> items = comboBox.getItems();
                List<Col> cols = new ArrayList<>();
                for (Col c : items) {
                    if (c.isSelected()) {
                        // 选中的列信息
                        cols.add(c);
                    }
                }

                // 设置配置文件
                try {
                    //
                    properties.store(new FileOutputStream(configFile), "重写"+AFTERFIXEDPRE+"参数");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ta.setText("");
                // 首行的数据源类型
                String toggleText = ((RadioButton) toggleGroup.getSelectedToggle()).getText();
                // 编译前的前置字符和后置字符
                String startPre = preTf.getText();
                String startEnd = endTf.getText();
                // 编译后的前置字符和后置字符
                String afterPre = afterPreTf.getText();
                String afterEnd = afterEndTf.getText();
                // 正则去掉字符串中utf-8字符编码的空格
                byte[] bytes = new byte[]{(byte) 0xC2, (byte) 0xA0};
                Pattern p = Pattern.compile(new String(bytes, StandardCharsets.UTF_8));
                Matcher m;
                m = p.matcher(startPre);
                startPre = m.replaceAll("");
                m = p.matcher(startEnd);
                startEnd = m.replaceAll("");
                m = p.matcher(afterPre);
                afterPre = m.replaceAll("");
                m = p.matcher(afterEnd);
                afterEnd = m.replaceAll("");
                startPre = startPre.trim();
                startEnd = startEnd.trim();
                afterPre = afterPre.trim();
                afterEnd = afterEnd.trim();
                System.out.println("startPre=>>>" + startPre + "<<<");
                System.out.println("startEnd=>>>" + startEnd + "<<<");
                System.out.println("afterPre=>>>" + afterPre + "<<<");
                System.out.println("afterEnd=>>>" + afterEnd + "<<<");
                // 编译类型
                String encodeType = ((RadioButton) encodeGroup.getSelectedToggle()).getText();
                // 文件格式
                String type = ((RadioButton) fileType.getSelectedToggle()).getText();
                // 获取是否在编译前的url后面追加固定字符（在后置字符前），以及固定字符本身
                boolean preEncodeAfterFirstSelected = fixedPreAfterFirstCb.isSelected();
                String preEncodeAfterFirstText = fixedPreAfterFirst;
                System.out.println("编译前固定 " + preEncodeAfterFirstText);

                // 获取是否在编译后的url前面追加固定字符（在前置字符前），以及固定字符本身
                boolean selected = fixedAfterPreCb.isSelected();
                String fixedAfterPre = fixedAfterPreTf.getText();

                // excel中url所在的列数
                int colIndex = -1;
                if (excelRadio.getText().equals(type) && !onlyTextLineRadio.getText().equals(toggleText)) {
                    if (StringUtils.isEmpty(tf.getText().trim())) {
                        ta.setText("请输入url在excel文件中的第几列");
                        return;
                    }
                    colIndex = Integer.parseInt(tf.getText().trim()) - 1;
                }

                if (toggleText.equals(onlyTextLineRadio.getText())) {
                    // 如果为单个url转换
                    // 获取单个url
                    String oneUrl = startPre + ((TextField) aList.get(1)).getText();
                    if (preEncodeAfterFirstSelected) {
                        oneUrl += preEncodeAfterFirstText;
                    }
                    oneUrl += startEnd;
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
                    if (selected) {
                        convertUrl = fixedAfterPre + convertUrl;
                    }
                    ta.setText(convertUrl);
                } else if (toggleText.equals(folderRadio.getText())) {
                    // 如果为文件夹下所有文件中url批量转换
                    // 获取文件夹路径
                    String folderPath = ((TextField) cList.get(1)).getText();
                    // 获取所有文件
                    File folder = new File(folderPath);
                    File[] files = folder.listFiles();
                    batchFilesAndExportToExcel(files, type, colIndex, cols, startPre, startEnd, afterPre,
                            afterEnd, encodeType, ta, preEncodeAfterFirstSelected, preEncodeAfterFirstText, selected, fixedAfterPre);
                } else {
                    // 如果为文件中url批量转换
                    String filePath = ((TextField) bList.get(1)).getText();
                    File file = new File(filePath);
                    File[] files = new File[]{file};
                    batchFilesAndExportToExcel(files, type, colIndex, cols, startPre, startEnd, afterPre,
                            afterEnd, encodeType, ta, preEncodeAfterFirstSelected, preEncodeAfterFirstText, selected, fixedAfterPre);
                }
            }
        });

        vBox.getChildren().addAll(line1, line2, line3, line3After, line4Pre, line4, line5Pre, line5, line6, execute, ta);
        anchorPane.getChildren().add(vBox);
        return anchorPane;
    }

    /**
     * 批量处理文件，并导出到excel文件中
     * @param files
     * @param fileType
     * @param colIndex
     * @param cols
     * @param startPre
     * @param startEnd
     * @param afterPre
     * @param afterEnd
     * @param encodeType
     */
    private void batchFilesAndExportToExcel(File[] files, String fileType, int colIndex, List<Col> cols,
                                            String startPre, String startEnd, String afterPre, String afterEnd,
                                            String encodeType, TextArea ta, boolean preEncodeFlag, String preEncodeFixed,
                                            boolean fixedFlag, String fixed) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date now = new Date();

        System.out.println("编译前的url后面追加固定字符（在后置字符前）" + preEncodeFlag + " 固定" + preEncodeFixed);
        System.out.println("编译后在url前面追加的固定字符(在前置字符前) " + fixedFlag + " 固定" + fixed);
        // 获取文件路径
        String path = files[0].getPath();
        // 结果文件路径
        String newPath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "result-" + sdf.format(now) + ".xlsx";

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("URL转换结果");
        // 冻结首行
        sheet.createFreezePane(0, 1);
        XSSFRow row = sheet.createRow(0);

        // 设置格式
        XSSFCellStyle cs = workbook.createCellStyle();
        // 设置填充方式和背景颜色
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setFillForegroundColor(new XSSFColor(new Color(153, 153, 153)));
        // 设置居中方式
        cs.setAlignment(HorizontalAlignment.CENTER);

        // 遍历选中的行
        int count = -1;
        for (Col c : cols) {
            count++;
            XSSFCell cell = row.createCell(count);
            cell.setCellValue(c.getName());
            cell.setCellStyle(cs);
        }
        XSSFCell cell1 = row.createCell(count + 1);
        cell1.setCellValue("原URL");
        XSSFCell cell2 = row.createCell(count + 2);
        cell2.setCellValue("处理后的URL");

        cell1.setCellStyle(cs);
        cell2.setCellStyle(cs);

        int num = 0;
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(newPath);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage().contains("另一个程序正在使用此文件，进程无法访问"));
            if (e.getMessage().contains("另一个程序正在使用此文件，进程无法访问")) {
                ta.setText(e.getMessage());
            }
            return;
        }
        // 判断文件格式
        if (Common.TXT.equals(fileType)) {
            // 读取文件，遍历行
            for (File f : files) {
                if (f.getPath().equals(newPath)) {
                    continue;
                }
                // 读取文件
                FileInputStream inputStream = null;
                InputStreamReader inputStreamReader = null;
                BufferedReader br = null;
                try {
                    inputStream = new FileInputStream(f);
                    inputStreamReader = new InputStreamReader(inputStream);
                    br = new BufferedReader(inputStreamReader);
                    String line = "";
                    if (Common.DECODE.equals(encodeType)) {
                        while ((line = br.readLine()) != null) {
                            if (StringUtils.isEmpty(line)) {
                                continue;
                            }
                            num++;
                            String source = startPre + line;
                            if (preEncodeFlag) {
                                source += preEncodeFixed;
                            }
                            source += startEnd;
                            String decode = decode(source);
                            String result = afterPre + decode + afterEnd;
                            if (fixedFlag) {
                                result = fixed + result;
                            }
                            // 写入excel
                            XSSFRow rowNew = sheet.createRow(num);
                            XSSFCell sourceCell = rowNew.createCell(0);
                            sourceCell.setCellValue(line);
                            XSSFCell targetCell = rowNew.createCell(1);
                            targetCell.setCellValue(result);

                        }
                    } else {
                        while ((line = br.readLine()) != null) {
                            num++;
                            if (StringUtils.isEmpty(line)) {
                                continue;
                            }
                            String source = startPre + line;
                            if (preEncodeFlag) {
                                source += preEncodeFixed;
                            }
                            source += startEnd;
                            String encode = encode(source);
                            String result = afterPre + encode + afterEnd;
                            if (fixedFlag) {
                                result = fixed + result;
                            }
                            // 写入excel
                            XSSFRow rowNew = sheet.createRow(num);
                            XSSFCell sourceCell = rowNew.createCell(0);
                            sourceCell.setCellValue(line);
                            XSSFCell targetCell = rowNew.createCell(1);
                            targetCell.setCellValue(result);

                        }
                    }
                    br.close();
                    inputStreamReader.close();
                    inputStream.close();
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("另一个程序正在使用此文件，进程无法访问")) {
                        ta.setText(e.getMessage());
                        return;
                    }
                    try {
                        if (br != null) {
                            br.close();
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (workbook != null) {
                            workbook.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    return;
                }
                num++;
            }
        } else if (Common.EXCEL.equals(fileType)){
            // 读取文件，遍历单元格
            for (File f : files) {
                if (f.getPath().equals(newPath)) {
                    continue;
                }
                // 读取文件
                FileInputStream inputStream = null;
                XSSFWorkbook source = null;
                try {
                    inputStream = new FileInputStream(f);
                    source = new XSSFWorkbook(inputStream);
                    XSSFSheet sheet0 = source.getSheetAt(0);
                    int rowNum = sheet0.getLastRowNum() + 1;
                    if (Common.DECODE.equals(encodeType)) {
                        for (int i = 1; i < rowNum; i++) {
                            XSSFRow rowi = sheet0.getRow(i);
                            if (rowi == null) {
                                continue;
                            }
                            XSSFCell cell = rowi.getCell(colIndex);
                            if (cell == null) {
                                continue;
                            }

                            String sourceUrl = cell.getStringCellValue();
                            if (StringUtils.isEmpty(sourceUrl)) {
                                continue;
                            }
                            num++;
                            String sourceU = startPre + sourceUrl;
                            if (preEncodeFlag) {
                                sourceU += preEncodeFixed;
                            }
                            sourceU += startEnd;
                            String decode = decode(sourceU);
                            String result = afterPre + decode + afterEnd;
                            if (fixedFlag) {
                                result = fixed + result;
                            }
                            // 写入excel
                            XSSFRow rowNew = sheet.createRow(num);
                            int max = -1;
                            for (Col c : cols) {
                                max++;
                                int index = c.getIndex();
                                XSSFCell gnCell = rowNew.createCell(max);
                                String gn = getCellValue(rowi, index);
                                gnCell.setCellValue(gn);
                            }
                            XSSFCell sourceCell = rowNew.createCell(max + 1);
                            sourceCell.setCellValue(sourceUrl);
                            XSSFCell targetCell = rowNew.createCell(max + 2);
                            targetCell.setCellValue(result);
                        }
                    } else {
                        for (int i = 1; i < rowNum; i++) {
                            XSSFRow rowi = sheet0.getRow(i);
                            if (rowi == null) {
                                continue;
                            }
                            XSSFCell cell = rowi.getCell(colIndex);
                            if (cell == null) {
                                continue;
                            }
                            String sourceUrl = cell.getStringCellValue();
                            if (StringUtils.isEmpty(sourceUrl)) {
                                continue;
                            }
                            num++;
                            String sourceU = startPre + sourceUrl;
                            if (preEncodeFlag) {
                                sourceU += preEncodeFixed;
                            }
                            sourceU += startEnd;
                            String encode = encode(sourceU);
                            String result = afterPre + encode + afterEnd;
                            if (fixedFlag) {
                                result = fixed + result;
                            }
                            // 写入excel
                            XSSFRow rowNew = sheet.createRow(num);
                            int max = -1;
                            for (Col c : cols) {
                                max++;
                                int index = c.getIndex();
                                XSSFCell gnCell = rowNew.createCell(max);
                                String gn = getCellValue(rowi, index);
                                gnCell.setCellValue(gn);
                            }
                            XSSFCell sourceCell = rowNew.createCell(max + 1);
                            sourceCell.setCellValue(sourceUrl);
                            XSSFCell targetCell = rowNew.createCell(max + 2);
                            targetCell.setCellValue(result);

                        }
                    }
                    source.close();
                    inputStream.close();
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("另一个程序正在使用此文件，进程无法访问")) {
                        ta.setText(e.getMessage());
                        return;
                    }
                    try {
                        if (source != null) {
                            source.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (workbook != null) {
                            workbook.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    return;
                }
                num++;
            }
        }
        ta.setText("批处理完成，结果文件：" + newPath);
        try {
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * url编码，默认编码格式 utf-8
     * @param sourceUrl 要编码的字符串
     * @return
     */
    private String encode(String sourceUrl) {
        try {
            return URLEncoder.encode(sourceUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 解码，默认编码格式 utf-8
     * @param sourceUrl 要解码的字符串
     * @return
     */
    private String decode(String sourceUrl) {
        try {
            return URLDecoder.decode(sourceUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取单元格的值，并转换为String
     * @param row
     * @param colIndex
     * @return
     */
    private String getCellValue(XSSFRow row, int colIndex) {
        XSSFCell cell = row.getCell(colIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK
                || cell.getCellType() == CellType._NONE || cell.getCellType() == CellType.ERROR || colIndex < 0) {
            return "";
        } else if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            boolean booleanCellValue = cell.getBooleanCellValue();
            return String.valueOf(booleanCellValue);
        } else if (cell.getCellType() == CellType.NUMERIC) {
            double numericCellValue = cell.getNumericCellValue();
            // 去掉double末尾的0
            BigDecimal bd = new BigDecimal(numericCellValue);
            BigDecimal noZeroBigDecimal = bd.stripTrailingZeros();
            // 不使用科学计数法
            String value = noZeroBigDecimal.toPlainString();
            return String.valueOf(value);
        } else if (cell.getCellType() == CellType.FORMULA) {
            return cell.getStringCellValue();
        } else {
            return "";
        }

    }

}