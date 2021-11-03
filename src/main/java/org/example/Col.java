package org.example;

/**
 * @Author mavenr
 * @Classname  Col
 * @Description 可选择对象基础类，包含显示的名称以及被选中的状态
 * @Date 2021/11/3 11:13 下午
 */
public class Col {
    private String name;
    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
