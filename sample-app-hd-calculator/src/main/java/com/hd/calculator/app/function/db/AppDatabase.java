package com.hd.calculator.app.function.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hd.calculator.app.function.db.dao.AccountDao;
import com.hd.calculator.app.function.db.dao.BillDailyReportDao;
import com.hd.calculator.app.function.db.dao.BillRecordDao;
import com.hd.calculator.app.function.db.dao.DishesDao;
import com.hd.calculator.app.function.db.dao.DishesSortDao;
import com.hd.calculator.app.function.db.dao.OrderRecordDao;
import com.hd.calculator.app.function.db.dao.ReduceDishesRecordDao;
import com.hd.calculator.app.function.db.dao.TableDao;
import com.hd.calculator.app.function.db.dao.TaxDao;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.DishesSortEntity;
import com.hd.calculator.app.function.db.entity.TableEntity;
import com.hd.calculator.app.function.db.entity.TaxEntity;
import com.hd.calculator.app.function.db.entity.operation.BillDailyReportEntity;
import com.hd.calculator.app.function.db.entity.operation.BillDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.ReduceDishesRecordEntity;


@Database(entities = {
        AccountEntity.class,
        DishesEntity.class,
        DishesSortEntity.class,
        TaxEntity.class,
        TableEntity.class,
        OrderRecordEntity.class,
        OrderDishesRecordEntity.class,
        BillRecordEntity.class,
        BillDishesRecordEntity.class,
        ReduceDishesRecordEntity.class,
        BillDailyReportEntity.class},
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // 单例实例
    private static volatile AppDatabase INSTANCE;

    //getDatabase
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_database.db").fallbackToDestructiveMigration() // 开发阶段使用（生产环境移除）
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract AccountDao accountDao();

    //dishesDao
    public abstract DishesSortDao dishesSortDao();

    public abstract DishesDao dishesDao();

    public abstract TaxDao taxDao();

    //tableDao
    public abstract TableDao tableDao();

    public abstract OrderRecordDao orderRecordDao();

    public abstract BillRecordDao billRecordDao();

    public abstract ReduceDishesRecordDao reduceDishesRecordDao();

    public abstract BillDailyReportDao billDailyReportDao();
}
