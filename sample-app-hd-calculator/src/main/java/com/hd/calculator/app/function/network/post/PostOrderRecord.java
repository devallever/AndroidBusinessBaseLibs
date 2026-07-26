package com.hd.calculator.app.function.network.post;

import java.util.List;

public class PostOrderRecord {
    private long createTime;
    private int tableCode;
    private int orderUserId;
    private int orderType;
    private List<PostDishesData> dishesList;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getTableCode() {
        return tableCode;
    }

    public void setTableCode(int tableCode) {
        this.tableCode = tableCode;
    }

    public int getOrderUserId() {
        return orderUserId;
    }

    public void setOrderUserId(int orderUserId) {
        this.orderUserId = orderUserId;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public List<PostDishesData> getDishesList() {
        return dishesList;
    }

    public void setDishesList(List<PostDishesData> dishesList) {
        this.dishesList = dishesList;
    }
}
