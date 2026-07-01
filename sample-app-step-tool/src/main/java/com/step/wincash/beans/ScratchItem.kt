package com.step.wincash.beans

data class ScratchItem(
    val text: Int,
    val level: Int,
    var revealed: Boolean = false   // 是否已被清除遮罩
)