package com.teach.javafx.request;

import java.util.Map;

/**
 * 选项项
 */
public class OptionItem {
    private Integer id;
    private String value;
    private String label;
    private String title;

    public OptionItem() {
    }

    public OptionItem(Integer id, String value, String label) {
        this.id = id;
        this.value = value;
        this.label = label;
        this.title = label; // 设置title与label相同
    }

    public OptionItem(Map<String, Object> map) {
        if (map != null) {
            // 使用安全的类型转换
            Object idObj = map.get("id");
            if (idObj instanceof Integer) {
                this.id = (Integer) idObj;
            } else if (idObj != null) {
                try {
                    this.id = Integer.parseInt(idObj.toString());
                } catch (NumberFormatException e) {
                    this.id = null;
                }
            }

            Object valueObj = map.get("value");
            this.value = valueObj != null ? valueObj.toString() : null;

            Object labelObj = map.get("label");
            this.label = labelObj != null ? labelObj.toString() : null;

            Object titleObj = map.get("title");
            this.title = titleObj != null ? titleObj.toString() : this.label;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return title != null ? title : label; // 如果title为空，返回label
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return label != null ? label : (title != null ? title : "");
    }
}
