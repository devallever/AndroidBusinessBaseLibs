package com.clean.wood.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class BoosterManger private constructor() {
    companion object {
        val ins by lazy {
            BoosterManger()
        }
    }

    suspend fun scanning() = withContext(Dispatchers.IO) {
        delay(1000 * 6)
    }

    suspend fun boosting() = withContext(Dispatchers.IO) {
        delay(1000 * 6)
    }
}