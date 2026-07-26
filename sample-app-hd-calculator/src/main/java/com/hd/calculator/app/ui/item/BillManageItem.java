package com.hd.calculator.app.ui.item;

import java.util.ArrayList;
import java.util.List;

public class BillManageItem {
    //orderId || billId
    private long id;
    //billCode
    private int billCode;
    //tableId
    private int tableCode;
    //cost
    private float cost;
    private float payTotal;
    //waiterId
    private long waiterId;
    //waiterName
    private String waiterName;
    //orderTime
    private long orderTime;

    private int orderType;

    private boolean fromDeleteTable;

    private boolean isCanceled;

    private int payType;

    private List<DishesItem> dishesList = new ArrayList<>();

    //expend
    private boolean expend;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTableCode() {
        return tableCode;
    }

    public void setTableCode(int tableCode) {
        this.tableCode = tableCode;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public long getWaiterId() {
        return waiterId;
    }

    public void setWaiterId(long waiterId) {
        this.waiterId = waiterId;
    }

    public String getWaiterName() {
        return waiterName;
    }

    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName;
    }

    public long getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(long orderTime) {
        this.orderTime = orderTime;
    }

    public List<DishesItem> getDishesList() {
        return dishesList;
    }

    public void setDishesList(List<DishesItem> dishesList) {
        this.dishesList = dishesList;
    }

    public boolean isExpend() {
        return expend;
    }

    public void setExpend(boolean expend) {
        this.expend = expend;
    }

    public float getPayTotal() {
        return payTotal;
    }

    public void setPayTotal(float payTotal) {
        this.payTotal = payTotal;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public boolean isFromDeleteTable() {
        return fromDeleteTable;
    }

    public void setFromDeleteTable(boolean fromDeleteTable) {
        this.fromDeleteTable = fromDeleteTable;
    }

    public int getBillCode() {
        return billCode;
    }

    public void setBillCode(int billCode) {
        this.billCode = billCode;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }
}
