package com.step.wincash.ui.widget.scratchcards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.step.wincash.base.BaseActivity
import com.step.wincash.databinding.DemoActivityScatchCardBinding

class DemoScratchCardsActivity : BaseActivity<DemoActivityScatchCardBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {


        // 设置返回按钮点击事件
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        
        // 设置重置按钮
        binding.btnReset.setOnClickListener {
            resetScratchCards()
        }
    }

    private fun initData() {
        // 配置ScratchCardView
        binding.scratchCardView.setRevealThreshold(0.5f) // 超过50%时自动显示
        
        // 设置随机数字
        binding.scratchCardView.setRandomNumbers()
        
        // 设置单个格子揭示监听器
        binding. scratchCardView.setOnCellRevealListener { position ->
//            showToast("格子 $position 已揭示！")
        }
        
        // 设置全部揭示监听器
        binding.scratchCardView.setOnAllRevealedListener {
            showToast("恭喜，所有格子都已揭示！")
        }
    }

    private fun resetScratchCards() {
        // 重置所有刮刮卡
        binding.scratchCardView.resetAll()
        
        // 重新设置随机数字
        binding.scratchCardView.setRandomNumbers()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DemoActivityScatchCardBinding {
        return DemoActivityScatchCardBinding.inflate(layoutInflater)
    }
}