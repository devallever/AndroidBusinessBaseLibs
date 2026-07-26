package com.hd.calculator.app.function.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hd.calculator.app.function.db.entity.DishesEntity;

import java.util.List;

@Dao
public interface DishesDao {

    //ADD
    @Insert
    void addDishes(DishesEntity dishesEntity);

    //UPDATE
    @Update
    void updateDishes(DishesEntity dishesEntity);

    //getDishesListByThirdSortId
    @Query("SELECT * FROM dishes WHERE thirdId = :thirdId")
    List<DishesEntity> getDishesListByThirdId(long thirdId);

    //getByDishesId
    @Query("SELECT * FROM dishes WHERE dishesId = :dishesId")
    DishesEntity getByDishesId(int dishesId);

    //getByCode
    @Query("SELECT * FROM dishes WHERE code = :code")
    DishesEntity getByCode(String code);

    //getAll
    @Query("SELECT * FROM dishes")
    List<DishesEntity> getAll();


    //DELETE ALL
    @Query("DELETE FROM dishes")
    void deleteAll();

}
