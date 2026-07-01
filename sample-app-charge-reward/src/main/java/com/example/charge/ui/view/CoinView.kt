package com.example.charge.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.charge.constant.Coin
import com.example.charge.databinding.ViewCoinBinding
import com.example.charge.init.InitManager
import com.example.charge.utils.CountryUtil
import com.example.charge.utils.gone
import com.example.charge.utils.setVisible
import com.example.charge.utils.visible

class CoinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewCoinBinding.inflate( LayoutInflater.from(context), this, true)

    @SuppressLint("SetTextI18n")
    fun init(coin: Coin) {
        binding.apply {
            if (coin.num >= 10f) {
                goldFl.visible()
                greenFl.gone()
                goldNumTv.text = " ${coin.num.toInt()} "
            } else {
                goldFl.gone()
                greenFl.visible()
                greenNumTv.text =  "$${coin.num}"
            }
            adImg.setVisible(coin.needSeeAd)
        }
    }

}