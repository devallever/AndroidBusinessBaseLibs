package com.hd.calculator.app.ui.item;

public class MainUnpaidTableItem {
    //id
    private long orderId;
    private int tableCode;
    //time
    private long time;
    //count
    private int count;
    //cost, float
    private float cost;
    private String waiter;

    //12E/5K/2B
    private String buffetCountDisplay;

    private int orderType;


    private int adultCount;

    private int childCount;

    private int babyCount;


    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public String getWaiter() {
        return waiter;
    }

    public void setWaiter(String waiter) {
        this.waiter = waiter;
    }

    public String getBuffetCountDisplay() {
        return buffetCountDisplay;
    }

    public void setBuffetCountDisplay(String buffetCountDisplay) {
        this.buffetCountDisplay = buffetCountDisplay;
    }

    public int getTableCode() {
        return tableCode;
    }

    public void setTableCode(int tableCode) {
        this.tableCode = tableCode;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
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
}
