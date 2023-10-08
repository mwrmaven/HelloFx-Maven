package org.example.button;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

/**
 * @author mavenr
 * @Classname BatchButton
 * @Description TODO 批量处理按钮
 * @Date 2021/11/12 16:44
 */
public class BatchButton {

    /**
     * 按钮上的文本
     */
    private String text = "批量处理";

    /**
     * 按钮的宽度
     */
    private double wight = 120;

    /**
     * 按钮的高度
     */
    private double height = 40;

    /**
     * 按钮底部颜色
     */
    private String bottomColorNum = "#9d4024";

    /**
     * 按钮上部颜色
     */
    private String topColorNum = "#d86e3a";

    /**
     * 按钮上字体大小
     */
    private int fontSize = 20;

    private Button batchButton = new Button();

    /**
     * 空构造
     */
    public BatchButton() {}

    /**
     *
     * @param text
     */
    public BatchButton(String text) {
        this.text = text;
    }

    /**
     *
     * @param text 按钮上的显示文本
     * @param wight 按钮宽度
     * @param height 按钮高度
     */
    public BatchButton(String text, double wight, double height) {
        this.text = text;
        this.wight = wight;
        this.height = height;
    }

    /**
     *
     * @param text 按钮上的显示文本
     * @param wight 按钮宽度
     * @param height 按钮高度
     * @param topColorNum 按钮上部的颜色
     * @param bottomColorNum 按钮下部的颜色
     * @param fontSize 按钮上字体的大小
     */
    public BatchButton(String text, double wight, double height, String topColorNum, String bottomColorNum, int fontSize) {
        this.text = text;
        this.wight = wight;
        this.height = height;
        this.topColorNum = topColorNum;
        this.bottomColorNum = bottomColorNum;
        this.fontSize = fontSize;
    }

    public Button createInstance() {
        return createInstance(text, fontSize, wight, height);
    }

    public Button createInstance(String text, int fontSize, double width, double height) {
        batchButton.setText(text);
        batchButton.setPrefWidth(width);
        batchButton.setPrefHeight(height);

        String originStyle = "-fx-background-color: " + bottomColorNum + ", " + bottomColorNum + ", " + topColorNum + ";" +
                "-fx-font-size: " + fontSize + "; -fx-text-fill: white; -fx-font-family: KaiTi;" +
                "-fx-background-radius: 8; -fx-padding: 8 15 15 15;" +
                "-fx-background-insets: 0, 0 0 5 0, 0 0 6 0, 0 0 7 0;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.75), 4, 0, 0, 1);" +
                "-fx-font-weight: bold";

        String pressedStyle = "-fx-padding: 10 15 13 15; -fx-background-radius: 8; " +
                "-fx-background-insets: 2 0 0 0,2 0 3 0, 2 0 4 0, 2 0 5 0;" +
                "-fx-font-size: " + fontSize + "; -fx-text-fill: white; -fx-font-family: KaiTi;";

        batchButton.setStyle(originStyle);
        batchButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                batchButton.setStyle(pressedStyle);
            }
        });
        batchButton.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                batchButton.setStyle(originStyle);
            }
        });

        return batchButton;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getWight() {
        return wight;
    }

    public void setWight(double wight) {
        this.wight = wight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getBottomColorNum() {
        return bottomColorNum;
    }

    public void setBottomColorNum(String bottomColorNum) {
        this.bottomColorNum = bottomColorNum;
    }

    public String getTopColorNum() {
        return topColorNum;
    }

    public void setTopColorNum(String topColorNum) {
        this.topColorNum = topColorNum;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
}
