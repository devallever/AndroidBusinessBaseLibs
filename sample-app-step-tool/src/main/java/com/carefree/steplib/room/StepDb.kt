package com.carefree.steplib.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.carefree.steplib.bean.StepBean

@Database(entities = [StepBean::class], version = 1 , exportSchema = true)
abstract class StepDb : RoomDatabase() {
    abstract fun stepDao(): StepDao
}