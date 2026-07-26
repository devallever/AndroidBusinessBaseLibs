package com.hd.calculator.app.function.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hd.calculator.app.function.db.entity.TableEntity;

import java.util.List;


@Dao
public interface TableDao {

    //ADD
    @Insert
    void addTable(TableEntity tableEntity);

    //UPDATE
    @Update
    void updateTable(TableEntity tableEntity);

    //deleteAll
    @Query("DELETE FROM tables")
    void deleteAll();

    //deleteByZone
    @Query("DELETE FROM tables WHERE zone = :zone")
    void deleteByZone(int zone);

    //getByTableId
    @Query("SELECT * FROM tables WHERE tableId = :tableId")
    TableEntity getByTableId(int tableId);

    //getAll
    @Query("SELECT * FROM tables")
    List<TableEntity> getAll();

    //getByZone
    @Query("SELECT * FROM tables WHERE zone = :zone")
    List<TableEntity> getByZone(int zone);

    @Query("SELECT * FROM tables WHERE code = :tableCode")
    TableEntity getByTableCode(int tableCode);

}
