package com.example.charge.data

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.FloatRange
import com.example.charge.constant.FloatIconType
import com.example.charge.utils.nextFloat
import kotlin.random.Random
import kotlin.random.nextInt

class FloatIconData {
    var type = 0
    var value = 0f

    companion object {
        fun generate(): FloatIconData {
            val data = FloatIconData()
            data.type = Random.nextInt(FloatIconType.GOLD..FloatIconType.GREEN_AD)
            when (data.type) {
                FloatIconType.GOLD -> data.value = Random.nextInt(10..100).toFloat()
                FloatIconType.GREEN -> data.value = Random.nextFloat(0.01f, 0.1f)
                FloatIconType.SPEED -> data.value = 30f
                FloatIconType.GREEN_AD -> data.value = Random.nextFloat(1f, 2f)
            }
            return data
        }

        fun generateNoAd(): FloatIconData {
            val data = FloatIconData()
            data.type = Random.nextInt(FloatIconType.GOLD..FloatIconType.SPEED)
            when (data.type) {
                FloatIconType.GOLD -> data.value = Random.nextInt(10..100).toFloat()
                FloatIconType.GREEN -> data.value = Random.nextFloat(0.01f, 0.1f)
                FloatIconType.SPEED -> data.value = 30f
            }
            return data
        }
    }
}