package com.hd.calculator.app.function.db.entity.operation;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bill_record")
public class BillRecordEntity {
    public static final String COLUMN_CREATE_TIME = "createTime";
    public static final String COLUMN_BILL_CODE = "billCode";
    public static final String COLUMN_TABLE_CODE = "tableCode";
    public static final String COLUMN_AMOUNT = "amount";
    //amount

    @PrimaryKey(autoGenerate = true)
    private long id;
    private int serverId;
    private long createTime;
    private int tableCode;
    private long billUserId;
    private int billCode;
    private float amount;
    private float payTotal;
    private float tipsTotal;
    private int payType;
    private int orderType;
    private boolean fromDeleteTable;
    private boolean canceled;
    private boolean upload;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
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

    public long getBillUserId() {
        return billUserId;
    }

    public void setBillUserId(long billUserId) {
        this.billUserId = billUserId;
    }

    public int getBillCode() {
        return billCode;
    }

    public void setBillCode(int billCode) {
        this.billCode = billCode;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getPayTotal() {
        return payTotal;
    }

    public void setPayTotal(float payTotal) {
        this.payTotal = payTotal;
    }

    public float getTipsTotal() {
        return tipsTotal;
    }

    public void setTipsTotal(float tipsTotal) {
        this.tipsTotal = tipsTotal;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
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

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
