package com.hd.calculator.app.ui.item;

public class MainTableItem {
    //id
    private int tableCode;
    //time
    private long time;
    //water
    private int dishesCount;
    //count
    private String buffetCount;
    //cost, float
    private Float cost;
    //used
    private boolean used;

    private long orderId;

    private int adultCount;

    private int childCount;

    private int babyCount;

    private boolean onLine;

    private long userId;

    private int orderType;

    public int getTableCode() {
        return tableCode;
    }

    public void setTableCode(int tableCode) {
        this.tableCode = tableCode;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getDishesCount() {
        return dishesCount;
    }

    public void setDishesCount(int dishesCount) {
        this.dishesCount = dishesCount;
    }

    public String getBuffetCount() {
        return buffetCount;
    }

    public void setBuffetCount(String buffetCount) {
        this.buffetCount = buffetCount;
    }

    public Float getCost() {
        return cost;
    }

    public void setCost(Float cost) {
        this.cost = cost;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public int getAdultCount() {
        return adultCount;
    }

    public void setAdultCount(int adultCount) {
        this.adultCount = adultCount;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public int getBabyCount() {
        return babyCount;
    }

    public void setBabyCount(int babyCount) {
        this.babyCount = babyCount;
    }

    public boolean isOnLine() {
        return onLine;
    }

    public void setOnLine(boolean onLine) {
        this.onLine = onLine;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }
}
