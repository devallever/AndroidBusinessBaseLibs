package com.clean.wood.data.model

import com.clean.wood.utils.Constant
import java.time.LocalDateTime

data class AdCache(
    val adPosition: Constant.AdPosition,
    val cachedTime: LocalDateTime,
    var adRes: Any
)