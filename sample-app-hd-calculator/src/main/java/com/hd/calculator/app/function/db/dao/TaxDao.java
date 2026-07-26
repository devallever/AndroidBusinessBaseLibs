package com.hd.calculator.app.function.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hd.calculator.app.function.db.entity.TaxEntity;

import java.util.List;

@Dao
public interface TaxDao {

    //ADD
    @Insert
    void addTax(TaxEntity taxEntity);

    //UPDATE
    @Update
    void updateTax(TaxEntity taxEntity);

    //getTaxByType
    @Query("SELECT * FROM tax WHERE type = :taxType")
    List<TaxEntity> getTaxByType(int taxType);

    //getAll
    @Query("SELECT * FROM tax")
    List<TaxEntity> getAll();

    //deleteAll
    @Query("DELETE FROM tax")
    void deleteAll();
}
