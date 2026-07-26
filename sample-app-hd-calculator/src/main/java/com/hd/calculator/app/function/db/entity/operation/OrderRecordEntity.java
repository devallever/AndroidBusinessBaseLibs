package com.hd.calculator.app.function.db.entity.operation;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_record")
public class OrderRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    //createTime
    private long createTime;
    //tableCode
    private int tableCode;
    //orderUserId
    private long orderUserId;
    //orderType
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

    public int getTableCode() {
        return tableCode;
    }

    public void setTableCode(int tableCode) {
        this.tableCode = tableCode;
    }

    public long getOrderUserId() {
        return orderUserId;
    }

    public void setOrderUserId(long orderUserId) {
        this.orderUserId = orderUserId;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }
}
