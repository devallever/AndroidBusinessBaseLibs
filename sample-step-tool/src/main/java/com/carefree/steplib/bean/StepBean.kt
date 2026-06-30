package com.carefree.steplib.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.carefree.steplib.lib.ConstStep
import com.carefree.steplib.room.StepDbManager
import com.carefree.steplib.room.StepHourConverter


@Entity(tableName = StepDbManager.TABLE_NAME)
@TypeConverters(StepHourConverter::class)
data class StepBean(
    @PrimaryKey(autoGenerate = true) val id: Int,
    // 按天存储数据
    var todayYmd: String,
    var time: Long,
    var dayStep: Int,
    // 每天的步数目标
    var stepGoal: Int = ConstStep.STEP_GOAL_DEF,
    var hourStep: MutableList<StepHourBean> = mutableListOf(), // 存储每小时的数据

    //版本2 新增数据
    var dayStepBase: Int = 0 // 存储陀螺仪计算的步数
) {
    override fun toString(): String {
        return "StepEntity(id=$id, todayYmd='$todayYmd', time=$time, dayStep=$dayStep, dayStepBase=$dayStepBase, stepGoal=$stepGoal, hourStep=$hourStep)"
    }
}