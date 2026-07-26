package com.hd.calculator.app.function.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.hd.calculator.app.function.db.entity.operation.BillDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillWithDishesRef;

import java.util.List;

@Dao
public interface BillRecordDao {

    //ADD
    @Insert
    void addBillRecord(BillRecordEntity billRecordEntity);

    //add BillDishesRecord
    @Insert
    void addBillDishesRecord(BillDishesRecordEntity billDishesRecordEntity);

    //getBillWithDishesList
    @Transaction
    @Query("SELECT * FROM bill_record")
    List<BillWithDishesRef> getBillWithDishesList();

    //getLastBillRecord
    @Query("SELECT * FROM bill_record ORDER BY id DESC LIMIT 1")
    BillRecordEntity getLastBillRecord();

    /***
     * getBillDishesRecordListBy billUserId, payType, createTime > startTime and createTime < endTime order by billCode asc
     * 跑堂当天类型账单
     * @return
     */
//    @Transaction
//    @Query("SELECT * FROM bill_record WHERE billUserId = :billUserId AND createTime >= :startTime AND createTime < :endTime ORDER BY " +
//            "CASE WHEN :sortBy = 'createTime' THEN createTime END, " +
//            "CASE WHEN :sortBy = 'tableCode' THEN tableCode END, " +
//            "CASE WHEN :sortBy = 'billCode' THEN billCode END, " +
//            "CASE WHEN :sortBy = 'amount' THEN amount END " +
//            "asc")
//    public List<BillWithDishesRef> getBillDishesRecordListByBillUserIdAndCreateTimeOrderBy(int billUserId, long startTime, long endTime, String sortBy, String sortOrder);
    @Transaction
    @RawQuery
    List<BillWithDishesRef> getBySql(SupportSQLiteQuery query);

    //getByTableCode order by createTime DESC
    @Transaction
    @Query("SELECT * FROM bill_record WHERE tableCode = :tableCode ORDER BY createTime DESC")
    List<BillWithDishesRef> getByTableCodeOrderByCreateTimeDesc(int tableCode);

    @Transaction
    @Query("SELECT * FROM bill_record WHERE id = :id")
    BillWithDishesRef getById(long id);

    @Update
    void updateBillRecord(BillRecordEntity billRecordEntity);


    //getBillDishesRecordListByBillUserIdAndCreateTime
    @Transaction
    @Query("SELECT * FROM bill_record WHERE billUserId = :billUserId AND createTime >= :startTime AND createTime < :endTime")
    List<BillWithDishesRef> getBillByUserIdAndCreateTime(long billUserId, long startTime, long endTime);

    //getFirstBill
    @Transaction
    @Query("SELECT * FROM bill_record ORDER BY createTime ASC LIMIT 1")
    BillWithDishesRef getFirstBill();

    //getByCreateTime
    @Transaction
    @Query("SELECT * FROM bill_record WHERE createTime >= :startTime AND createTime < :endTime ORDER BY createTime ASC")
    List<BillWithDishesRef> getByCreateTime(long startTime, long endTime);

    //deleteBeforeByTime
    @Query("DELETE FROM bill_record WHERE createTime < :time")
    void deleteBeforeByTime(long time);

    //deleteByCreateTime
    @Query("DELETE FROM bill_record WHERE createTime >= :startTime AND createTime < :endTime")
    void deleteByCreateTime(long startTime, long endTime);

}
