package com.hd.calculator.app.function.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.hd.calculator.app.function.db.entity.operation.BillDailyReportEntity;

import java.util.List;

@Dao
public interface BillDailyReportDao {
    @Insert
    void addBillDailyReport(BillDailyReportEntity billDailyReportEntity);

    //getAll
    @Query("SELECT * FROM bill_daily_report")
    List<BillDailyReportEntity> getAll();

    //getByTime

    /***
     *
     * @param time yyyy-MM-dd
     * @return
     */
    @Query("SELECT * FROM bill_daily_report WHERE time = :time")
    BillDailyReportEntity getByTime(String time);

    //getLast
    @Query("SELECT * FROM bill_daily_report ORDER BY code ASC LIMIT 1")
    BillDailyReportEntity getLast();

    //deleteBeforeByTime
    @Query("DELETE FROM bill_daily_report WHERE createTime < :time")
    void deleteBeforeByTime(long time);

    //deleteByCreateTime
    @Query("DELETE FROM bill_daily_report WHERE createTime >= :startTime and createTime < :endTime")
    void deleteByCreateTime(long startTime, long endTime);
}
