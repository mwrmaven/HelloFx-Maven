package org.example.interfaces.impl;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.example.common.Col;
import org.example.common.Common;
import org.example.common.MultiComboBox;
import org.example.init.Config;
import org.example.util.Unit;
import org.example.button.BatchButton;
import org.example.interfaces.Function;

import java.io.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author mavenr
 * @Classname UrlConvret
 * @Description URL 编码转换，即编码和解码
 * @Date 2021/12/8 13:28
 */
public class UrlConvert implements Function {

    private Unit unit = new Unit();
    // 编译前在url后面追加的固定字符(在后置字符前)
    private static final String FIXEDAFTERFIRST = "fixedAfterFirst";
    // 编译后在url前面追加的固定字符(在前置字符前)
    private static final String AFTERFIXEDPRE = "afterFixedPre";
    // 生成URL的基础URL模板
    private static final String URLTEMPLATE = "urlTemplate";

    private static final Pattern pattern = Pattern.compile("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}");

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private static final String OR = "或";
    private static final String AND = "并且";

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public String tabName() {
        return Common.TOP_BUTTON_5;
    }

    @Override
    public String tabStyle() {
        String style = "-fx-font-weight: bold; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-focus-color: transparent; -fx-text-base-color: white; " +
                "-fx-background-color: #ec693f;  -fx-pref-height: 40; ";
        return style;
    }

    @Override
    public AnchorPane tabPane(Stage primaryStage, double width, double h) {
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

        // 在获取文件后添加按钮
        Button fetchNumButton = new Button("提取货号");
        fetchNumButton.setPrefWidth(100);
        fetchNumButton.setAlignment(Pos.CENTER);
        // 设置button样式
        String style = "-fx-background-color: #ffcc33; -fx-background-radius: 4";
        fetchNumButton.setStyle(style);
        bList.add(fetchNumButton);
        // 添加label区域
        Label fetchNumLabel = new Label();
        bList.add(fetchNumLabel);
        // 设置点击事件
        fetchNumButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // 获取文件后提取货号
                createFileAndFetchNumByButton(bList, primaryStage);
            }
        });

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
        // 设置样式为下划线

        HBox line3 = new HBox();
        line3.setSpacing(10);
        line3.getChildren().addAll(txtRadio, excelRadio);
        line3.setAlignment(Pos.CENTER_LEFT);

        RadioButton fixCol = new RadioButton(Common.FIXCOL);
        RadioButton fromCol = new RadioButton(Common.FROMCOL);
        fromCol.setSelected(true);
        ToggleGroup colType = new ToggleGroup();
        fixCol.setToggleGroup(colType);
        fromCol.setToggleGroup(colType);

        TextField tf = new TextField();
        tf.setPrefWidth(300);
        tf.setVisible(true);
        tf.setPromptText("请输入url或指定列在excel文件中的第几列");

        HBox line3_1 = new HBox();
        line3_1.setSpacing(10);
        line3_1.setAlignment(Pos.CENTER_LEFT);
        line3_1.getChildren().addAll(fixCol, fromCol, tf);

        Label baseWordLabel = new Label("生成URL的基础URL模板：");

        String urlTemplateList = Config.get(URLTEMPLATE);
        ComboBox<String> baseWordText = new ComboBox<>();
        // 设置格式
        baseWordText.setEditable(true);
        baseWordText.setPrefWidth(width / 2 - 200);
        baseWordText.setPromptText("指定列的参数所在位置使用【para】代替，时间使用【date】代替，例如 http://www.【para】.com？date=【date】");
        if (StringUtils.isNotBlank(urlTemplateList)) {
            String[] splitBaseWordTextParams = urlTemplateList.split(";");
            for (String p : splitBaseWordTextParams) {
                if (StringUtils.isNotEmpty(p)) {
                    baseWordText.getItems().add(p);
                }
            }
        }

        baseWordText.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // combobox输入的值
                String newItem = baseWordText.getEditor().getText();
                if (StringUtils.isNotEmpty(newItem) && !baseWordText.getItems().contains(newItem)) {
                    baseWordText.getItems().add(newItem);
                }

                // 将选项写入到配置文件
                String collect = baseWordText.getItems().stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining(";"));
                if (!collect.equals(urlTemplateList)) {
                    // 将参数写入到配置文件
                    Config.set(URLTEMPLATE, collect);
                }
            }
        });

        // 删除按钮
        Button deleteURLTemplateButton = new Button("删除选中项");
        deleteURLTemplateButton.setStyle("-fx-background-radius: 4; -fx-background-color: #878787; -fx-text-fill: white;");
        deleteURLTemplateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // 移除选中的项
                String selected = baseWordText.getEditor().getText();
                baseWordText.getItems().remove(selected);
                // 将编辑中设置为空
                baseWordText.getEditor().setText("");
                // 保存combobox中的选项写入到配置文件
                String collect = baseWordText.getItems().stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining(";"));
                if (!collect.equals(urlTemplateList)) {
                    // 将参数写入到配置文件
                    Config.set(URLTEMPLATE, collect);
                }
            }
        });

        HBox line3_2 = new HBox();
        line3_2.setSpacing(10);
        line3_2.setAlignment(Pos.CENTER_LEFT);
        line3_2.getChildren().addAll(baseWordLabel, baseWordText, deleteURLTemplateButton);

        Label dateLabel = new Label("选择URL中的日期：");
        DatePicker dp = new DatePicker();
        HBox line3_3 = new HBox();
        line3_3.setSpacing(10);
        line3_3.setAlignment(Pos.CENTER_LEFT);
        line3_3.getChildren().addAll(dateLabel, dp);


        // 创建源文件中标题列的下拉列表
        MultiComboBox<Col> mcb = new MultiComboBox<>();

        // 选择要拷贝出来的列
        HBox line3After = new HBox();
        line3After.setSpacing(10);
        line3After.setAlignment(Pos.CENTER_LEFT);
        Label labelCopy = new Label("请选择需要拷贝出来的列（可多选）：");
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
        String fixedAfterFirstParam = Config.get(FIXEDAFTERFIRST);
        ComboBox<String> preFixes = new ComboBox<>();
        // 设置格式
        preFixes.setEditable(true);
        if (StringUtils.isNotBlank(fixedAfterFirstParam)) {
            String[] splitParams = fixedAfterFirstParam.split(",");
            for (String p : splitParams) {
                if (StringUtils.isNotEmpty(p)) {
                    preFixes.getItems().add(p);
                }
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
                    Config.set(FIXEDAFTERFIRST, collect);
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
                    Config.set(FIXEDAFTERFIRST, collect);
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
        String param = Config.get(AFTERFIXEDPRE);
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
                    Config.set(AFTERFIXEDPRE, fixedAfterPreTf.getText());
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

        // 获取文本模板
        List<Node> nodes = unit.chooseFile(primaryStage, width, "选择模板文件");
        ((Button) nodes.get(0)).setPrefWidth(120);
        HBox template = new HBox();
        template.setSpacing(10);
        template.setAlignment(Pos.CENTER_LEFT);
        for (Node n : nodes) {
            template.getChildren().add(n);
        }

        // 批量处理按钮
        BatchButton button = new BatchButton();
        Button execute = button.createInstance();

        // 页面中的文本域
        TextArea ta = new TextArea();
        // 删除水平滚动条 TODO
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
                    vBox.getChildren().remove(line3_1);
                    vBox.getChildren().remove(line3_2);
                    vBox.getChildren().remove(line3_3);
                    vBox.getChildren().remove(line3After);
                } else if (radioText.equals(folderRadio.getText())) {
                    // 清空“请输入单个url”
                    TextField tf = (TextField) aList.get(1);
                    tf.clear();
                    for (Node node : cList) {
                        line2.getChildren().add(node);
                    }
                    if (((RadioButton) oldValue).getText().equals(onlyTextLineRadio.getText())) {
                        vBox.getChildren().add(2, line3);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3) + 1, line3_1);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_1) + 1, line3_2);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_2) + 1, line3_3);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_3) + 1, line3After);
                    }
                } else {
                    // 清空“请输入单个url”
                    TextField tf = (TextField) aList.get(1);
                    tf.clear();
                    for (Node node : bList) {
                        line2.getChildren().add(node);
                    }
                    if (((RadioButton) oldValue).getText().equals(onlyTextLineRadio.getText())) {
                        vBox.getChildren().add(2, line3);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3) + 1, line3_1);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_1) + 1, line3_2);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_2) + 1, line3_3);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_3) + 1, line3After);
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

                    if (((RadioButton)colType.getSelectedToggle()).getText().equalsIgnoreCase(fromCol.getText())) {
                        // 需要拷贝出来的列 和 是否指定url列
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3) + 1, line3_1);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_1) + 1, line3_2);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_2) + 1, line3_3);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_3) + 1, line3After);

                    } else {
                        // 需要拷贝出来的列 和 是否指定url列
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3) + 1, line3_1);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_1) + 1, line3After);
                    }

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
                    // 需要拷贝出来的列 和 是否指定url列
                    tf.setText("");
                    vBox.getChildren().remove(line3_1);
                    vBox.getChildren().remove(line3_2);
                    vBox.getChildren().remove(line3_3);
                    vBox.getChildren().remove(line3After);
                }
            }
        });

        colType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (fromCol.getText().equals(((RadioButton) newValue).getText())) {
                    vBox.getChildren().add(vBox.getChildren().indexOf(line3After), line3_2);
                    vBox.getChildren().add(vBox.getChildren().indexOf(line3After), line3_3);
                } else {
                    vBox.getChildren().remove(line3_2);
                    vBox.getChildren().remove(line3_3);
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
                    } else {
                        System.out.println("获取到文件路径：" + newValue);
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
        });

        // 批量处理按钮触发事件
        execute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                long start = System.currentTimeMillis();
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

                String templateFilePath = ((TextField) nodes.get(1)).getText();

                // 获取选中的日期
                LocalDate pointDate = dp.getValue();

                // 获取生成URL的基础URL模板的下拉框中的选项
                String baseWordValue = baseWordText.getEditor().getText();

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
                            afterEnd, encodeType, ta, preEncodeAfterFirstSelected, preEncodeAfterFirstText,
                            selected, fixedAfterPre, templateFilePath, ((RadioButton) colType.getSelectedToggle()).getText(), baseWordValue, pointDate);
                } else {
                    // 如果为文件中url批量转换
                    String filePath = ((TextField) bList.get(1)).getText();
                    File file = new File(filePath);
                    File[] files = new File[]{file};
                    batchFilesAndExportToExcel(files, type, colIndex, cols, startPre, startEnd, afterPre,
                            afterEnd, encodeType, ta, preEncodeAfterFirstSelected, preEncodeAfterFirstText,
                            selected, fixedAfterPre, templateFilePath, ((RadioButton) colType.getSelectedToggle()).getText(), baseWordValue, pointDate);
                }
                System.out.println("处理耗时：" + (System.currentTimeMillis() - start) / 1000 + " 秒");
            }
        });

        vBox.getChildren().addAll(line1, line2, line3, line3_1, line3_2, line3_3, line3After, line4Pre, line4, line5Pre, line5, line6, template, execute, ta);
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
                                            boolean fixedFlag, String fixed, String templateFilePath, String colType, String baseWordValue, LocalDate pointDate) {


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
        cs.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 153, (byte) 153, (byte) 153}, new DefaultIndexedColorMap()));
        // 设置居中方式
        cs.setAlignment(HorizontalAlignment.CENTER);

        // 设置报错的格式
        XSSFCellStyle errorStyle = workbook.createCellStyle();
        // 设置填充方式和北京颜色
        errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        errorStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 255, (byte) 0, (byte) 0}, new DefaultIndexedColorMap()));

        // 设置间隔的浅灰色背景
        XSSFCellStyle splitStyle = workbook.createCellStyle();
        // 设置填充方式和北京颜色
        splitStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        splitStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 211, (byte) 211, (byte) 211}, new DefaultIndexedColorMap()));


        // 遍历选中的行
        int count = -1;
        for (Col c : cols) {
            count++;
            XSSFCell cell = row.createCell(count);
            cell.setCellValue(c.getName());
            cell.setCellStyle(cs);
            sheet.setColumnWidth(count, 10 * 256);
        }
        XSSFCell cellPre1 = row.createCell(count + 1);
        cellPre1.setCellValue("网页价格");
        sheet.setColumnWidth(count + 1, 10 * 256);
        XSSFCell cellPre2 = row.createCell(count + 2);
        cellPre2.setCellValue("小程序价格");
        sheet.setColumnWidth(count + 2, 10 * 256);
        XSSFCell cellPre3 = row.createCell(count + 3);
        cellPre3.setCellValue("会员价格");
        sheet.setColumnWidth(count + 3, 10 * 256);

        XSSFCell cell1 = row.createCell(count + 4);
        cell1.setCellValue("原URL");
        sheet.setColumnWidth(count + 4, 60 * 256);
        XSSFCell cell2 = row.createCell(count + 5);
        cell2.setCellValue("处理后的URL");
        sheet.setColumnWidth(count + 5, 60 * 256);

        cellPre1.setCellStyle(cs);
        cellPre2.setCellStyle(cs);
        cellPre3.setCellStyle(cs);
        cell1.setCellStyle(cs);
        cell2.setCellStyle(cs);

        int num = 0;

        List<XWPFParagraph> docxParagraphs = new ArrayList<>();
        if (StringUtils.isNotBlank(templateFilePath)) {
            // 获取模板文件中的段落
            File file = new File(templateFilePath);
            List<Paragraph> docParagraphs = new ArrayList<>();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                if (StringUtils.isNotEmpty(templateFilePath) && templateFilePath.endsWith(".doc")) {
                    try {
                        HWPFDocument doc = new HWPFDocument(fis);
                        Range range = doc.getRange();
                        for (int i = 0; i < range.numParagraphs(); i++) {
                            docParagraphs.add(range.getParagraph(i));
                        }
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
                } else if (StringUtils.isNotEmpty(templateFilePath) && templateFilePath.endsWith(".docx")) {
                    try {
                        XWPFDocument docx = new XWPFDocument(fis);
                        docxParagraphs = docx.getParagraphs();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (StringUtils.isNotEmpty(templateFilePath)){
                    ta.setText("当前模板文件只支持doc、docx格式");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 用于获取转换成doc、docx格式文件的内容
        List<XWPFParagraph> newParagraphs = new ArrayList<>();
        XWPFDocument newDocument = new XWPFDocument();
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
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    return;
                }
                num++;
            }
        } else if (Common.EXCEL.equals(fileType)){
            // 请求商品网页信息的准备
            CookieStore cookieStore = new BasicCookieStore();
            HttpClientBuilder builder = HttpClientBuilder.create().setDefaultCookieStore(cookieStore);
            HttpClient client = builder.build();
            String sid = "";
            try {
                sid = getSID(client, cookieStore);
            } catch (IOException e) {
                System.out.println("未查询到请求网址所需的SID");
            }

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
                    Map<String, Col> nameAndCol = cols.stream().collect(Collectors.toMap(Col::getName, item -> item, (a, b) -> a));
                    Set<String> colNames = nameAndCol.keySet();
                    Map<String, String> nameAndValue = new HashMap<>();
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

                            String sourceUrl = unit.getCellValue(cell);
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
                            boolean flag = true;
                            for (Col c : cols) {
                                max++;
                                int index = c.getIndex();
                                String gn = getCellValue(rowi, index);
                                XSSFCell gnCell = rowNew.createCell(max);
                                gnCell.setCellValue(gn);
                                nameAndValue.put(c.getName(), gn);
                                if (StringUtils.isNotEmpty(gn) && StringUtils.isNotBlank(gn)) {
                                    flag = false;
                                }
                            }
                            if (flag) {
                                sheet.removeRow(rowNew);
                                num--;
                                continue;
                            }
                            client = HttpClients.createDefault();
                            String[] priceArray = getHtmlPrice(sourceUrl, sid, client);
                            XSSFCell priceCell1 = rowNew.createCell(max + 1);
                            priceCell1.setCellValue(priceArray[0]);
                            XSSFCell priceCell2 = rowNew.createCell(max + 2);
                            priceCell2.setCellValue(priceArray[1]);
                            XSSFCell priceCell3 = rowNew.createCell(max + 3);
                            priceCell3.setCellValue(priceArray[2]);
                            XSSFCell sourceCell = rowNew.createCell(max + 4);
                            sourceCell.setCellValue(sourceUrl);
                            XSSFCell targetCell = rowNew.createCell(max + 5);
                            targetCell.setCellValue(result);

                            if (rowNew.getRowNum() % 2 == 0) {
                                priceCell1.setCellStyle(splitStyle);
                                priceCell2.setCellStyle(splitStyle);
                                priceCell3.setCellStyle(splitStyle);
                                sourceCell.setCellStyle(splitStyle);
                                targetCell.setCellStyle(splitStyle);
                            }
                            if (!priceArray[0].equals(priceArray[1])) {
                                priceCell1.setCellStyle(errorStyle);
                                priceCell2.setCellStyle(errorStyle);
                                priceCell3.setCellStyle(errorStyle);
                            }

                            // 替换文本模板中的参数
                            for (XWPFParagraph x : docxParagraphs) {
                                String text = x.getText();
                                if (x.getText().matches("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}")) {
                                    text = text.substring(text.indexOf("{") + 1, text.lastIndexOf("}"));
                                }
                                XWPFParagraph newX = newDocument.createParagraph();
                                XWPFRun run = newX.createRun();
                                if (colNames.contains(text)) {
                                    run.setText(x.getText().replaceAll("\\$\\{" + text + "\\}", nameAndValue.get(text)));
                                } else if (x.getText().contains("原URL")) {
                                    run.setText(x.getText().replaceAll("\\$\\{原URL\\}", sourceUrl));
                                } else if (x.getText().contains("处理后的URL")) {
                                    run.setText(x.getText().replaceAll("\\$\\{处理后的URL\\}", result));
                                } else {
                                    run.setText(x.getText());
                                }
                                run.getCTR().setRPr(x.getRuns().get(0).getCTR().getRPr());
                                newX.addRun(run);
                                newParagraphs.add(newX);
                            }
                            // 添加一行空白数据
                            XWPFParagraph newX = newDocument.createParagraph();
                            XWPFRun run = newX.createRun();
                            run.setText("\n");
                            newX.addRun(run);
                            newParagraphs.add(newX);
                        }
                    } else {
                        for (int i = 1; i < rowNum; i++) {
                            XSSFRow rowi = sheet0.getRow(i);
                            if (rowi == null) {
                                continue;
                            }
                            XSSFCell cell = rowi.getCell(1);
                            if (cell == null) {
                                continue;
                            }
                            String sourceUrl = getCellValue(rowi, colIndex);
                            if (StringUtils.isEmpty(sourceUrl)) {
                                continue;
                            }
                            num++;

                            if (colType.equalsIgnoreCase(Common.FROMCOL)) {
                                if (StringUtils.isBlank(baseWordValue) || !baseWordValue.contains("【para】")) {
                                    ta.setText("请正确填写生成URL的基础URL模板");
                                    return;
                                }
                                if (baseWordValue.contains("【date】") && pointDate == null) {
                                    ta.setText("请选择URL中的日期！");
                                    return;
                                }

                                String pd = pointDate.format(dtf);
                                System.out.println("选中的日期为：" + pd);
                                sourceUrl = baseWordValue.replaceAll("【para】", sourceUrl);
                                sourceUrl = sourceUrl.replaceAll("【date】", pd);
                            }

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
                            boolean flag = true;
                            for (Col c : cols) {
                                max++;
                                int index = c.getIndex();
                                XSSFCell gnCell = rowNew.createCell(max);
                                String gn = getCellValue(rowi, index);
                                gnCell.setCellValue(gn);
                                nameAndValue.put(c.getName(), gn);
                                if (StringUtils.isNotEmpty(gn) && StringUtils.isNotBlank(gn)) {
                                    flag = false;
                                }

                                if (rowNew.getRowNum() % 2 == 0) {
                                    gnCell.setCellStyle(splitStyle);
                                }
                            }
                            if (flag) {
                                sheet.removeRow(rowNew);
                                num--;
                                continue;
                            }
                            client = HttpClients.createDefault();
                            String[] priceArray = getHtmlPrice(sourceUrl, sid, client);
                            XSSFCell priceCell1 = rowNew.createCell(max + 1);
                            priceCell1.setCellValue(priceArray[0]);
                            XSSFCell priceCell2 = rowNew.createCell(max + 2);
                            priceCell2.setCellValue(priceArray[1]);
                            XSSFCell priceCell3 = rowNew.createCell(max + 3);
                            priceCell3.setCellValue(priceArray[2]);
                            XSSFCell sourceCell = rowNew.createCell(max + 4);
                            sourceCell.setCellValue(sourceUrl);
                            XSSFCell targetCell = rowNew.createCell(max + 5);
                            targetCell.setCellValue(result);

                            if (rowNew.getRowNum() % 2 == 0) {
                                priceCell1.setCellStyle(splitStyle);
                                priceCell2.setCellStyle(splitStyle);
                                priceCell3.setCellStyle(splitStyle);
                                sourceCell.setCellStyle(splitStyle);
                                targetCell.setCellStyle(splitStyle);
                            }

                            if (!priceArray[0].equals(priceArray[1])) {
                                priceCell1.setCellStyle(errorStyle);
                                priceCell2.setCellStyle(errorStyle);
                                priceCell3.setCellStyle(errorStyle);
                            }

                            // 替换文本模板中的参数
                            for (int k = 0; k < docxParagraphs.size(); k++) {
                                XWPFParagraph x = docxParagraphs.get(k);
                                String text = x.getText();
                                if (text.matches("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}")) {
                                    text = text.substring(text.indexOf("{") + 1, text.lastIndexOf("}"));
                                }

                                String value = nameAndValue.get(text);
                                if ((k == 0 && x.getText().matches("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}") && (StringUtils.isNotEmpty(value) || StringUtils.isNotBlank(value)))
                                        || (k == 0 && !x.getText().matches("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}"))) {
                                    // 添加一行空白数据
                                    XWPFParagraph newX = newDocument.createParagraph();
                                    XWPFRun run = newX.createRun();
                                    run.setText("\n");
                                    newX.addRun(run);
                                    newParagraphs.add(newX);
                                }

                                XWPFParagraph newX = newDocument.createParagraph();
                                XWPFRun run = newX.createRun();
                                if (colNames.contains(text)) {
                                    run.setText(x.getText().replaceAll("\\$\\{" + text + "\\}", value));
                                } else if (x.getText().contains("原URL")) {
                                    run.setText(x.getText().replaceAll("\\$\\{原URL\\}", sourceUrl));
                                } else if (x.getText().contains("处理后的URL")) {
                                    run.setText(x.getText().replaceAll("\\$\\{处理后的URL\\}", result));
                                } else {
                                    run.setText(x.getText());
                                }
                                run.getCTR().setRPr(x.getRuns().get(0).getCTR().getRPr());
                                newX.addRun(run);
                                newParagraphs.add(newX);
                            }
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
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    return;
                }
                num++;
            }
        }

        // 输出结果文件
        outPush(templateFilePath, newDocument, newPath, workbook, ta);
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
    private String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
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
            return unit.getCellValue(cell);
        } else {
            return "";
        }

    }

    /**
     * 将 word 文档 和 excel 文档 输出
     * @param wordFilePath word模板文档的路径
     * @param wordDocument  word文档内容
     * @param excelFilePath excel文档的路径
     * @param workbook excel文档内容
     * @param ta 文本提示框
     */
    private void outPush(String wordFilePath, XWPFDocument wordDocument, String excelFilePath, XSSFWorkbook workbook, TextArea ta) {
        if (StringUtils.isNotEmpty(wordFilePath)) {
            Date now = new Date();
            // 将内容输出到word文档
            String resultWordPath = wordFilePath.substring(0, wordFilePath.lastIndexOf(".")) + "-" + sdf.format(now) + ".docx";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(resultWordPath);
                wordDocument.write(fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            ta.setText("批处理完成，Word结果文件：" + resultWordPath);
        }

        FileOutputStream outputStream = null;
        try {
            try {
                outputStream = new FileOutputStream(excelFilePath);
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage().contains("另一个程序正在使用此文件，进程无法访问"));
                if (e.getMessage().contains("另一个程序正在使用此文件，进程无法访问")) {
                    ta.setText(e.getMessage());
                }
                return;
            }
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ta.setText(ta.getText() + "\n" + "批处理完成，Excel结果文件：" + excelFilePath);
    }

    /**
     * 获取网页请求中的sid
     * @param client 客户端
     * @param cookieStore cookie存储集
     * @return sid
     * @throws IOException
     */
    private String getSID(HttpClient client, CookieStore cookieStore) throws IOException {
        String jsUrl = "https://track.sanfu.com/track/page/loginApi.js";
        HttpGet get = new HttpGet(jsUrl);
        client.execute(get);
        List<Cookie> cookies = cookieStore.getCookies();
        System.out.println("--------------------------------------------------------------------");
        String sid = "";
        for (Cookie item : cookies) {
            System.out.println("name=" + item.getName() + "; value=" + item.getValue());
            if ("SFSID".equals(item.getName())) {
                sid = item.getValue();
            }
        }
        return sid;
    }

    /**
     * 获取网页中商品的价格
     * @param goodsUrl 商品的网页路径
     * @param sid 请求网页时使用的参数
     * @param client 客户端
     * @return 商品的显示价格
     * @throws IOException
     */
    private String[] getHtmlPrice(String goodsUrl, String sid, HttpClient client) throws Exception {
        System.out.println("--------------------------------------------------------------------");
        System.out.println("sid = " + sid);
        System.out.println("--------------------------------------------------------------------");

        String goodsSn = "";
        String shopId = "";
        goodsUrl = goodsUrl.substring(goodsUrl.indexOf("?") + 1);
        String[] split = goodsUrl.split("&");
        for (String s : split) {
            String[] kv = s.split("=");
            if ("goods_sn".equals(kv[0])) {
                goodsSn = kv[1];
            }
            if ("shopId".equals(kv[0])) {
                shopId = kv[1];
            }
        }
        String realUrl = "https://m.sanfu.com/ms-sanfu-wap-goods-item/itemNew?goodsSn=" + goodsSn + "&sid=" + sid + "&shoId=" + shopId + "&localShoId=" + shopId;
        System.out.println("realUrl = " + realUrl);
        System.out.println("--------------------------------------------------------------------");
        HttpGet get  = new HttpGet(realUrl);
        HttpResponse execute = client.execute(get);
        String goodsInfo = IOUtils.toString(execute.getEntity().getContent(), "UTF-8");
//        System.out.println(goodsInfo);
        JSONObject object = JSONObject.parseObject(goodsInfo);

        String[] priceArray = new String[3];
        JSONObject msg;
        try {
            msg = object.getJSONObject("msg");
        } catch (JSONException e) {
            System.out.println(object.getString("msg"));
            priceArray[0] = priceArray[1] = priceArray[2] = object.getString("msg");
            return priceArray;
        }
        // 网页价格
        double mbPrice = msg.getJSONObject("goodsStock").getDoubleValue("salePrice");
        // 小程序价格
        double scPrice = msg.getJSONObject("goods").getDoubleValue("scPrice");
        // 会员价格
        double memberPrice = msg.getJSONObject("goodsStock").getDoubleValue("memberPrice");

        System.out.println("mbPrice=" + mbPrice);
        System.out.println("scPrice=" + scPrice);
        System.out.println("memberPrice=" + memberPrice);

        priceArray[0] = String.valueOf(mbPrice);
        priceArray[1] = String.valueOf(scPrice);
        priceArray[2] = String.valueOf(memberPrice);

        return priceArray;
    }

    /**
     * 生成新文件并提取货号
     *
     * @param bList
     * @param primaryStage
     */
    private void createFileAndFetchNum(List<Node> bList, Stage primaryStage) {
        System.out.println("开始执行提取货号操作");
        Label label = new Label();
        bList.add(label);
        ((Button) bList.get(0)).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fc = new FileChooser();
                File sourceFile = fc.showOpenDialog(primaryStage);
                if (sourceFile == null) {
                    return;
                }
                label.setText("提取货号中……");
                String path = sourceFile.getPath();
                String newPath = path.substring(0, path.lastIndexOf("."))
                        + sdf.format(new Date()) + path.substring(path.lastIndexOf("."));
                Workbook workbook = null;
                FileOutputStream fos = null;
                int index = -1;
                try {
                    workbook = new XSSFWorkbook(Files.newInputStream(sourceFile.toPath()));
                    fos = new FileOutputStream(newPath);
                    // 获取链接转换明细列
                    Sheet sheet = workbook.getSheetAt(0);
                    Row titleRow = sheet.getRow(0);
                    for (int i = 0; i < titleRow.getLastCellNum(); i++) {
                        Cell titleCell = titleRow.getCell(i);
                        if (titleCell == null || titleCell.getCellType().equals(CellType.BLANK)) {
                            continue;
                        }
                        if ("链接转换明细".equals(unit.getCellValue(titleCell).trim())) {
                            index = i;
                        }
                    }

                    boolean flag = true;
                    if (index == -1) {
                        ((TextField) bList.get(1)).setText(path);
                        return;
                    } else {
                        Cell titleCell = titleRow.getCell(index + 1);
                        if (titleCell != null && "货号".equals(unit.getCellValue(titleCell).trim())) {
                            // 跳过
                            flag = false;
                        } else {
                            // 单元格右移
                            titleRow.shiftCellsRight(index + 1, titleRow.getLastCellNum(), 1);
                            // 插入货号单元格
                            Cell cell = titleRow.createCell(index + 1);
                            cell.setCellValue("货号");
                        }
                    }

                    // 遍历行
                    for (int i = 1; i < sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) {
                            continue;
                        }
                        Cell cell = row.getCell(index);
                        if (cell == null) {
                            continue;
                        }
                        String sourceUrl = getCellValue(row, index);
                        if (StringUtils.isEmpty(sourceUrl)) {
                            continue;
                        }
                        // 获取链接转换明细
                        Cell detailCel = row.getCell(index);
                        String detail = unit.getCellValue(detailCel);
                        int end = detail.length();
                        if (detail.contains("价格")) {
                            end = detail.indexOf("价格");
                        } else if (detail.contains("¥")) {
                            end = detail.indexOf("¥");
                        } else if (detail.contains("定价")) {
                            end = detail.indexOf("定价");
                        } else if (detail.contains("￥")) {
                            end = detail.indexOf("￥");
                        }
                        String num = "";
                        if (detail.contains("货号：")) {
                            num = detail.substring(detail.indexOf("货号：") + 3, end).trim();
                        } else if (detail.contains("货号:")) {
                            num = detail.substring(detail.indexOf("货号:") + 3, end).trim();
                        } else if (detail.contains("货号")) {
                            num = detail.substring(detail.indexOf("货号") + 2, end).trim();
                        }
                        // 正则匹配
                        Pattern pattern = Pattern.compile("\\d+");
                        Matcher matcher = pattern.matcher(num);
                        if (matcher.find()) {
                            String temp = matcher.group(0);
                            System.out.println(temp);
                            num = temp;
                        }
                        // 替换数据中的空格
                        num = num.replaceAll(" ","");
                        num = num.trim();
                        if (flag) {
                            // 在其后插入货号单元格
                            // 单元格右移
                            row.shiftCellsRight(index + 1, row.getLastCellNum(), 1);
                            // 插入货号单元格
                            Cell numCell = row.createCell(index + 1);
                            numCell.setCellValue(num);
                        } else {
                            // 插入货号单元格
                            Cell numCell = row.getCell(index + 1);
                            if (numCell == null) {
                                numCell = row.createCell(index + 1);
                            }
                            numCell.setCellValue(num);
                        }
                    }

                    workbook.write(fos);
                    label.setText("提取货号成功！");
                } catch (Exception e) {
                    label.setText("提取货号失败！");
                    throw new RuntimeException(e);
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                        if (workbook != null) {
                            workbook.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                ((TextField) bList.get(1)).setText(newPath);
            }
        });
    }

    /**
     * 点击按钮生成新文件并提取货号
     *
     * @param bList
     * @param primaryStage
     */
    private void createFileAndFetchNumByButton(List<Node> bList, Stage primaryStage) {
        System.out.println("开始执行提取货号操作");
        Label label = (Label) bList.get(3);

        String path = ((TextField) bList.get(1)).getText();
        if (StringUtils.isBlank(path)){
            label.setText("请先获取文件");
            return;
        }
        label.setText("提取货号中……");
        String newPath = path.substring(0, path.lastIndexOf("."))
                + sdf.format(new Date()) + path.substring(path.lastIndexOf("."));
        Workbook workbook = null;
        FileOutputStream fos = null;
        int index = -1;
        try {
            workbook = new XSSFWorkbook(new FileInputStream(path));
            fos = new FileOutputStream(newPath);
            // 获取链接转换明细列
            Sheet sheet = workbook.getSheetAt(0);
            Row titleRow = sheet.getRow(0);
            for (int i = 0; i < titleRow.getLastCellNum(); i++) {
                Cell titleCell = titleRow.getCell(i);
                if (titleCell == null || titleCell.getCellType().equals(CellType.BLANK)) {
                    continue;
                }
                if ("链接转换明细".equals(unit.getCellValue(titleCell).trim())) {
                    index = i;
                }
            }

            boolean flag = true;
            if (index == -1) {
                ((TextField) bList.get(1)).setText(path);
                return;
            } else {
                Cell titleCell = titleRow.getCell(index + 1);
                if (titleCell != null && "货号".equals(unit.getCellValue(titleCell).trim())) {
                    // 跳过
                    flag = false;
                } else {
                    // 单元格右移
                    titleRow.shiftCellsRight(index + 1, titleRow.getLastCellNum(), 1);
                    // 插入货号单元格
                    Cell cell = titleRow.createCell(index + 1);
                    cell.setCellValue("货号");
                }
            }

            // 遍历行
            for (int i = 1; i < sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Cell cell = row.getCell(index);
                if (cell == null) {
                    continue;
                }
                String sourceUrl = getCellValue(row, index);
                if (StringUtils.isEmpty(sourceUrl)) {
                    continue;
                }
                // 如果存在货号，则不重新获取
                String code = getCellValue(row, index + 1).trim();
                if (code.matches("\\d+")) {
                    continue;
                }
                // 获取链接转换明细
                Cell detailCel = row.getCell(index);
                String detail = unit.getCellValue(detailCel);
                int end = detail.length();
                if (detail.contains("价格")) {
                    end = detail.indexOf("价格");
                } else if (detail.contains("¥")) {
                    end = detail.indexOf("¥");
                } else if (detail.contains("定价")) {
                    end = detail.indexOf("定价");
                } else if (detail.contains("￥")) {
                    end = detail.indexOf("￥");
                }
                String num = "";
                if (detail.contains("货号：")) {
                    num = detail.substring(detail.indexOf("货号：") + 3, end).trim();
                } else if (detail.contains("货号:")) {
                    num = detail.substring(detail.indexOf("货号:") + 3, end).trim();
                } else if (detail.contains("货号")) {
                    num = detail.substring(detail.indexOf("货号") + 2, end).trim();
                }
                // 正则匹配
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(num);
                if (matcher.find()) {
                    String temp = matcher.group(0);
                    System.out.println(temp);
                    num = temp;
                }
                // 替换数据中的空格
                num = num.replaceAll(" ","");
                num = num.trim();
                if (flag) {
                    // 在其后插入货号单元格
                    // 单元格右移
                    row.shiftCellsRight(index + 1, row.getLastCellNum(), 1);
                    // 插入货号单元格
                    Cell numCell = row.createCell(index + 1);
                    numCell.setCellValue(num);
                } else {
                    // 插入货号单元格
                    System.out.println(index + ";" + num);
                    Cell numCell = row.getCell(index + 1);
                    if (numCell == null) {
                        numCell = row.createCell(index + 1);
                    }
                    numCell.setCellValue(num);
                }
            }

            workbook.write(fos);
            label.setText("提取货号成功！");
        } catch (Exception e) {
            label.setText("提取货号失败！");
            throw new RuntimeException(e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ((TextField) bList.get(1)).setText(newPath);
    }
}
