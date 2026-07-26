package com.hd.calculator.app.ui.item;

public class TicketDishesItem {
    private int count;
    private String name;//格式菜名(价格)
    private float cost;
    private String taxSign;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public String getTaxSign() {
        return taxSign;
    }

    public void setTaxSign(String taxSign) {
        this.taxSign = taxSign;
    }
}
