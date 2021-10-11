package org.example;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
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
        line1.getChildren().addAll(onlyTextLineRadio, oneFileRadio, folderRadio);

        // 第二行信息
        List<Node> aList = unit.inputText(primaryStage, width, "请输入单个url");
        List<Node> bList = unit.chooseFile(primaryStage, width, null);
        List<Node> cList = unit.chooseFolder(primaryStage, width);

        HBox line2 = new HBox();
        line2.setSpacing(10);
        for (Node node : bList) {
            line2.getChildren().add(node);
        }

        // 配置选择不同数据源时，第二行显示的不同信息
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                String radioText = ((RadioButton) newValue).getText();
                if (radioText.equals(onlyTextLineRadio)) {

                } else if (radioText.equals(folderRadio.getText())) {

                } else {

                }

            }
        });


        vBox.getChildren().addAll(line1, line2);
        anchorPane.getChildren().add(vBox);
        return anchorPane;
    }

    /**
     * url编码
     */
    private void encode() {
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
