package com.hd.calculator.app.function.network.response;

import java.util.List;

public class ThirdCategoryData {
    private long id;
    private String name;
    private List<DishesData> dishesList;

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

    public List<DishesData> getDishesList() {
        return dishesList;
    }

    public void setDishesList(List<DishesData> dishesList) {
        this.dishesList = dishesList;
    }
}