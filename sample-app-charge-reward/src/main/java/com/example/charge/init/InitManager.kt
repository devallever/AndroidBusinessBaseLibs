package com.example.charge.init

import java.util.Locale

object InitManager {
    fun getCountryCode(): String {
        return Locale.getDefault().country
    }
}