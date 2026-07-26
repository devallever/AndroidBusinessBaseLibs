package com.hd.calculator.app.function.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.hd.calculator.app.function.db.entity.operation.ReduceDishesRecordEntity;

import java.util.List;

@Dao
public interface ReduceDishesRecordDao {
    //ADD
    @Insert
    void add(ReduceDishesRecordEntity entity);

    //getListByCreateTime createTime >= ? and createTime < ?
    @Query("SELECT * FROM reduce_dishes_record WHERE createTime >= :startTime and createTime < :endTime")
    List<ReduceDishesRecordEntity> getListByCreateTime(long startTime, long endTime);

    //getAllList
    @Query("SELECT * FROM reduce_dishes_record")
    List<ReduceDishesRecordEntity> getAllList();

    //deleteBeforeByTime
    @Query("DELETE FROM reduce_dishes_record WHERE createTime < :time")
    void deleteBeforeByTime(long time);

    //deleteByCreateTime
    @Query("DELETE FROM reduce_dishes_record WHERE createTime >= :startTime and createTime < :endTime")
    void deleteByCreateTime(long startTime, long endTime);
}
