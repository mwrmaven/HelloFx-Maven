package org.example;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @Author mavenr
 * @Classname  Col
 * @Description 可选择对象基础类，包含显示的名称以及被选中的状态
 * @Date 2021/11/3 11:13 下午
 */
public class Col {
    private String name;
    private Integer index;
    private BooleanProperty selected = new SimpleBooleanProperty();


    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}
