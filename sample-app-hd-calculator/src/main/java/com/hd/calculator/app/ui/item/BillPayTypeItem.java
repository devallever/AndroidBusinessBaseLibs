package com.hd.calculator.app.ui.item;

public class BillPayTypeItem {
    //count
    private int count;
    //cost
    private float cost;

    private int payType;

    private String payTypeName;

    private boolean select;

    public BillPayTypeItem(int count, float cost, int payType, String payTypeName, boolean select) {
        this.count = count;
        this.cost = cost;
        this.payType = payType;
        this.payTypeName = payTypeName;
        this.select = select;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    // getter and setter

    public String getPayTypeName() {
        return payTypeName;
    }

    public void setPayTypeName(String payTypeName) {
        this.payTypeName = payTypeName;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
