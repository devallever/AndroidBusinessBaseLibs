package com.hd.calculator.app.function.db.entity.operation;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class OrderWithDishesRef {
    @Embedded
    private OrderRecordEntity order;
    @Relation(parentColumn = "id", entityColumn = "orderId", entity = OrderDishesRecordEntity.class)
    private List<OrderDishesRecordEntity> dishesList;

    public OrderRecordEntity getOrder() {
        return order;
    }

    public void setOrder(OrderRecordEntity order) {
        this.order = order;
    }

    public List<OrderDishesRecordEntity> getDishesList() {
        return dishesList;
    }

    public void setDishesList(List<OrderDishesRecordEntity> dishesList) {
        this.dishesList = dishesList;
    }
}
