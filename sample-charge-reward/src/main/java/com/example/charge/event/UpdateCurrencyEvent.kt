package com.example.charge.event

import com.example.charge.currency.CurrencyType


data class UpdateCurrencyEvent(val currencyType: CurrencyType = CurrencyType.GOLD, val sender: Any = Any())

