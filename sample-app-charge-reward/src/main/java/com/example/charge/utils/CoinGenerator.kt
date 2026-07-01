package com.example.charge.utils

import com.example.charge.constant.Coin

object CoinGenerator {
        // 概率表（价值 -> 概率）
        private val coins = listOf(
            10f  to 0.3333f,
            20f  to 0.20f,
            50f  to 0.1333f,
            100f to 0.10f,
            200f to 0.0667f,
            0.1f to 0.0667f,
            0.2f to 0.0667f,
            0.3f to 0.0333f
        )
        private val cumulative by lazy {
            val list = ArrayList<Pair<Float, Float>>(coins.size)
            var s = 0f
            for ((num, p) in coins) {
                s += p
                list += num to s
            }
            list
        }
        fun randomCoin(): Coin {
            val r = Math.random().toFloat()
            val num = cumulative.firstOrNull { r <= it.second }?.first ?: coins.last().first
            return Coin(num = num, needSeeAd = false) // 是否看广告由游戏调度层决定
        }
    }