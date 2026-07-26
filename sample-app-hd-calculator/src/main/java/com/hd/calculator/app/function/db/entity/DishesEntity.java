package com.hd.calculator.app.function.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dishes")
public class DishesEntity {

    //primarykey id
    @PrimaryKey(autoGenerate = true)
    private long id;

    //firstId
    private long firstId;
    //secondId
    private long secondId;
    //thirdId
    private long thirdId;
    private long firstSortType;
    //dishesId
    private long dishesId;
    //code
    private String code;
    //name
    private String name;
    //price
    private float price;
    //taxInHouse
    private double taxInHouse;
    //TaxTakeout
    private double taxTakeout;
    //buffetType
    private String buffetType;
    //enablePrint
    private boolean enablePrint;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFirstId() {
        return firstId;
    }

    public void setFirstId(long firstId) {
        this.firstId = firstId;
    }

    public long getSecondId() {
        return secondId;
    }

    public void setSecondId(long secondId) {
        this.secondId = secondId;
    }

    public long getThirdId() {
        return thirdId;
    }

    public void setThirdId(long thirdId) {
        this.thirdId = thirdId;
    }

    public long getDishesId() {
        return dishesId;
    }

    public void setDishesId(long dishesId) {
        this.dishesId = dishesId;
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

    public double getTaxInHouse() {
        return taxInHouse;
    }

    public void setTaxInHouse(double taxInHouse) {
        this.taxInHouse = taxInHouse;
    }

    public double getTaxTakeout() {
        return taxTakeout;
    }

    public void setTaxTakeout(double taxTakeout) {
        this.taxTakeout = taxTakeout;
    }

    public String getBuffetType() {
        return buffetType;
    }

    public void setBuffetType(String buffetType) {
        this.buffetType = buffetType;
    }

    public boolean isEnablePrint() {
        return enablePrint;
    }

    public void setEnablePrint(boolean enablePrint) {
        this.enablePrint = enablePrint;
    }

    public long getFirstSortType() {
        return firstSortType;
    }

    public void setFirstSortType(long firstSortType) {
        this.firstSortType = firstSortType;
    }
}
