package com.hd.calculator.app.function.network.post;

import com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef;

public class TableUseData {
    private OrderWithDishesRef order;

    public OrderWithDishesRef getOrder() {
        return order;
    }

    public void setOrder(OrderWithDishesRef order) {
        this.order = order;
    }
}
