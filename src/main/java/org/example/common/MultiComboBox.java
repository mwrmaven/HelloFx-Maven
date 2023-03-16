package org.example.common;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.skin.ComboBoxListViewSkin;
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
                ComboBoxListViewSkin<T> tComboBoxListViewSkin = new ComboBoxListViewSkin<>(this);
                // 设置点击不隐藏
                tComboBoxListViewSkin.setHideOnClick(false);
                return tComboBoxListViewSkin;
            }
        };

        comboBox.setPrefWidth(prefWidth);
        // 设置下拉框的元素
        comboBox.setItems(items);
        // 设置下拉框的横框
        comboBox.setButtonCell(new ListCell<>());

        // 设置下拉元素的显示内容
        comboBox.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> param) {
                return new ListCell<T>(){
                    private CheckBox cb = new CheckBox();
                    private BooleanProperty booleanProperty;
                    {
                        // 为选择框 CheckBox 设置点击触发事件
                        cb.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                getListView().getSelectionModel().select(getItem());
                                // 使用过滤，选出被选中的对象
                                String selected = comboBox.getItems().stream().filter(T::isSelected)
                                        .map(T::getName)
                                        .collect(Collectors.joining(","));
                                comboBox.getButtonCell().setText(selected);
                            }
                        });
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

        // 设置样式
        comboBox.setStyle("-fx-background-color: #DBDBDB; -fx-background-radius: 4;");

        return comboBox;
    }


}
