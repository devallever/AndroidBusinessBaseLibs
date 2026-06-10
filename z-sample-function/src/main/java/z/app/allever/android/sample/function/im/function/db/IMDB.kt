package z.app.allever.android.sample.function.im.function.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.allever.android.lib.core.app.App
import z.app.allever.android.sample.function.im.function.db.dao.MessageDao
import z.app.allever.android.sample.function.im.function.db.dao.UserDao
import z.app.allever.android.sample.function.im.function.db.entity.MessageEntity
import z.app.allever.android.sample.function.im.user.UserInfo

@SuppressLint("StaticFieldLeak")
@Database(entities = [UserInfo::class, MessageEntity::class], version = 1)
abstract class IMDB : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao

    companion object {
        private var INS: IMDB? = null
        private var context: Context = App.context

        fun init(context: Context) {
            this.context = context.applicationContext
        }

        @Synchronized
        fun getIns(): IMDB {
            INS?.let {
                return it
            }

            return Room.databaseBuilder(
                context.applicationContext,
                IMDB::class.java,
                "im_db"
            )
                .build().apply {
                    INS = this
                }
        }
    }
}