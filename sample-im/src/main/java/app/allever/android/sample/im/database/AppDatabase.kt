package app.allever.android.sample.im.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.allever.android.sample.im.database.dao.UserDao
import app.allever.android.sample.im.database.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun init(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "local_im_db"
                        ).build()
                    }
                }
            }
        }

        fun getInstance(): AppDatabase {
            return INSTANCE ?: throw IllegalStateException("请先在 Application 中初始化 AppDatabase")
        }
    }
}