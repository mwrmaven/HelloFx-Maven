package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * @author mavenr
 * @Classname CompareTwoFiles
 * @Description 比较两个文件的不同
 * @Date 2021/9/16 17:06
 */
public class CompareTwoFiles {

    private Unit unit = new Unit();

    public AnchorPane compare(Stage primaryStage, double width) {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(10));
        // 左边部分
        VBox left = new VBox();
        left.setPrefWidth((width / 2 + 200) / 2);
        // 左上选择框
        HBox leftTop = new HBox();
        leftTop.setSpacing(10);
        leftTop.setAlignment(Pos.TOP_LEFT);
        List<Node> newChoose = unit.chooseFile(primaryStage, width / 2, "选择新文件");
        for (Node node : newChoose) {
            leftTop.getChildren().addAll(node);
        }
        //

        left.getChildren().addAll(leftTop);


        // 右边部分
        VBox right = new VBox();
        right.setPrefWidth((width / 2 + 200) / 2);
        HBox rightTop = new HBox();
        rightTop.setSpacing(10);
        rightTop.setAlignment(Pos.CENTER_LEFT);
        List<Node> oldChoose = unit.chooseFile(primaryStage, width / 2, "选择旧文件");
        for (Node node : oldChoose) {
            rightTop.getChildren().addAll(node);
        }

        right.getChildren().addAll(rightTop);





        // 添加左右布局
        hBox.getChildren().addAll(left, right);
        // 布局类
        AnchorPane ap = new AnchorPane();
        ap.getChildren().addAll(hBox);
        ap.setPadding(new Insets(10));
        return ap;
    }
}
