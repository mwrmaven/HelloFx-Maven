package org.example.interfaces.impl;

import com.mavenr.code.QRCode;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.button.BatchButton;
import org.example.interfaces.Function;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;


/**
 * @author mavenr
 * @Classname ScreenShotAndReadQRCode
 * @Description 截屏并识别二维码
 * @Date 2023/10/8 9:45
 */
public class ScreenShotAndReadQRCode implements Function {

    private Stage privateStage, screenStage;

    private HBox hBox;

    private double sceneX_start, sceneY_start, sceneX_end, sceneY_end;

    private TextArea ta;

    @Override
    public String tabName() {
        return "截图识别二维码信息";
    }

    @Override
    public String tabStyle() {
        String style = "-fx-font-weight: bold; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-focus-color: transparent; -fx-text-base-color: white; " +
                "-fx-background-color: DarkSlateBlue;  -fx-pref-height: 30; ";

        return style;
    }

    @Override
    public AnchorPane tabPane(Stage stage, double width, double h) {
        privateStage = stage;
        AnchorPane root = new AnchorPane();

        double w = width / 2 + 200;
        // 竖向排列
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPrefWidth(w);
        vBox.setPrefHeight(h-100);

        // 第一行，按钮
        HBox buttonHbox = new HBox();
        buttonHbox.setAlignment(Pos.CENTER);
        buttonHbox.setPadding(new Insets(10, 0, 0, 0));
        // 按钮
        BatchButton batchButton = new BatchButton("点击按钮进行截图", 200, 40);
        Button button = batchButton.createInstance();
        buttonHbox.getChildren().add(button);
        vBox.getChildren().add(buttonHbox);

        // 第二行，显示二维码的内容
        HBox taHbox = new HBox();
        taHbox.setAlignment(Pos.CENTER);
        // 显示区域
        ta = new TextArea();
        ta.setPrefWidth(w - 60);
        taHbox.getChildren().add(ta);
        vBox.getChildren().add(taHbox);

        // 按钮触发事件
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                executeCut(stage);
            }
        });

        root.getChildren().add(vBox);

        return root;
    }

    /**
     * 执行截图操作
     */
    private void executeCut(Stage stage) {
        // 隐藏窗口
        privateStage.setIconified(true);

        // 创建新窗口
        screenStage = new Stage();

        AnchorPane ap = new AnchorPane();
        ap.setStyle("-fx-background-color: #B5B5B522");
        Scene scene = new Scene(ap);
        scene.setFill(Paint.valueOf("#ffffff00"));

        screenStage.setFullScreenExitHint("");
        screenStage.setScene(scene);
        // 全屏
        screenStage.setFullScreen(true);
        // 透明
        screenStage.initStyle(StageStyle.TRANSPARENT);
        screenStage.show();

        // 调用矩形拖拉
        drag(ap);

    }

    /**
     * 矩形拖拉
     * @param an
     */
    private void drag(AnchorPane an) {
        ta.setText("");

        // 按住鼠标触发
        an.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // 判断如果是右键，则退出
                if (event.getButton() == MouseButton.SECONDARY) {
//                    System.out.println("点击右键，退出截图");
                    screenStage.close();
                    privateStage.setIconified(false);
                    return;
                }
                an.getChildren().clear();

                hBox = new HBox();
                hBox.setBackground(null);
                hBox.setBorder(new Border(new BorderStroke(Paint.valueOf("#CD3700"), BorderStrokeStyle.SOLID, null, new BorderWidths(2))));

                // 获取坐标
                sceneX_start = event.getSceneX();
                sceneY_start = event.getSceneY();

                an.getChildren().add(hBox);
                AnchorPane.setLeftAnchor(hBox, sceneX_start);
                AnchorPane.setTopAnchor(hBox, sceneY_start);
            }
        });

        // AnchorPane 根据拖拽动态变化，充满整个拖拽空间
        an.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                an.startFullDrag();
            }
        });

        // 获取拖拽结束的坐标
        an.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {
            @Override
            public void handle(MouseDragEvent event) {
                Label label = new Label();
                label.setAlignment(Pos.CENTER);
                label.setPrefWidth(170);
                label.setPrefHeight(30);

                an.getChildren().add(label);

                AnchorPane.setLeftAnchor(label, sceneX_start);
                AnchorPane.setTopAnchor(label, sceneY_start - label.getPrefHeight());

                label.setTextFill(Paint.valueOf("#ffffff"));
                label.setStyle("-fx-background-color: #000000");

                double sceneX = event.getSceneX();
                double sceneY = event.getSceneY();

                double width = sceneX - sceneX_start;
                double height = sceneY - sceneY_start;

                hBox.setPrefWidth(width);
                hBox.setPrefHeight(height);

                label.setText("宽度：" + width + "; 高度：" + height + ";");
            }
        });

        // 当鼠标拖拽出矩形后，可以通过点击完成，得到截图
        an.setOnMouseDragExited(new EventHandler<MouseDragEvent>() {
            @Override
            public void handle(MouseDragEvent event) {
                sceneX_end = event.getSceneX();
                sceneY_end = event.getSceneY();

                Button btnFin = new Button("完成");
                hBox.getChildren().add(btnFin);
                hBox.setAlignment(Pos.BOTTOM_RIGHT);

                btnFin.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            getCodeContent();
                            privateStage.setIconified(false);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });
    }

    public void getCodeContent() throws Exception {
        // 关闭截图窗口
        screenStage.close();
        double w = sceneX_end - sceneX_start;
        double h = sceneY_end - sceneY_start;

        // 截图
        Robot robot = new Robot();
        Rectangle rectangle = new Rectangle((int) sceneX_start, (int) sceneY_start, (int) w, (int) h);
        BufferedImage bufferedImage = robot.createScreenCapture(rectangle);

        // 读取图片内容
        List<String> strings = QRCode.readFromCode(bufferedImage);
        for (String str : strings) {
            ta.appendText(str + "\n");
        }

    }
}
