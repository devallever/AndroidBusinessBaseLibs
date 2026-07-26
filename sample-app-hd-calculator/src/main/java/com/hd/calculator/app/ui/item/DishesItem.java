package com.hd.calculator.app.ui.item;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class DishesItem implements Parcelable {
    public static final Creator<DishesItem> CREATOR = new Creator<DishesItem>() {
        @Override
        public DishesItem createFromParcel(Parcel in) {
            return new DishesItem(in);
        }

        @Override
        public DishesItem[] newArray(int size) {
            return new DishesItem[size];
        }
    };
    //如果是已经出单，id != 0, OrderDishesEntity的id
    private long id;
    private String code;
    private String name;
    private float price;
    private int count;
    private boolean ordered;
    private boolean canceled;
    private String remark;
    private long firstSortType;
    private boolean enablePrint;

    public DishesItem() {
    }

    protected DishesItem(Parcel in) {
        id = in.readLong();
        code = in.readString();
        name = in.readString();
        price = in.readFloat();
        count = in.readInt();
        ordered = in.readByte() != 0;
        canceled = in.readByte() != 0;
        remark = in.readString();
        firstSortType = in.readLong();
        enablePrint = in.readByte() != 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public long getFirstSortType() {
        return firstSortType;
    }

    public void setFirstSortType(long firstSortType) {
        this.firstSortType = firstSortType;
    }

    public boolean isEnablePrint() {
        return enablePrint;
    }

    public void setEnablePrint(boolean enablePrint) {
        this.enablePrint = enablePrint;
    }

    public DishesItem copy() {
        DishesItem item = new DishesItem();
        item.setId(id);
        item.setCode(code);
        item.setName(name);
        item.setPrice(price);
        item.setCount(1);
        item.setOrdered(false);
        item.setRemark(remark);
        item.setCanceled(canceled);
        item.setFirstSortType(firstSortType);
        item.setEnablePrint(enablePrint);
        return item;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(code);
        dest.writeString(name);
        dest.writeFloat(price);
        dest.writeInt(count);
        dest.writeByte((byte) (ordered ? 1 : 0));
        dest.writeByte((byte) (canceled ? 1 : 0));
        dest.writeString(remark);
        dest.writeLong(firstSortType);
        dest.writeByte((byte) (enablePrint ? 1 : 0));
    }
}
