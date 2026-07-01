package com.carefree.steplib.room

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object StepDbManager {
    private const val DB_NAME = "StepLib.db"
    const val TABLE_NAME = "step_lib"


    @Volatile
    private var stepDb: StepDb? = null

    fun stepDB(context: Context): StepDb {
        return stepDb ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                StepDb::class.java,
                DB_NAME
            )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
            stepDb = instance
            instance
        }
    }
}