package com.example.charge.constant

enum class HitMoleAnim(val fileName : String) {
    MOLE("mole.json"), //普通地鼠
    MOLE_HIT("mole_hit.json"), //地鼠被打
    BOMB("bomb.json"), //炸弹
    BOMB_HIT("bomb_hit.json"), //炸弹爆炸
    MOLE_HIT_Effect("mole_hit_effect.json"), //打中地鼠
    ReceiveCoin("fox_effect.json")  //接住金币
}