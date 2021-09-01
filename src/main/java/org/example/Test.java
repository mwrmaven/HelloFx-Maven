package org.example;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author mavenr
 * @Classname Test
 * @Description TODO
 * @Date 2021/9/1 9:51
 */
public class Test extends Application {
    static boolean isManaged = false;
    static boolean isVisible = false;
    static int opacityValue = 0;
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {

        Button button1 = new Button("b1");
        Button button2 = new Button("b2");
        Button button3 = new Button("b3");
        Button button4 = new Button("b2Managed " + isManaged);
        Button button5 = new Button("b2Visible " + isVisible);
        Button button6 = new Button("b2Opacity " + opacityValue);
        button4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                button2.setManaged(isManaged);
                isManaged = !isManaged;
                button4.setText("b2Managed " + isManaged);
            }
        });
        button5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                button2.setManaged(isVisible);
                isVisible = !isVisible;
                button5.setText("b2Visible " + isVisible);
            }
        });
        button6.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                button2.setOpacity(opacityValue);
                opacityValue = 1 - opacityValue;
                button6.setText("b2Opacity " + opacityValue);
            }
        });

        HBox hBox1 = new HBox();
        hBox1.getChildren().addAll(button1, button2, button3);

        HBox hBox2 = new HBox();
        hBox2.getChildren().addAll(button4, button5, button6);
        hBox2.setLayoutY(100);

        AnchorPane ap = new AnchorPane();
        ap.getChildren().addAll(hBox1, hBox2);

        Scene scene = new Scene(ap);
        primaryStage.setScene(scene);
        primaryStage.setTitle("javafx");
        primaryStage.setWidth(800);
        primaryStage.setHeight(800);
        primaryStage.show();
    }
}
