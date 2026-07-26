package com.hd.calculator.app.function.db.entity.operation;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reduce_dishes_record")
public class ReduceDishesRecordEntity {
    //id
    @PrimaryKey(autoGenerate = true)
    private long id;
    //createTime
    private long createTime;
    //dishesCode
    private String dishesCode;
    //count
    private int count;
    //tableCode
    private int tableCode;
    //userId
    private long userId;
    //orderId
    private long orderId;

    private int orderType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getDishesCode() {
        return dishesCode;
    }

    public void setDishesCode(String dishesCode) {
        this.dishesCode = dishesCode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTableCode() {
        return tableCode;
    }

    public void setTableCode(int tableCode) {
        this.tableCode = tableCode;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }
}
