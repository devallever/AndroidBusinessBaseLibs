package com.clean.wood.data.model

import com.clean.wood.utils.Constant

data class JunkItem(
    val junkType: Constant.JunkType,
    val icon: Int,
    val label: String,
    var scanning: Boolean = true,
    var select: Boolean = false
)