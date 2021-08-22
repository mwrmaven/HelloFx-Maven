package org.example;

import javafx.application.Application;
import javafx.event.ActionEvent;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 */
public class App extends Application {
    private final Desktop desktop = Desktop.getDesktop();
    public static void main(String[] args) {
        launch(App.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Button openButton = new Button("打开文件夹");
        openButton.setOnAction((ActionEvent e) -> {
            File file = directoryChooser.showDialog(stage);
            if (file != null) {
                System.out.println(file.getAbsoluteFile());
            } else {
                System.out.println("请选择文件夹");
                AlertWindow.alter("请选择文件夹");
            }
        });

        GridPane inputGridPane = new GridPane();
        GridPane.setConstraints(openButton, 0, 0);
        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.getChildren().addAll(openButton);

        Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(inputGridPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));

        Scene scene = new Scene(rootGroup, 640, 360);

        stage.setScene(scene);
        stage.setTitle("HelloFx Maven Test");
        stage.show();
    }
}
