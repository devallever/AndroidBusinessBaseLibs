package com.hd.calculator.app.function.network.response;

import java.util.List;

public class FirstCategoryData {
    private long id;
    private String name;
    private List<SecondCategoryData> children;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SecondCategoryData> getChildren() {
        return children;
    }

    public void setChildren(List<SecondCategoryData> children) {
        this.children = children;
    }
}