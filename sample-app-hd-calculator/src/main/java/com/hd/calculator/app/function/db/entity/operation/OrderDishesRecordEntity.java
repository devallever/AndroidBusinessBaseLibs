package com.hd.calculator.app.function.db.entity.operation;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_dishes_record",
        foreignKeys = {
                @ForeignKey(entity = OrderRecordEntity.class,
                        parentColumns = "id",
                        childColumns = "orderId",
                        onDelete = ForeignKey.CASCADE),
//                @ForeignKey(entity = DishesEntity.class,
//                        parentColumns = "code",
//                        childColumns = "dishesCode")
        })
public class OrderDishesRecordEntity {
    //id
    @PrimaryKey(autoGenerate = true)
    private long id;
    //订单id
    @ColumnInfo(index = true)
    private long orderId;
    private int count;
    private String dishesCode;

    private String remark;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
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
