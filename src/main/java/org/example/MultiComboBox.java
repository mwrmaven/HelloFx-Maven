package org.example;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Skin;


/**
 * @author mavenr
 * @Classname MultiComboBox
 * @Description 多选下拉框（依赖资料，https://cloud.tencent.com/developer/ask/189402）
 * @Date 2021/11/3 17:30
 */
public class MultiComboBox<T> {

    private ComboBox<T> cb;

    MultiComboBox() {
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
    }

    public void setPrefWidth(double width) {
        cb.setPrefWidth(width);
    }

    public void setItems(ObservableList<T> items) {
        cb.setItems(items);
    }

    public ObservableList<T> getItems() {
        ObservableList<T> items = cb.getItems();
        return items;
    }

}
