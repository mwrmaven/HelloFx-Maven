package org.example.common;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.util.stream.Collectors;


/**
 * @author mavenr
 * @Classname MultiComboBox
 * @Description 多选下拉框（依赖资料，https://cloud.tencent.com/developer/ask/189402）
 * @Date 2021/11/3 17:30
 */
public class MultiComboBox<T extends Col> {

    public ComboBox<T> createComboBox(ObservableList<T> items, double prefWidth) {
        ComboBox<T> comboBox = new ComboBox<T>(items){
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

        comboBox.setPrefWidth(prefWidth);
        comboBox.setItems(items);

        // 设置下拉元素的显示内容
        comboBox.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> param) {
                return new ListCell<T>(){
                    private CheckBox cb = new CheckBox();
                    private BooleanProperty booleanProperty;
                    {
                        cb.setOnAction(e -> getListView().getSelectionModel().select(getItem()));
                    }

                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            if (booleanProperty != null) {
                                // 解绑多选框的选择状态
                                cb.selectedProperty().unbindBidirectional(booleanProperty);
                            }
                            // 获取下拉项目中项目的选择状态
                            booleanProperty = item.selectedProperty();
                            // 设置多选框的选择状态
                            cb.selectedProperty().bindBidirectional(booleanProperty);
                            setGraphic(cb);
                            setText(item.getName());
                        } else {
                            setGraphic(null);
                            setText(null);
                        }
                    }
                };
            }
        });

        // 设置选项按钮上的显示内容
        comboBox.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                // 使用过滤，选出被选中的对象
                String selected = comboBox.getItems().stream().filter(T::isSelected)
                        .map(T::getName)
                        .collect(Collectors.joining(","));
                // 设置按钮的文本---------------------------------------------------------
                setText(selected);
            }
        });

        // 设置样式
        comboBox.setStyle("-fx-background-color: #DBDBDB; -fx-background-radius: 4;");

        return comboBox;
    }


}
