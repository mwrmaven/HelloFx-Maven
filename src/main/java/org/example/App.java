package org.example;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.example.common.Common;
import org.example.entity.ProcessInfo;
import org.example.init.Config;
import org.example.interfaces.Function;
import org.example.util.SocketUtil;
import org.example.util.Unit;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

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
        // 获取系统信息
        String osName = System.getProperty("os.name");
        System.out.println("系统：" + osName);
        if (osName.contains("Windows")) {
            // 窗口关闭时触发
            primaryStage.setOnCloseRequest(event -> {
                closeWindowsChromeDriver();
                // 关闭chrome测试浏览器端口
				SocketUtil.kill(9527);
            });
        } else if (osName.contains("Mac")){
            // 解决mac环境下jdk11运行javafx显示乱码问题
            scene.getRoot().setStyle("-fx-font-family: 'serif'; -fx-font-size: 14");

            // 窗口关闭时触发
            primaryStage.setOnCloseRequest(event -> {
                // 关闭chrome测试浏览器端口
                SocketUtil.kill(9527);
            });
        }
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
            AnchorPane anchorPane = ite.tabPane(primaryStage, width, h);
            tab.setContent(anchorPane);
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

    /**
     * 关闭所有的chromedriver进程
     */
    private void closeWindowsChromeDriver() {
        System.out.println("关闭窗口！");
        List<ProcessInfo> windowsProcessList = Unit.getProcessList();
        for (ProcessInfo pi : windowsProcessList) {
            if (pi.getInfo().startsWith("chromedriver")) {
                System.out.println("查询到chromedriver的相关进程id为：" + pi.getPid());
                try {
                    Runtime.getRuntime().exec("taskkill /f /t /pid " + pi.getPid());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
