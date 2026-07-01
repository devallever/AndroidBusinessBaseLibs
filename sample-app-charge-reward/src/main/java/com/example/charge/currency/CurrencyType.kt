package com.example.charge.currency

enum class CurrencyType(val type: Int) {
    GOLD(1),
    GREEN(2);

    companion object {
        fun fromValue(value: Int): CurrencyType =
            entries.find { it.type == value } ?: GOLD
    }
}