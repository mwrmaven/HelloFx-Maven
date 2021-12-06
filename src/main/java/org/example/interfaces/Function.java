package org.example.interfaces;

import javafx.scene.layout.AnchorPane;

/**
 * @Author mavenr
 * @Classname  Function
 * @Description 功能接口
 * @Date 2021/12/6 9:32 下午
 */
public interface Function {
    /**
     * 标签页名称
     * @return
     */
    String tabName();

    /**
     * 标签的style
     * @return
     */
    String tabStyle();

    /**
     * 标签页中实际显示的布局
     * @return
     */
    AnchorPane tabPane();
}
