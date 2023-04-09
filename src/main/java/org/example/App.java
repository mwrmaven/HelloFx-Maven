package org.example;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.common.Common;
import org.example.init.Config;
import org.example.interfaces.Function;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author mavenr
 * @Classname App
 * @Description TODO
 * @Date 2021/12/7 10:27
 */
public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 读取配置文件
        Config.createProperties();

        // 获取屏幕的宽、高
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        double w = width / 2 + 200;
        double h = height / 2 + 360;

        // 使用TabPane，页面切换
        TabPane tp = new TabPane();
        tp.setStyle("-fx-font-size: 14;");
        tp.setTabMinHeight(50);

        // 场景配置
        Scene scene = new Scene(tp);
        // 解决显示乱码问题
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        primaryStage.setScene(scene);
        primaryStage.setTitle(Common.STAGE_TITLE);
        primaryStage.getIcons().add(new Image("image/folder.png"));
        primaryStage.setWidth(w);
        primaryStage.setHeight(h);
        primaryStage.setMinWidth(w);
        primaryStage.setMinHeight(h);
        primaryStage.setMaxWidth(w);
        primaryStage.setMaxHeight(h);
        primaryStage.show();

        ServiceLoader<Function> allTabs = ServiceLoader.load(Function.class);
        Iterator<Function> iterator = allTabs.iterator();
        while (iterator.hasNext()) {
            Function ite = iterator.next();

            Tab tab = new Tab(ite.tabName());
            tab.setStyle(ite.tabStyle());
            tab.setClosable(false);
            tab.setContent(ite.tabPane(primaryStage, width, h));
            tp.getTabs().add(tab);

            // 设置默认的标签页
            if (Common.TOP_BUTTON_5.equals(ite.tabName())) {
                tp.getSelectionModel().select(tab);
            }
        }

        // 设置切换tab时高度的设置
        tp.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                // 改变标签的状态
                String newStyle = newValue.getStyle();
                newValue.setStyle(newStyle.replace(" -fx-pref-height: 30; ", " -fx-pref-height: 40; "));

                String oldStyle = oldValue.getStyle();
                oldValue.setStyle(oldStyle.replace(" -fx-pref-height: 40; ", " -fx-pref-height: 30; "));
            }
        });
    }
}
