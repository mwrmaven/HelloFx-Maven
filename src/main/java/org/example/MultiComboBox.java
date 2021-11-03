package org.example;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;

import java.util.stream.Collectors;


/**
 * @author mavenr
 * @Classname MultiComboBox
 * @Description 多选下拉框（依赖资料，https://cloud.tencent.com/developer/ask/189402）
 * @Date 2021/11/3 17:30
 */
public class MultiComboBox<T extends CheckBox> {

    private ComboBox<T> cb;

    public ComboBox<T> format() {
        cb = new ComboBox<T>(){
            @Override
            protected Skin<?> createDefaultSkin() {
                return new ComboBoxListViewSkin<T>(this) {
                    // 设置点击时不隐藏
                    @Override
                    protected boolean isHideOnClickEnabled() {
                        return false;
                    }
                };
            }
        };

        // 设置button按钮上显示的文本
//        cb.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                cb.setButtonCell(new ListCell<T>() {
//                    @Override
//                    protected void updateItem(T item, boolean empty) {
//                        super.updateItem(item, empty);
//                        // 使用过滤，选出被选中的对象
//                        String selected = cb.getItems().stream().filter(CheckBox::isSelected)
//                                .map(CheckBox::getText)
//                                .collect(Collectors.joining(","));
//                        // 设置按钮的文本
//                        setText(selected);
//                    }
//                });
//            }
//        });
        return cb;
    }
}
