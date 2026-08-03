package app.allever.android.lucky.choice.spin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Wheel::class, Option::class], version = 1)
abstract class LuckSpinDatabase: RoomDatabase() {

    abstract fun wheelDao(): WheelDao
    abstract fun optionDao(): OptionDao

    companion object {
        const val DATABASE_NAME = "luckspin.db"

        private var INSTANCE: LuckSpinDatabase? = null

        fun getInstance(applicationContext: Context): LuckSpinDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    applicationContext,
                    LuckSpinDatabase::class.java,
                    DATABASE_NAME
                ).build()
                    .also { INSTANCE = it }
            }
    }
}