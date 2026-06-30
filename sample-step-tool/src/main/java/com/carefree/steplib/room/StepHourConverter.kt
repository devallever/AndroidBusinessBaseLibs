package com.carefree.steplib.room

import androidx.room.TypeConverter
import com.carefree.steplib.bean.StepHourBean
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 *classDes:
 *@author: CHL
 *create date: 2023/3/7
 */
class StepHourConverter {
    @TypeConverter
    fun stringToSomeObjectList(data: String?): MutableList<StepHourBean> {
        if (data == null) {
            return mutableListOf()
        }
        return Json.decodeFromString(data)
    }

    @TypeConverter
    fun someObjectListToString(someObjects: MutableList<StepHourBean>): String {
        return Json.encodeToString(someObjects)
    }
}