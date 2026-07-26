package com.hd.calculator.app.function.db.entity.operation;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bill_daily_report")
public class BillDailyReportEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String time;//yyyy-MM-dd
    private float amount;
    //最后一张账单时间
    private long lastBillTime;

    private long createTime;

    /**
     * 账单编号 总是上一个账单号+1，默认1
     */
    private int code;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public long getLastBillTime() {
        return lastBillTime;
    }

    public void setLastBillTime(long lastBillTime) {
        this.lastBillTime = lastBillTime;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
