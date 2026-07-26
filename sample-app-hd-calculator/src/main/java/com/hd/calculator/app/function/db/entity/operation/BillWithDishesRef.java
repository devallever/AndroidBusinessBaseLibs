package com.hd.calculator.app.function.db.entity.operation;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class BillWithDishesRef {
    @Embedded
    private BillRecordEntity bill;
    @Relation(parentColumn = "id", entityColumn = "billId", entity = BillDishesRecordEntity.class)
    private List<BillDishesRecordEntity> dishesList;

    public BillRecordEntity getOrder() {
        return bill;
    }

    public void setOrder(BillRecordEntity bill) {
        this.bill = bill;
    }

    public List<BillDishesRecordEntity> getDishesList() {
        return dishesList;
    }

    public void setDishesList(List<BillDishesRecordEntity> dishesList) {
        this.dishesList = dishesList;
    }

    public BillRecordEntity getBill() {
        return bill;
    }

    public void setBill(BillRecordEntity bill) {
        this.bill = bill;
    }
}
