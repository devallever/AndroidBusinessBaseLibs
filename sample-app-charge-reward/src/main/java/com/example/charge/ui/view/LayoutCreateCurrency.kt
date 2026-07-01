package com.example.charge.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.charge.R
import com.example.charge.currency.CurrencyType
import com.example.charge.databinding.LayoutCreateGoldBinding
import com.example.charge.init.InitManager
import com.example.charge.utils.CountryUtil

class LayoutCreateCurrency @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    LinearLayout(context, attributeSet, defStyle) {

    private val binding: LayoutCreateGoldBinding =
        LayoutCreateGoldBinding.inflate(LayoutInflater.from(context), this, true)

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    @SuppressLint("SetTextI18n")
    fun setValue(value: Float, currencyType: CurrencyType) {
        when (currencyType) {
            CurrencyType.GOLD -> {
                binding.tvValue.text = "+${value.toInt()}"
                binding.ivIcon.setImageResource(R.drawable.ic_gold)
            }
            CurrencyType.GREEN -> {
                binding.tvValue.text = "+$$value"
                binding.ivIcon.setImageResource(R.drawable.ic_green)
            }
        }
    }
}