package com.hd.calculator.app.function.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hd.calculator.app.function.db.entity.DishesSortEntity;

import java.util.List;

@Dao
public interface DishesSortDao {

    //ADD
    @Insert
    void addDishesSort(DishesSortEntity dishesSortEntity);

    @Update
    void updateDishesSort(DishesSortEntity dishesSortEntity);

    //getFirstLevelDishesSortList
    @Query("SELECT * FROM dishes_sort WHERE level = 1")
    List<DishesSortEntity> getFirstLevelSortList();

    //getSecondLevelDishesSortListByFirstId
    @Query("SELECT * FROM dishes_sort WHERE level = 2 AND parentSortId = :parentSortId")
    List<DishesSortEntity> getSecondLevelSortListByFirstId(long parentSortId);

    //getThirdLevelDishesSortListBySecondId
    @Query("SELECT * FROM dishes_sort WHERE level = 3 AND parentSortId = :parentSortId")
    List<DishesSortEntity> getThirdLevelSortListBySecondId(long parentSortId);

    //getBySortId
    @Query("SELECT * FROM dishes_sort WHERE parentSortId = :parentSortId")
    DishesSortEntity getBySortId(int parentSortId);

    //getAll
    @Query("SELECT * FROM dishes_sort")
    List<DishesSortEntity> getAll();

    //delete all
    @Query("DELETE FROM dishes_sort")
    void deleteAll();
}
