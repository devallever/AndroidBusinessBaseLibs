package com.hd.calculator.app.function.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef;

import java.util.List;

@Dao
public interface OrderRecordDao {

    @Insert
    void addOrderRecord(OrderRecordEntity orderRecordEntity);

    @Insert
    void addOrderDishesRecord(OrderDishesRecordEntity orderDishesRecordEntity);

    //getAllOrderRecord
    @Transaction
    @Query("SELECT * FROM order_record")
    List<OrderWithDishesRef> getAllOrderRecord();

    //getAllOrderRecord
    @Transaction
    @Query("SELECT * FROM order_record WHERE orderType = " + OrderType.ORDER_TYPE_IN_HOUSE +" order by createTime DESC")
    List<OrderWithDishesRef> getAllInHouseOrderRecord();

    //getByOrderId
    @Transaction
    @Query("SELECT * FROM order_record WHERE id = :id")
    OrderWithDishesRef getByOrderId(long id);

    //getByTableCode
    @Query("SELECT * FROM order_record WHERE tableCode = :tableCode")
    OrderRecordEntity getByTableCode(int tableCode);

    @Query("SELECT * FROM order_record WHERE tableCode = :tableCode AND orderType = " + OrderType.ORDER_TYPE_IN_HOUSE)
    OrderRecordEntity getInHouseByTableCode(int tableCode);

    //getTakeoutOrderByTableCode
    @Query("SELECT * FROM order_record WHERE tableCode = :tableCode AND orderType = " + OrderType.ORDER_TYPE_TAKE_OUT)
    OrderRecordEntity getTakeoutOrderByTableCode(int tableCode);

    //getLastOrderRecord
    @Query("SELECT * FROM order_record ORDER BY id DESC LIMIT 1")
    OrderRecordEntity getLastOrderRecord();

    //getOrderDishesListByOrderId
    @Query("SELECT * FROM order_dishes_record WHERE orderId = :orderId")
    List<OrderDishesRecordEntity> getOrderDishesListByOrderId(long orderId);

    @Transaction
    @Query("SELECT * FROM order_record WHERE tableCode = :tableCode")
    List<OrderWithDishesRef> getOrderWithDishesByTableCode(int tableCode);

    @Transaction
    @Query("SELECT * FROM order_record WHERE tableCode = :tableCode AND orderType = " + OrderType.ORDER_TYPE_IN_HOUSE)
    List<OrderWithDishesRef> getInHouseOrderWithDishesByTableCode(int tableCode);

    //updateOrderDishesRecord
    @Update
    void updateOrderDishesListRecord(List<OrderDishesRecordEntity> orderDishesRecordEntityList);

    @Update
    void updateOrderDishesRecord(OrderDishesRecordEntity orderDishesRecordEntity);


    //deleteOrder
    @Delete
    void deleteOrder(OrderRecordEntity orderRecordEntity);

    //deleteOrderRecordByOrderId
    @Query("DELETE FROM order_record WHERE id = :orderId")
    void deleteOrderRecordByOrderId(long orderId);

    @Delete
    void deleteOrderDishesRecord(OrderDishesRecordEntity orderDishesRecordEntity);

    //update
    @Update
    void update(OrderRecordEntity orderRecordEntity);

    @Update
    void updateOrderDishes(OrderDishesRecordEntity orderDishesRecordEntity);

    //查询所有orderType升序且 tableCode升序
    @Transaction
    @Query("SELECT * FROM order_record ORDER BY orderType ASC, tableCode ASC")
    List<OrderWithDishesRef> getUnPaidOrder();

    @Query("DELETE FROM order_dishes_record WHERE id = :id")
    void deleteOrderDishesById(long id);

    @Query("SELECT * FROM order_dishes_record WHERE id = :id")
    OrderDishesRecordEntity getOrderDishesById(long id);

    //deleteByTime
    @Query("DELETE FROM order_record WHERE createTime < :time")
    void deleteBeforeByTime(long time);

    //deleteOneOrder
    @Query("DELETE FROM order_record WHERE createTime >= :startTime and createTime < :endTime")
    void deleteByCreateTime(long startTime, long endTime);
}
