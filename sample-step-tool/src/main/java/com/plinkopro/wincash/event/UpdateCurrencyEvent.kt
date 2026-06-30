package com.plinkopro.wincash.event

import com.plinkopro.wincash.beans.CurrencyType

data class UpdateCurrencyEvent( val currencyType: CurrencyType, val sender: Any)

