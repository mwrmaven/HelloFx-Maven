package org.example.interfaces.impl;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.example.common.Common;
import org.example.util.Unit;
import org.example.button.BatchButton;
import org.example.interfaces.Function;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mavenr
 * @Classname IncreaseIdentificationPane
 * @Description 批量递增标识修改文件名
 * @Date 2021/12/10 10:36
 */
public class IncreaseIdentificationPane implements Function {
    @Override
    public String tabName() {
        return Common.TOP_BUTTON_2;
    }

    @Override
    public String tabStyle() {
        String style = "-fx-font-weight: bold; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-focus-color: transparent; -fx-text-base-color: white; " +
                "-fx-background-color: #4695d6;  -fx-pref-height: 30; ";
        return style;
    }

    @Override
    public AnchorPane tabPane(Stage stage, double width, double h) {
        AnchorPane ap = new AnchorPane();
        Unit unit = new Unit();
        // 创建选择组件
        List<Node> nodes = unit.chooseFolder(stage, width, null);
        HBox hBox1 = new HBox();
        hBox1.setSpacing(20);
        for (Node node : nodes) {
            hBox1.getChildren().addAll(node);
        }

        HBox hBox2 = new HBox();
        hBox2.setSpacing(20);
        hBox2.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(Common.EDIT_TYPE_LABLE);
        // 创建单选组和单选按钮
        ToggleGroup tg = new ToggleGroup();
        RadioButton button1 = new RadioButton(Common.EDIT_TYPE_1);
        RadioButton button2 = new RadioButton(Common.EDIT_TYPE_2);
        RadioButton button3 = new RadioButton(Common.EDIT_TYPE_3);
        // 将单选按钮添加到单选组
        button1.setToggleGroup(tg);
        button2.setToggleGroup(tg);
        button3.setToggleGroup(tg);
        // 默认选中后置
        button1.setSelected(true);
        // 选中替换字符后显示的文本框
        TextField textField = new TextField();
        textField.setPromptText(Common.EDIT_TYPE_3_PROMPT_TEXT);
        textField.setVisible(false);
        // 配置按钮的触发事件
        tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (button3.getText().equals(((RadioButton)tg.getSelectedToggle()).getText())) {
                    textField.setVisible(true);
                } else {
                    textField.setVisible(false);
                }
            }
        });
        // 将组件添加到横向布局中
        hBox2.getChildren().addAll(label, button1, button2, button3, textField);

        // 选择模板文件
        List<Node> nodes1 = unit.chooseFile(stage, width, null);
        HBox hBox4 = new HBox();
        hBox4.setSpacing(20);
        TextField num = new TextField();
        num.setPromptText(Common.CREATE_TYPE_1_PROMPT_TEXT);

        nodes1.forEach(item -> {
            hBox4.getChildren().addAll(item);
        });
        hBox4.getChildren().addAll(num);
        hBox4.setVisible(false);
        hBox4.setManaged(false);

        HBox hBox3 = new HBox();
        Label labelOfHBox2 = new Label(Common.CREATE_TYPE_LABLE);
        ToggleGroup tgParent = new ToggleGroup();
        RadioButton create = new RadioButton(Common.CREATE_TYPE_1);
        RadioButton replace = new RadioButton(Common.CREATE_TYPE_2);
        create.setToggleGroup(tgParent);
        replace.setToggleGroup(tgParent);
        replace.setSelected(true);
        hBox3.getChildren().addAll(labelOfHBox2, create, replace);
        hBox3.setSpacing(20);
        hBox3.setAlignment(Pos.CENTER_LEFT);
        // 选择单选时触发的事件
        tgParent.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (create.getText().equals(((RadioButton) tgParent.getSelectedToggle()).getText())) {
                    hBox4.setVisible(true);
                    hBox4.setManaged(true);
                    // 选择文件夹路径不可编辑，并清空文本
                    nodes.get(0).setDisable(true);
                    ((TextField) nodes.get(1)).setText("");
                } else {
                    hBox4.setManaged(false);
                    hBox4.setVisible(false);
                    // 选择文件夹路径可编辑
                    nodes.get(0).setDisable(false);
                }
            }
        });

        // 递增标识的类型
        HBox hBox5 = new HBox();
        hBox5.setSpacing(20);
        Label label5 = new Label("初始标识符的类型: ");
//        RadioButton rbiWord = new RadioButton("字母");
        RadioButton rbiNum = new RadioButton("数字");
        RadioButton rbiTime = new RadioButton("时间(格式为: yyyyMMdd 或 yyyy-MM-dd 或 yyyy___MM_dd; 例如: 2021_08_31)");
        ToggleGroup tgIdenType = new ToggleGroup();
//        rbiWord.setToggleGroup(tgIdenType);
        rbiNum.setToggleGroup(tgIdenType);
        rbiTime.setToggleGroup(tgIdenType);
        rbiTime.setSelected(true);
        hBox5.getChildren().addAll(label5, rbiNum, rbiTime);

        // 递增标识的配置
        double textWidth = (width / 2 - 240) / 3;
        HBox hBox6 = new HBox();
        hBox6.setSpacing(20);
        hBox6.setAlignment(Pos.CENTER_LEFT);
        Label label6 = new Label("递增标识的配置: ");
        TextField preText = new TextField();
        preText.setPromptText("标识前置固定字符，没有则不填");
        preText.setPrefWidth(textWidth - 40);
        TextField identification = new TextField();
        identification.setPromptText("初始标识符(新字符)");
        identification.setPrefWidth(textWidth);
        TextField afterText = new TextField();
        afterText.setPromptText("标识后置固定字符，没有则不填");
        afterText.setPrefWidth(textWidth - 40);
        TextField step = new TextField();
        step.setPromptText("步长");
        step.setPrefWidth(60);
        Label stepUnit = new Label("步长单位: ");
        // 单位下拉框
        ChoiceBox cb = new ChoiceBox(FXCollections.observableArrayList(Common.DAY, Common.WEEK,
                Common.MONTH, Common.YEAR));
        cb.setTooltip(new Tooltip("请选择步长单位"));
        cb.setValue(Common.DAY);
        hBox6.getChildren().addAll(label6, preText, identification, afterText, step, stepUnit, cb);
        // 设置选择不通标识符类型时，步长单位的隐藏和显示
        tgIdenType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (rbiTime.getText().equals(((RadioButton) tgIdenType.getSelectedToggle()).getText())) {
                    stepUnit.setVisible(true);
                    cb.setVisible(true);
                } else {
                    stepUnit.setVisible(false);
                    cb.setVisible(false);
                }
            }
        });

        // 文件输出路径
        HBox hBox7 = new HBox();
        hBox7.setSpacing(20);
        hBox7.setAlignment(Pos.CENTER_LEFT);
        List<Node> nodes2 = unit.chooseFolder(stage, width, "文件输出路径");
        nodes2.forEach(item -> {
            hBox7.getChildren().addAll(item);
        });

        BatchButton batchButton = new BatchButton();
        batchButton.setText("批量修改");
        Button edit = batchButton.createInstance();
        TextArea ta = new TextArea();
        ta.setEditable(false);
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);
        root.getChildren().addAll(hBox2, hBox3, hBox4, hBox1, hBox5, hBox6, hBox7, edit, ta);
        ap.getChildren().add(root);

        // 配置edit的点击事件
        edit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // 选择文件夹路径
                String folderPath = ((TextField) nodes.get(1)).getText();
                // 文件名中插入或替换，key 和 value
                String insertOrReplaceKey = ((RadioButton) tg.getSelectedToggle()).getText();
                String insertOrReplaceValue = textField.getText();
                // 根据模板文件创建文件或只替换文件名
                String createOrReplace = ((RadioButton) tgParent.getSelectedToggle()).getText();
                // 选择模板文件路径
                String four = ((TextField) nodes1.get(1)).getText();
                // 初始标识符的类型
                String identificationType = ((RadioButton) tgIdenType.getSelectedToggle()).getText();
                // 标识符前置字符
                String pre = preText.getText();
                // 标识符(新字符)
                String iden = identification.getText();
                // 标识符后置字符
                String after = afterText.getText();
                // 步长
                String stepN = step.getText();
                // 步长单位
                String stepU = String.valueOf(cb.getValue());
                // 输出路径
                String outPath = ((TextField) nodes2.get(1)).getText();
                // 生成文件份数
                String filesNum = num.getText();

                // 如果选择文件夹路径和选择文件路径都为空则报错
                if (StringUtils.isEmpty(folderPath) && StringUtils.isEmpty(four)) {
                    ta.setText("请配置“选择文件夹路径”或“选择文件路径”");
                }
                // 如果是文件名中插入和替换为：替换字符，则输入框不能为空
                if (button3.getText().equals(((RadioButton)tg.getSelectedToggle()).getText())
                        && StringUtils.isEmpty(insertOrReplaceValue)) {
                    ta.setText("请输入旧字符");
                }
                // 判断标识符是否为空
                if (StringUtils.isEmpty(iden)) {
                    ta.setText("请输入标识符(新字符)");
                }
                // 判断步长是否为空
                if (StringUtils.isEmpty(stepN)) {
                    ta.setText("请输入递增的步长");
                }

                if (create.getText().equals(((RadioButton) tgParent.getSelectedToggle()).getText())
                        && !filesNum.matches("[0-9]+")) {
                    ta.setText("请输入生成文件的个数");
                }
                String keyType = Common.NUMBER;
                // 判断标识符类型
                if (identificationType.equals(rbiNum.getText())
                        && !iden.matches("[0-9]+")) {
                    ta.setText("标识符请输入数字");
                }
                if (identificationType.equals(rbiTime.getText())) {
                    keyType = Common.TIME;
                    if (!iden.matches("[0-9]{8}") && !iden.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")
                            && !iden.matches("[0-9]{4}_[0-9]{2}_[0-9]{2}")){
                        ta.setText("标识符请输入时间格式");
                    }
                }

                // 根据用户选择的条件进行批量处理
                if (create.getText().equals(((RadioButton) tgParent.getSelectedToggle()).getText())) {
                    // 根据模板文件创建文件
                    try {
                        createFile(insertOrReplaceKey, insertOrReplaceValue, four, Integer.valueOf(filesNum),
                                keyType, pre, iden, after, Integer.valueOf(stepN), stepU, outPath, ta);
                    } catch (Exception e) {
                        ta.setText("根据模板文件创建文件出错");
                        e.printStackTrace();
                    }
                } else {
                    // 只替换文件名
                    try {
                        onlyReplaceName(insertOrReplaceKey, insertOrReplaceValue, folderPath,
                                keyType, pre, iden, after, Integer.valueOf(stepN), stepU, outPath, ta);
                    } catch (Exception e) {
                        ta.setText("文件名称修改出错");
                        e.printStackTrace();
                    }
                }
            }
        });


        return ap;
    }

    /**
     * 根据模板文件创建文件
     * @param insertOrReplaceKey 文件名中目标是 后置插入/前置插入/替换字符
     * @param insertOrReplaceValue 如果为替换字符，则这个参数为旧字符，否则无用
     * @param filePath 模板文件路径
     * @param fileNum 生成文件的个数
     * @param keyType 初始标识符的类型 数字/时间
     * @param pre 前置固定字符
     * @param iden 初始标识符
     * @param after 后置固定字符
     * @param stepN 步长
     * @param stepU 步长单位
     * @param outPath 文件输出路径
     * @param ta 文本显示框，用来显示提示信息
     */
    private void createFile(String insertOrReplaceKey, String insertOrReplaceValue, String filePath, int fileNum,
                            String keyType, String pre, String iden, String after, int stepN,
                            String stepU, String outPath, TextArea ta) throws IOException, ParseException {
        int index = filePath.lastIndexOf(File.separator);
        String fileFolder = filePath.substring(0, index);
        String fileName = filePath.substring(index + 1);

        List<String> identity = new ArrayList<>();
        identity.add(iden);
        // 标识符类型
        if (Common.NUMBER.equals(keyType)) {
            int start = Integer.parseInt(iden);
            for (int i = 0; i < fileNum; i++) {
                start += stepN;
                identity.add(String.valueOf(start));
            }
        } else if (Common.TIME.equals(keyType)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (iden.contains("_")) {
                sdf = new SimpleDateFormat("yyyy_MM_dd");
            }
            if (!iden.contains("-") && !iden.contains("_")) {
                sdf = new SimpleDateFormat("yyyyMMdd");
            }
            Date date;
            try {
                date = sdf.parse(iden);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                // 提示时间格式错误
                String text = ta.getText();
                text = text + "\n" + "标识符时间格式错误";
                ta.setText(text);
                return;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int dateType = 0;
            if (Common.DAY.equals(stepU)) {
                dateType = Calendar.DAY_OF_MONTH;
            } else if (Common.WEEK.equals(stepU)) {
                dateType = Calendar.WEEK_OF_MONTH;
            } else if (Common.MONTH.equals(stepU)) {
                dateType = Calendar.MONTH;
            } else if (Common.YEAR.equals(stepU)) {
                dateType = Calendar.YEAR;
            }

            for (int i = 0; i < fileNum; i++) {
                calendar.add(dateType, stepN);
                identity.add(sdf.format(calendar.getTime()));
            }
        } else {
            return;
        }

        if (StringUtils.isEmpty(outPath)) {
            outPath = fileFolder;
        }

        if (!File.separator.equals(outPath.substring(outPath.length() - 1))) {
            outPath += File.separator;
        }

        if (Common.EDIT_TYPE_1.equals(insertOrReplaceKey)) {
            // 后置插入
            for (int i = 0; i < identity.size(); i++) {
                String newFileName = fileName.substring(0, fileName.lastIndexOf("."))
                        + pre + identity.get(i) + after
                        + fileName.substring(fileName.lastIndexOf("."));
                File newFile = new File(outPath + newFileName);
                Files.copy(new File(filePath).toPath(), newFile.toPath());

            }
        } else if (Common.EDIT_TYPE_2.equals(insertOrReplaceKey)) {
            // 前置插入
            for (int i = 0; i < identity.size(); i++) {
                String newFileName = pre + identity.get(i) + after
                        + fileName;
                File newFile = new File(outPath + newFileName);
                Files.copy(new File(filePath).toPath(), newFile.toPath());
            }
        } else if (Common.EDIT_TYPE_3.equals(insertOrReplaceKey)) {
            if (StringUtils.isEmpty(insertOrReplaceValue)) {
                ta.setText("旧字符不能为空");
                return;
            }
            // 替换字符
            for (int i = 0; i < identity.size(); i++) {
                String newFileName = fileName.substring(0, fileName.lastIndexOf("."))
                        .replaceAll(insertOrReplaceValue, pre + identity.get(i) + after)
                        + fileName.substring(fileName.lastIndexOf("."));
                File newFile = new File(outPath + newFileName);
                Files.copy(new File(filePath).toPath(), newFile.toPath());
            }
        }
    }

    /**
     * 只替换文件名
     * @param insertOrReplaceKey 文件名中目标是 后置插入/前置插入/替换字符
     * @param insertOrReplaceValue 如果为替换字符，则这个参数为旧字符，否则无用
     * @param folderPath 文件夹路径
     * @param keyType 初始标识符的类型 数字/时间
     * @param pre 前置固定字符
     * @param iden 初始标识符
     * @param after 后置固定字符
     * @param stepN 步长
     * @param stepU 步长单位
     * @param outPath 文件输出路径
     */
    private void onlyReplaceName(String insertOrReplaceKey, String insertOrReplaceValue, String folderPath,
                                 String keyType, String pre, String iden, String after, int stepN,
                                 String stepU, String outPath, TextArea ta) throws ParseException, IOException {
        // 获取文件夹下所有的文件名
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        List<File> fs = new ArrayList<>();

        for (File f : files) {
            if (f.isFile() && !f.getName().startsWith(".")) {
                fs.add(f);
            }
        }
        // 按文件名排序
        fs = fs.stream().sorted(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return (int) (o1.lastModified() - o2.lastModified());
            }
        }).collect(Collectors.toList());

        // 打印文件名
        fs.forEach(item -> System.out.println(item.getName()));

        // 文件数量
        int fileNum = fs.size();

        List<String> identity = new ArrayList<>();
        identity.add(iden);
        // 标识符类型
        if (Common.NUMBER.equals(keyType)) {
            int start = Integer.parseInt(iden);
            for (int i = 0; i < fileNum; i++) {
                start += stepN;
                identity.add(String.valueOf(start));
            }
        } else if (Common.TIME.equals(keyType)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (iden.contains("_")) {
                sdf = new SimpleDateFormat("yyyy_MM_dd");
            }
            if (!iden.contains("-") && !iden.contains("_")) {
                sdf = new SimpleDateFormat("yyyyMMdd");
            }
            Date date;
            try {
                date = sdf.parse(iden);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                // 提示时间格式错误
                String text = ta.getText();
                text = text + "\n" + "标识符时间格式错误";
                ta.setText(text);
                return;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int dateType = 0;
            if (Common.DAY.equals(stepU)) {
                dateType = Calendar.DAY_OF_MONTH;
            } else if (Common.WEEK.equals(stepU)) {
                dateType = Calendar.WEEK_OF_MONTH;
            } else if (Common.MONTH.equals(stepU)) {
                dateType = Calendar.MONTH;
            } else if (Common.YEAR.equals(stepU)) {
                dateType = Calendar.YEAR;
            }

            for (int i = 0; i < fileNum - 1; i++) {
                calendar.add(dateType, stepN);
                identity.add(sdf.format(calendar.getTime()));
            }
        } else {
            return;
        }

        if (StringUtils.isEmpty(outPath)) {
            outPath = folderPath;
        }

        if (!File.separator.equals(outPath.substring(outPath.length() - 1))) {
            outPath += File.separator;
        }

        identity.forEach(item -> {
            System.out.println(item);
        });
        for (int i = 0; i < fs.size(); i++) {
            File item = fs.get(i);
            String id = identity.get(i);
            String name = item.getName();
            if (Common.EDIT_TYPE_1.equals(insertOrReplaceKey)) {
                String newFileName = name.substring(0, name.lastIndexOf("."))
                        + pre + identity.get(i) + after
                        + name.substring(name.lastIndexOf("."));
                File newFile = new File(outPath + newFileName);
                Files.copy(item.toPath(), newFile.toPath());
            } else if (Common.EDIT_TYPE_2.equals(insertOrReplaceKey)) {
                // 前置插入
                String newFileName = pre + identity.get(i) + after
                        + name;
                File newFile = new File(outPath + newFileName);
                Files.copy(item.toPath(), newFile.toPath());
            } else if (Common.EDIT_TYPE_3.equals(insertOrReplaceKey)) {
                if (StringUtils.isEmpty(insertOrReplaceValue)) {
                    ta.setText("旧字符不能为空");
                    return;
                }
                // 替换字符
                String newFileName = name.substring(0, name.lastIndexOf("."))
                        .replaceAll(insertOrReplaceValue, pre + identity.get(i) + after)
                        + name.substring(name.lastIndexOf("."));
                File newFile = new File(outPath + newFileName);
                Files.copy(item.toPath(), newFile.toPath());
            }
        }


    }
}
