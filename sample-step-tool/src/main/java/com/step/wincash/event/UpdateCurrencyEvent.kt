package com.step.wincash.event

import com.step.wincash.beans.CurrencyType

data class UpdateCurrencyEvent( val currencyType: CurrencyType, val sender: Any)

