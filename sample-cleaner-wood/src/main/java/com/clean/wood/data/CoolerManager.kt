package com.clean.wood.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class CoolerManager private constructor() {
    companion object {
        val ins by lazy {
            CoolerManager()
        }
    }

    suspend fun scanning() = withContext(Dispatchers.IO) {
        delay(1000 * 6)
    }

    suspend fun cooling() = withContext(Dispatchers.IO) {
        delay(1000 * 6)
    }
}