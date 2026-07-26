package com.hd.calculator.app.function.db.entity.operation;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "bill_dishes_record",
        foreignKeys = {
                @ForeignKey(entity = BillRecordEntity.class,
                        parentColumns = "id",
                        childColumns = "billId",
                        onDelete = ForeignKey.CASCADE),
//                @ForeignKey(entity = DishesEntity.class,
//                        parentColumns = "code",
//                        childColumns = "dishesCode")
        })
public class BillDishesRecordEntity {

    //id
    @PrimaryKey(autoGenerate = true)
    private long id;
    //订单id
    @ColumnInfo(index = true)
    private long billId;
    private int count;
    private String dishesCode;

    private String remark;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBillId() {
        return billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDishesCode() {
        return dishesCode;
    }

    public void setDishesCode(String dishesCode) {
        this.dishesCode = dishesCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
