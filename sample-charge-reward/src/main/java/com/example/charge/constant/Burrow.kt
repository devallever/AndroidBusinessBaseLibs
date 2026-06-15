package com.example.charge.constant

import com.example.charge.currency.CurrencyType

//地洞对象
data class Burrow(
    var type : BurrowType = BurrowType.BURROW,
    var num : Float = 0f,
    var awareType : CurrencyType = CurrencyType.GOLD,
    var needSeeAd: Boolean = false
)

fun getDefaultBurrowList(): MutableList<Burrow> {
    val list = mutableListOf<Burrow>()
    repeat(15){
        list.add(Burrow())
    }
    return list
}

enum class BurrowType {
    BURROW, // 地洞
    MOLE,   // 地鼠
    BOMB    // 炸弹
}