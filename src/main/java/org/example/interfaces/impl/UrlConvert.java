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
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
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
 * @Description URL ?????????????????????????????????
 * @Date 2021/12/8 13:28
 */
public class UrlConvert implements Function {

    private Unit unit = new Unit();
    // ????????????url???????????????????????????(??????????????????)
    private static final String FIXEDAFTERFIRST = "fixedAfterFirst";
    // ????????????url???????????????????????????(??????????????????)
    private static final String AFTERFIXEDPRE = "afterFixedPre";
    // ??????URL?????????URL??????
    private static final String URLTEMPLATE = "urlTemplate";

    private static final Pattern pattern = Pattern.compile("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}");

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private static final String OR = "???";
    private static final String AND = "??????";

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

        // url??????????????????????????????????????????url????????????
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

        // ???????????????
        List<Node> aList = unit.inputText(primaryStage, width, "???????????????url");
        List<Node> bList = unit.chooseFile(primaryStage, width, "????????????");
        List<Node> cList = unit.chooseFolder(primaryStage, width, null);

        HBox line2 = new HBox();
        line2.setAlignment(Pos.CENTER_LEFT);
        line2.setSpacing(10);
        for (Node node : bList) {
            line2.getChildren().add(node);
        }

        // ??????????????????
        RadioButton txtRadio = new RadioButton(Common.TXT);
        RadioButton excelRadio = new RadioButton(Common.EXCEL);
        excelRadio.setSelected(true);
        ToggleGroup fileType = new ToggleGroup();
        txtRadio.setToggleGroup(fileType);
        excelRadio.setToggleGroup(fileType);
        // ????????????????????????

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
        tf.setPromptText("?????????url???????????????excel?????????????????????");

        HBox line3_1 = new HBox();
        line3_1.setSpacing(10);
        line3_1.setAlignment(Pos.CENTER_LEFT);
        line3_1.getChildren().addAll(fixCol, fromCol, tf);

        Label baseWordLabel = new Label("??????URL?????????URL?????????");

        String urlTemplateList = Config.get(URLTEMPLATE);
        ComboBox<String> baseWordText = new ComboBox<>();
        // ????????????
        baseWordText.setEditable(true);
        baseWordText.setPrefWidth(width / 2 - 200);
        baseWordText.setPromptText("???????????????????????????????????????para???????????????????????????date?????????????????? http://www.???para???.com???date=???date???");
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
                // combobox????????????
                String newItem = baseWordText.getEditor().getText();
                if (StringUtils.isNotEmpty(newItem) && !baseWordText.getItems().contains(newItem)) {
                    baseWordText.getItems().add(newItem);
                }

                // ??????????????????????????????
                String collect = baseWordText.getItems().stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining(";"));
                if (!collect.equals(urlTemplateList)) {
                    // ??????????????????????????????
                    Config.set(URLTEMPLATE, collect);
                }
            }
        });

        // ????????????
        Button deleteURLTemplateButton = new Button("???????????????");
        deleteURLTemplateButton.setStyle("-fx-background-radius: 4; -fx-background-color: #878787; -fx-text-fill: white;");
        deleteURLTemplateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // ??????????????????
                String selected = baseWordText.getEditor().getText();
                baseWordText.getItems().remove(selected);
                // ????????????????????????
                baseWordText.getEditor().setText("");
                // ??????combobox?????????????????????????????????
                String collect = baseWordText.getItems().stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining(";"));
                if (!collect.equals(urlTemplateList)) {
                    // ??????????????????????????????
                    Config.set(URLTEMPLATE, collect);
                }
            }
        });

        HBox line3_2 = new HBox();
        line3_2.setSpacing(10);
        line3_2.setAlignment(Pos.CENTER_LEFT);
        line3_2.getChildren().addAll(baseWordLabel, baseWordText, deleteURLTemplateButton);

        Label dateLabel = new Label("??????URL???????????????");
        DatePicker dp = new DatePicker();
        HBox line3_3 = new HBox();
        line3_3.setSpacing(10);
        line3_3.setAlignment(Pos.CENTER_LEFT);
        line3_3.getChildren().addAll(dateLabel, dp);


        // ??????????????????????????????????????????
        MultiComboBox<Col> mcb = new MultiComboBox<>();

        // ???????????????????????????
        HBox line3After = new HBox();
        line3After.setSpacing(10);
        line3After.setAlignment(Pos.CENTER_LEFT);
        Label labelCopy = new Label("???????????????????????????????????????????????????");
        ObservableList<Col> titles = FXCollections.observableArrayList();
        ComboBox<Col> comboBox = mcb.createComboBox(titles, width / 3);
        line3After.getChildren().addAll(labelCopy, comboBox);

        HBox line4Pre = new HBox();
        line4Pre.setSpacing(10);
        line4Pre.setAlignment(Pos.CENTER_LEFT);
        Label fixedPreAfterFirstLable = new Label("????????????url???????????????????????????(??????????????????)???");
        CheckBox fixedPreAfterFirstCb = new CheckBox("??????????????????????????????");
        fixedPreAfterFirstCb.setSelected(true);
        //---------------------------------------------------------
        String fixedAfterFirstParam = Config.get(FIXEDAFTERFIRST);
        ComboBox<String> preFixes = new ComboBox<>();
        // ????????????
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
                // combobox????????????
                String newItem = preFixes.getEditor().getText();
                if (StringUtils.isNotEmpty(newItem) && !preFixes.getItems().contains(newItem)) {
                    preFixes.getItems().add(newItem);
                }

                // ??????????????????????????????
                String collect = preFixes.getItems().stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining(","));
                if (!collect.equals(fixedAfterFirstParam)) {
                    // ??????????????????????????????
                    Config.set(FIXEDAFTERFIRST, collect);
                }
            }
        });
        // ????????????
        Button deleteButton = new Button("???????????????");
        deleteButton.setStyle("-fx-background-radius: 4; -fx-background-color: #878787; -fx-text-fill: white;");
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // ??????????????????
                String selected = preFixes.getEditor().getText();
                preFixes.getItems().remove(selected);
                // ????????????????????????
                preFixes.getEditor().setText("");
                // ??????combobox?????????????????????????????????
                String collect = preFixes.getItems().stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining(","));
                if (!collect.equals(fixedAfterFirstParam)) {
                    // ??????????????????????????????
                    Config.set(FIXEDAFTERFIRST, collect);
                }
            }
        });


        line4Pre.getChildren().addAll(fixedPreAfterFirstLable, fixedPreAfterFirstCb, preFixes, deleteButton);

        HBox line4 = new HBox();
        line4.setSpacing(10);
        line4.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("????????????url?????????????????????");
        Label preLabel = new Label("????????????");
        TextField preTf = new TextField();
        // ????????????????????????
        preTf.setPromptText("??????????????????");
        Label endLine = new Label("????????????");
        TextField endTf = new TextField();
        // ????????????????????????
        endTf.setPromptText("??????????????????");
        line4.getChildren().addAll(label, preLabel, preTf, endLine, endTf);

        HBox line5Pre = new HBox();
        line5Pre.setSpacing(10);
        line5Pre.setAlignment(Pos.CENTER_LEFT);
        Label fixedAfterPreLabel = new Label("????????????url???????????????????????????(??????????????????)???");
        CheckBox fixedAfterPreCb = new CheckBox("??????????????????????????????");
        fixedAfterPreCb.setSelected(true);
        TextField fixedAfterPreTf = new TextField();
        // ????????????????????????
        String param = Config.get(AFTERFIXEDPRE);
        // ??????????????????????????????
        if (StringUtils.isNotEmpty(param)) {
            fixedAfterPreTf.setText(param);
        }
        line5Pre.getChildren().addAll(fixedAfterPreLabel, fixedAfterPreCb, fixedAfterPreTf);
        String text = fixedAfterPreTf.getText();
        // ??????????????????????????????
        fixedAfterPreTf.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // ????????????????????????????????????
                if (!text.equals(fixedAfterPreTf.getText())) {
                    // ??????????????????
                    Config.set(AFTERFIXEDPRE, fixedAfterPreTf.getText());
                }
            }
        });

        HBox line5 = new HBox();
        line5.setSpacing(10);
        line5.setAlignment(Pos.CENTER_LEFT);
        Label afterlabel = new Label("????????????url?????????????????????");
        Label afterPreLabel = new Label("????????????");
        TextField afterPreTf = new TextField();
        // ????????????????????????
        afterPreTf.setPromptText("??????????????????");
        Label afterEndLine = new Label("????????????");
        TextField afterEndTf = new TextField();
        // ????????????????????????
        afterEndTf.setPromptText("??????????????????");
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

        // ??????????????????
        List<Node> nodes = unit.chooseFile(primaryStage, width, "??????????????????");
        ((Button) nodes.get(0)).setPrefWidth(120);
        HBox template = new HBox();
        template.setSpacing(10);
        template.setAlignment(Pos.CENTER_LEFT);
        for (Node n : nodes) {
            template.getChildren().add(n);
        }

        // ??????????????????
        BatchButton button = new BatchButton();
        Button execute = button.createInstance();

        // ?????????????????????
        TextArea ta = new TextArea();
        ta.setWrapText(true);

        // ???????????????????????????????????????????????????????????????
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                // ???????????????
                comboBox.getItems().clear();

                // ????????????????????????
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
                    // ????????????????????????url???
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
                    // ????????????????????????url???
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

        // ????????????????????????????????????????????????
        fileType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                // ???????????????
                comboBox.getItems().clear();

                String text = ((RadioButton) newValue).getText();
                if (text.equals(excelRadio.getText())) {

                    if (((RadioButton)colType.getSelectedToggle()).getText().equalsIgnoreCase(fromCol.getText())) {
                        // ???????????????????????? ??? ????????????url???
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3) + 1, line3_1);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_1) + 1, line3_2);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_2) + 1, line3_3);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_3) + 1, line3After);

                    } else {
                        // ???????????????????????? ??? ????????????url???
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3) + 1, line3_1);
                        vBox.getChildren().add(vBox.getChildren().indexOf(line3_1) + 1, line3After);
                    }

                    File file = null;
                    // ?????????????????????
                    String sourceType = ((RadioButton) toggleGroup.getSelectedToggle()).getText();
                    if (Common.ONEFILE.equals(sourceType)) {
                        // ??????????????????
                        String filePath = ((TextField) bList.get(1)).getText();
                        if (StringUtils.isEmpty(filePath)) {
                            ta.setText("??????????????????");
                            return;
                        }
                        if (!filePath.endsWith(".xls") && !filePath.endsWith(".xlsx")) {
                            ta.setText("?????????????????????");
                            return;
                        }
                        file = new File(filePath);
                    } else if (Common.ONEFOLDER.equals(sourceType)) {
                        String folderPath = ((TextField) cList.get(1)).getText();
                        if (StringUtils.isEmpty(folderPath)) {
                            ta.setText("?????????????????????");
                            return;
                        }
                        File[] files = new File(folderPath).listFiles();
                        if (files.length == 0) {
                            ta.setText("?????????????????????????????????");
                            return;
                        }
                        file = files[0];
                        if (!file.getName().endsWith(".xls") && !file.getName().endsWith(".xlsx")) {
                            ta.setText("????????????????????????????????????");
                            return;
                        }
                    }

                    try {
                        // ??????excel??????????????????
                        XSSFWorkbook workbook = new XSSFWorkbook(file);
                        XSSFSheet sheet = workbook.getSheetAt(0);
                        XSSFRow row = sheet.getRow(0);
                        int size = row.getLastCellNum();

                        ObservableList<Col> objects = FXCollections.observableArrayList();
                        for (int i = 0; i < size; i++) {
                            String cellValue = getCellValue(row, i);
                            if (StringUtils.isNotEmpty(cellValue)) {
                                // ?????????????????????
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
                    // ??????????????????
                    // ???????????????????????? ??? ????????????url???
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

        // ?????????????????????
        ((TextField) bList.get(1)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // ?????????????????????????????????????????????????????????????????????excel??????
                if (Common.ONEFILE.equals(((RadioButton)toggleGroup.getSelectedToggle()).getText())
                        && Common.EXCEL.equals(((RadioButton)fileType.getSelectedToggle()).getText())) {
                    if (!newValue.endsWith(".xls") && !newValue.endsWith(".xlsx")) {
                        ta.setText("?????????xls???xlsx?????????????????????");
                        return;
                    } else {
                        File file = new File(newValue);
                        try {
                            // ??????excel??????????????????
                            XSSFWorkbook workbook = new XSSFWorkbook(file);
                            XSSFSheet sheet = workbook.getSheetAt(0);
                            XSSFRow row = sheet.getRow(0);
                            int size = row.getLastCellNum();
                            ObservableList<Col> objects = FXCollections.observableArrayList();
                            for (int i = 0; i < size; i++) {
                                String cellValue = getCellValue(row, i);
                                if (StringUtils.isNotEmpty(cellValue)) {
                                    // ?????????????????????
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

        // ?????????????????????
        ((TextField) cList.get(1)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // ?????????????????????????????????????????????????????????????????????excel??????
                if (Common.ONEFOLDER.equals(((RadioButton)toggleGroup.getSelectedToggle()).getText())
                        && Common.EXCEL.equals(((RadioButton)fileType.getSelectedToggle()).getText())) {
                    File file;
                    File[] files = new File(newValue).listFiles();
                    if (files.length == 0) {
                        ta.setText("?????????????????????????????????");
                        return;
                    }
                    file = files[0];
                    if (!file.getName().endsWith(".xls") && !file.getName().endsWith(".xlsx")) {
                        ta.setText("????????????????????????????????????");
                        return;
                    }
                    try {
                        // ??????excel??????????????????
                        XSSFWorkbook workbook = new XSSFWorkbook(file);
                        XSSFSheet sheet = workbook.getSheetAt(0);
                        XSSFRow row = sheet.getRow(0);
                        int size = row.getLastCellNum();
                        ObservableList<Col> objects = FXCollections.observableArrayList();
                        for (int i = 0; i < size; i++) {
                            String cellValue = getCellValue(row, i);
                            if (StringUtils.isNotEmpty(cellValue)) {
                                // ?????????????????????
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

        // ??????????????????????????????
        execute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                long start = System.currentTimeMillis();
                // ???????????????????????????
                String fixedPreAfterFirst = preFixes.getEditor().getText();
                System.out.println("????????????????????????: " + fixedPreAfterFirst);

                // ???????????????????????????
                System.out.println("??????????????????????????????" + comboBox.getButtonCell().getText());
                ObservableList<Col> items = comboBox.getItems();
                List<Col> cols = new ArrayList<>();
                for (Col c : items) {
                    if (c.isSelected()) {
                        // ??????????????????
                        cols.add(c);
                    }
                }

                ta.setText("");
                // ????????????????????????
                String toggleText = ((RadioButton) toggleGroup.getSelectedToggle()).getText();
                // ???????????????????????????????????????
                String startPre = preTf.getText();
                String startEnd = endTf.getText();
                // ???????????????????????????????????????
                String afterPre = afterPreTf.getText();
                String afterEnd = afterEndTf.getText();
                // ????????????????????????utf-8?????????????????????
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
                // ????????????
                String encodeType = ((RadioButton) encodeGroup.getSelectedToggle()).getText();
                // ????????????
                String type = ((RadioButton) fileType.getSelectedToggle()).getText();
                // ???????????????????????????url???????????????????????????????????????????????????????????????????????????
                boolean preEncodeAfterFirstSelected = fixedPreAfterFirstCb.isSelected();
                String preEncodeAfterFirstText = fixedPreAfterFirst;
                System.out.println("??????????????? " + preEncodeAfterFirstText);

                // ???????????????????????????url???????????????????????????????????????????????????????????????????????????
                boolean selected = fixedAfterPreCb.isSelected();
                String fixedAfterPre = fixedAfterPreTf.getText();

                // excel???url???????????????
                int colIndex = -1;
                if (excelRadio.getText().equals(type) && !onlyTextLineRadio.getText().equals(toggleText)) {
                    if (StringUtils.isEmpty(tf.getText().trim())) {
                        ta.setText("?????????url???excel?????????????????????");
                        return;
                    }
                    colIndex = Integer.parseInt(tf.getText().trim()) - 1;
                }

                String templateFilePath = ((TextField) nodes.get(1)).getText();

                // ?????????????????????
                LocalDate pointDate = dp.getValue();

                // ????????????URL?????????URL??????????????????????????????
                String baseWordValue = baseWordText.getEditor().getText();

                if (toggleText.equals(onlyTextLineRadio.getText())) {
                    // ???????????????url??????
                    // ????????????url
                    String oneUrl = startPre + ((TextField) aList.get(1)).getText();
                    if (preEncodeAfterFirstSelected) {
                        oneUrl += preEncodeAfterFirstText;
                    }
                    oneUrl += startEnd;
                    String convertUrl = "";
                    if (encodeType.equals(encodeRadio.getText())) {
                        // ??????
                        try {
                            convertUrl = URLEncoder.encode(oneUrl, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // ??????
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
                    // ????????????????????????????????????url????????????
                    // ?????????????????????
                    String folderPath = ((TextField) cList.get(1)).getText();
                    // ??????????????????
                    File folder = new File(folderPath);
                    File[] files = folder.listFiles();
                    batchFilesAndExportToExcel(files, type, colIndex, cols, startPre, startEnd, afterPre,
                            afterEnd, encodeType, ta, preEncodeAfterFirstSelected, preEncodeAfterFirstText,
                            selected, fixedAfterPre, templateFilePath, ((RadioButton) colType.getSelectedToggle()).getText(), baseWordValue, pointDate);
                } else {
                    // ??????????????????url????????????
                    String filePath = ((TextField) bList.get(1)).getText();
                    File file = new File(filePath);
                    File[] files = new File[]{file};
                    batchFilesAndExportToExcel(files, type, colIndex, cols, startPre, startEnd, afterPre,
                            afterEnd, encodeType, ta, preEncodeAfterFirstSelected, preEncodeAfterFirstText,
                            selected, fixedAfterPre, templateFilePath, ((RadioButton) colType.getSelectedToggle()).getText(), baseWordValue, pointDate);
                }
                System.out.println("???????????????" + (System.currentTimeMillis() - start) / 1000 + " ???");
            }
        });

        vBox.getChildren().addAll(line1, line2, line3, line3_1, line3_2, line3_3, line3After, line4Pre, line4, line5Pre, line5, line6, template, execute, ta);
        anchorPane.getChildren().add(vBox);
        return anchorPane;
    }

    /**
     * ?????????????????????????????????excel?????????
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

        System.out.println("????????????url????????????????????????????????????????????????" + preEncodeFlag + " ??????" + preEncodeFixed);
        System.out.println("????????????url???????????????????????????(??????????????????) " + fixedFlag + " ??????" + fixed);
        // ??????????????????
        String path = files[0].getPath();
        // ??????????????????
        String newPath = path.substring(0, path.lastIndexOf(File.separator) + 1) + "result-" + sdf.format(now) + ".xlsx";

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("URL????????????");
        // ????????????
        sheet.createFreezePane(0, 1);
        XSSFRow row = sheet.createRow(0);

        // ????????????
        XSSFCellStyle cs = workbook.createCellStyle();
        // ?????????????????????????????????
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 153, (byte) 153, (byte) 153}, new DefaultIndexedColorMap()));
        // ??????????????????
        cs.setAlignment(HorizontalAlignment.CENTER);

        // ??????????????????
        int count = -1;
        for (Col c : cols) {
            count++;
            XSSFCell cell = row.createCell(count);
            cell.setCellValue(c.getName());
            cell.setCellStyle(cs);
        }
        XSSFCell cellPre1 = row.createCell(count + 1);
        cellPre1.setCellValue("?????????????????????");
        XSSFCell cell1 = row.createCell(count + 2);
        cell1.setCellValue("???URL");
        XSSFCell cell2 = row.createCell(count + 3);
        cell2.setCellValue("????????????URL");

        cellPre1.setCellStyle(cs);
        cell1.setCellStyle(cs);
        cell2.setCellStyle(cs);

        int num = 0;

        List<XWPFParagraph> docxParagraphs = new ArrayList<>();
        if (StringUtils.isNotBlank(templateFilePath)) {
            // ??????????????????????????????
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
                    ta.setText("???????????????????????????doc???docx??????");
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

        // ?????????????????????doc???docx?????????????????????
        List<XWPFParagraph> newParagraphs = new ArrayList<>();
        XWPFDocument newDocument = new XWPFDocument();
        // ??????????????????
        if (Common.TXT.equals(fileType)) {
            // ????????????????????????
            for (File f : files) {
                if (f.getPath().equals(newPath)) {
                    continue;
                }
                // ????????????
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
                            // ??????excel
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
                            // ??????excel
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
                    if (e.getMessage() != null && e.getMessage().contains("?????????????????????????????????????????????????????????")) {
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
            // ?????????????????????????????????
            CookieStore cookieStore = new BasicCookieStore();
            HttpClientBuilder builder = HttpClientBuilder.create().setDefaultCookieStore(cookieStore);
            HttpClient client = builder.build();
            String sid = "";
            try {
                sid = getSID(client, cookieStore);
            } catch (IOException e) {
                System.out.println("?????????????????????????????????SID");
            }

            // ??????????????????????????????
            for (File f : files) {
                if (f.getPath().equals(newPath)) {
                    continue;
                }
                // ????????????
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
                            // ??????excel
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
                            String price = getHtmlPrice(sourceUrl, sid, client);
                            XSSFCell priceCell = rowNew.createCell(max + 1);
                            priceCell.setCellValue(price);
                            XSSFCell sourceCell = rowNew.createCell(max + 2);
                            sourceCell.setCellValue(sourceUrl);
                            XSSFCell targetCell = rowNew.createCell(max + 3);
                            targetCell.setCellValue(result);

                            // ??????????????????????????????
                            for (XWPFParagraph x : docxParagraphs) {
                                String text = x.getText();
                                if (x.getText().matches("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}")) {
                                    text = text.substring(text.indexOf("{") + 1, text.lastIndexOf("}"));
                                }
                                XWPFParagraph newX = newDocument.createParagraph();
                                XWPFRun run = newX.createRun();
                                if (colNames.contains(text)) {
                                    run.setText(x.getText().replaceAll("\\$\\{" + text + "\\}", nameAndValue.get(text)));
                                } else if (x.getText().contains("???URL")) {
                                    run.setText(x.getText().replaceAll("\\$\\{???URL\\}", sourceUrl));
                                } else if (x.getText().contains("????????????URL")) {
                                    run.setText(x.getText().replaceAll("\\$\\{????????????URL\\}", result));
                                } else {
                                    run.setText(x.getText());
                                }
                                run.getCTR().setRPr(x.getRuns().get(0).getCTR().getRPr());
                                newX.addRun(run);
                                newParagraphs.add(newX);
                            }
                            // ????????????????????????
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
                            XSSFCell cell = rowi.getCell(colIndex);
                            if (cell == null) {
                                continue;
                            }
                            String sourceUrl = getCellValue(rowi, colIndex);
                            if (StringUtils.isEmpty(sourceUrl)) {
                                continue;
                            }
                            num++;

                            if (colType.equalsIgnoreCase(Common.FROMCOL)) {
                                if (StringUtils.isBlank(baseWordValue) || !baseWordValue.contains("???para???")) {
                                    ta.setText("?????????????????????URL?????????URL??????");
                                    return;
                                }
                                if (baseWordValue.contains("???date???") && pointDate == null) {
                                    ta.setText("?????????URL???????????????");
                                    return;
                                }

                                String pd = pointDate.format(dtf);
                                System.out.println("?????????????????????" + pd);
                                sourceUrl = baseWordValue.replaceAll("???para???", sourceUrl);
                                sourceUrl = sourceUrl.replaceAll("???date???", pd);
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
                            // ??????excel
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
                            }
                            if (flag) {
                                sheet.removeRow(rowNew);
                                num--;
                                continue;
                            }
                            client = HttpClients.createDefault();
                            String price = getHtmlPrice(sourceUrl, sid, client);
                            XSSFCell priceCell = rowNew.createCell(max + 1);
                            priceCell.setCellValue(price);
                            XSSFCell sourceCell = rowNew.createCell(max + 2);
                            sourceCell.setCellValue(sourceUrl);
                            XSSFCell targetCell = rowNew.createCell(max + 3);
                            targetCell.setCellValue(result);

                            // ??????????????????????????????
                            for (int k = 0; k < docxParagraphs.size(); k++) {
                                XWPFParagraph x = docxParagraphs.get(k);
                                String text = x.getText();
                                if (text.matches("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}")) {
                                    text = text.substring(text.indexOf("{") + 1, text.lastIndexOf("}"));
                                }

                                String value = nameAndValue.get(text);
                                if ((k == 0 && x.getText().matches("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}") && (StringUtils.isNotEmpty(value) || StringUtils.isNotBlank(value)))
                                        || (k == 0 && !x.getText().matches("\\$\\{[\\u4e00-\\u9fa5_a-zA-Z0-9]*\\}"))) {
                                    // ????????????????????????
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
                                } else if (x.getText().contains("???URL")) {
                                    run.setText(x.getText().replaceAll("\\$\\{???URL\\}", sourceUrl));
                                } else if (x.getText().contains("????????????URL")) {
                                    run.setText(x.getText().replaceAll("\\$\\{????????????URL\\}", result));
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
                    if (e.getMessage() != null && e.getMessage().contains("?????????????????????????????????????????????????????????")) {
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

        // ??????????????????
        outPush(templateFilePath, newDocument, newPath, workbook, ta);
    }

    /**
     * url??????????????????????????? utf-8
     * @param sourceUrl ?????????????????????
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
     * ??????????????????????????? utf-8
     * @param sourceUrl ?????????????????????
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
     * ????????????????????????????????????String
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
            // ??????double?????????0
            BigDecimal bd = new BigDecimal(numericCellValue);
            BigDecimal noZeroBigDecimal = bd.stripTrailingZeros();
            // ????????????????????????
            String value = noZeroBigDecimal.toPlainString();
            return String.valueOf(value);
        } else if (cell.getCellType() == CellType.FORMULA) {
            return cell.getStringCellValue();
        } else {
            return "";
        }

    }

    /**
     * ??? word ?????? ??? excel ?????? ??????
     * @param wordFilePath word?????????????????????
     * @param wordDocument  word????????????
     * @param excelFilePath excel???????????????
     * @param workbook excel????????????
     * @param ta ???????????????
     */
    private void outPush(String wordFilePath, XWPFDocument wordDocument, String excelFilePath, XSSFWorkbook workbook, TextArea ta) {
        if (StringUtils.isNotEmpty(wordFilePath)) {
            Date now = new Date();
            // ??????????????????word??????
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
            ta.setText("??????????????????Word???????????????" + resultWordPath);
        }

        FileOutputStream outputStream = null;
        try {
            try {
                outputStream = new FileOutputStream(excelFilePath);
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage().contains("?????????????????????????????????????????????????????????"));
                if (e.getMessage().contains("?????????????????????????????????????????????????????????")) {
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
        ta.setText(ta.getText() + "\n" + "??????????????????Excel???????????????" + excelFilePath);
    }

    /**
     * ????????????????????????sid
     * @param client ?????????
     * @param cookieStore cookie?????????
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
     * ??????????????????????????????
     * @param goodsUrl ?????????????????????
     * @param sid ??????????????????????????????
     * @param client ?????????
     * @return ?????????????????????
     * @throws IOException
     */
    private String getHtmlPrice(String goodsUrl, String sid, HttpClient client) throws Exception {
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

        JSONObject msg;
        try {
            msg = object.getJSONObject("msg");
        } catch (JSONException e) {
            return object.getString("msg");
        }
        double mbPrice = msg.getJSONObject("goodsStock").getDoubleValue("salePrice");
        double scPrice = msg.getJSONObject("goods").getDoubleValue("scPrice");
        // ????????????
        double memberPrice = msg.getJSONObject("goodsStock").getDoubleValue("memberPrice");

        System.out.println("mbPrice=" + mbPrice);
        System.out.println("scPrice=" + scPrice);
        System.out.println("memberPrice=" + memberPrice);

        StringBuilder result = new StringBuilder("?????????????????????").append(Math.max(mbPrice, scPrice));
        if (memberPrice != 0) {
            result.append("; ???????????????").append(memberPrice);
        }
        return result.toString();
    }

}
