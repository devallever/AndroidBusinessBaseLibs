package com.carefree.steplib.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.carefree.steplib.bean.StepBean
import com.carefree.steplib.bean.StepHourBean
import com.carefree.steplib.utils.DateUtils
import kotlin.math.max

@Dao
interface StepDao {
    @Query("SELECT * FROM ${StepDbManager.TABLE_NAME}")
    fun getAll(): MutableList<StepBean>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg steps: StepBean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bean: StepBean)

    @Delete
    fun delete(step: StepBean)

    @Query("SELECT * FROM ${StepDbManager.TABLE_NAME} WHERE todayYmd = :todayYmd")
    fun getStepByDate(todayYmd: String): StepBean?

    @Query("select * from ${StepDbManager.TABLE_NAME} where time>=:startTime and time <=:endTime")
    fun getStepByFilter(startTime: Long, endTime: Long): MutableList<StepBean>

    @Transaction
    fun upStep(stepBean: StepBean){
        val stepByDate = getStepByDate(stepBean.todayYmd)
        if (stepByDate !=null){
            var addStep = max( stepBean.dayStep - stepByDate.dayStep,0)
            if (stepBean.dayStep!=-1){
                addStep = max( stepBean.dayStep - stepByDate.dayStep,0)
                stepByDate.dayStep = stepBean.dayStep
            }
            if (stepBean.dayStepBase!=-1){
                addStep = max( stepBean.dayStepBase- stepByDate.dayStepBase,0)
                stepByDate.dayStepBase = stepBean.dayStepBase
            }

            stepByDate.time = stepBean.time
            val hour = DateUtils.getHourFromTimestamp(stepBean.time)
            if (stepByDate.hourStep.isNotEmpty()){
                var hasStep = false
                for (bean in stepByDate.hourStep){
                    if (bean.hour == hour){
                        bean.step += addStep
                        hasStep = true
                        break
                    }
                }
                if (!hasStep){
                    stepByDate.hourStep.add(StepHourBean(hour,addStep))
                }
            }else {
                stepByDate.hourStep.add(StepHourBean(hour,addStep))
            }
            insert(stepByDate)
        }else {
            if (stepBean.dayStepBase<0){
                stepBean.dayStepBase = 0
            }
            if (stepBean.dayStep<0){
                stepBean.dayStep = 0
            }
            insert(stepBean)
        }
    }

    @Transaction
    fun upStepGoal(stepBean: StepBean){
        val stepByDate = getStepByDate(stepBean.todayYmd)
        if (stepByDate!=null){
            stepByDate.stepGoal = stepBean.stepGoal
            insert(stepByDate)
        }else {
            insert(stepBean)
        }
    }
}