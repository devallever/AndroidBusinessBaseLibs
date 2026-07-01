package com.example.charge.utils

object SpKey {

    const val IS_FIRST_LAUNCH_APP = "is_first_launch_app"

    const val GAME_SETTING = "game_setting"

    const val WITHDRAW_SETTING = "withdraw_setting"

    const val CURRENCY_GOLD_NUM = "CURRENCY_GOLD_NUM" //金币数量
    const val CURRENCY_GREEN_NUM = "CURRENCY_GREEN_NUM" //绿钞数量

    const val HIT_MOLE_COUNT = "HIT_MOLE_COUNT" //打中地鼠的次数
    const val HIT_MOLE_GAME_COUNT = "HIT_MOLE_GAME_COUNT" //打地鼠的游戏次数
    const val HIT_MOLE_GAME_ADD_COUNT_TIME = "HIT_MOLE_GAME_ADD_COUNT_TIME" //增加游戏次数的间隔时间
    const val HIT_MOLE_GAME_COUNT_INFINITE_TIME = "HIT_MOLE_GAME_COUNT_INFINITE_TIME" //打地鼠无限模式剩余时间

    const val RECEIVE_COIN_COUNT = "RECEIVE_COIN_COUNT" //接到金币的次数
    const val RECEIVE_COIN_GAME_COUNT = "RECEIVE_COIN_GAME_COUNT" //接金币的游戏次数
    const val RECEIVE_COIN_GAME_ADD_COUNT_TIME = "RECEIVE_COIN_GAME_ADD_COUNT_TIME" //增加游戏次数的间隔时间
    const val RECEIVE_COIN_GAME_COUNT_INFINITE_TIME = "RECEIVE_COIN_GAME_COUNT_INFINITE_TIME" //接金币无限模式剩余时间

    const val FIRST_SHOW_FLOAT_ICON = "FIRST_SHOW_FLOAT_ICON" //第一次显示悬浮图标
    const val FIRST_CLICK_SPEED_UP = "FIRST_CLICK_SPEED_UP" //第一次点击加速
    const val INPUT_PAYMENT_NAME = "INPUT_PAYMENT_NAME" //输入的收款人姓名
    const val INPUT_PAYMENT_PHONE = "INPUT_PAYMENT_PHONE" //输入的收款人手机号
    const val INPUT_PAYMENT_EMAIL = "INPUT_PAYMENT_EMAIL" //输入的收款人邮箱
    const val INPUT_PAYMENT_ID = "INPUT_PAYMENT_ID" //输入的收款人ID
    const val INPUT_PAYMENT_CPF = "INPUT_PAYMENT_CPF" //输入的收款人CPF

    const val WAITING_PLAYER_COUNT_1 = "WAITING_PLAYER_COUNT_1" //1档等待人数
    const val WAITING_PLAYER_COUNT_2 = "WAITING_PLAYER_COUNT_2" //2档等待人数
    const val WAITING_PLAYER_COUNT_3 = "WAITING_PLAYER_COUNT_3" //3档等待人数

    // 充电任务收集泡泡次数
    const val TASK_CHARGE_COLLECT_COUNT = "TASK_CHARGE_COLLECT_COUNT"
    // 充电任务签到次数
    const val TASK_CHARGE_SIGN_COUNT = "TASK_CHARGE_SIGN_1_COUNT"
    // 签到日期保存的key
    const val TASK_CHARGE_SIGN_DATE = "TASK_CHARGE_SIGN_DATE"

    //打地鼠打中次数
//    const val TASK_HIT_MOLE_COUNT = "TASK_HIT_MOLE_COUNT"
    // 打地鼠游戏次数
    const val TASK_HIT_MOLE_GAME_COUNT = "TASK_HIT_MOLE_GAME_COUNT"
    //接金币接中次数
//    const val TASK_RECEIVE_COIN_COUNT = "TASK_RECEIVE_COIN_COUNT"
    // 接金币游戏次数
    const val TASK_RECEIVE_COIN_GAME_COUNT = "TASK_RECEIVE_COIN_GAME_COUNT"
    
    // 充电任务完成收集泡泡状态
    const val TASK_CHARGE_COLLECT_FINISH = "TASK_CHARGE_COLLECT_FINISH_" //后接id
    // 充电任务签到状态
    const val TASK_CHARGE_SIGN_FINISH = "TASK_CHARGE_SIGN_FINISH_" //后接id

    // 打地鼠任务打地鼠完成状态常量
    const val TASK_HIT_MOLE_FINISH = "TASK_HIT_MOLE_FINISH_"  //后接id
    // 打地鼠任务游戏次数完成状态常量
    const val TASK_HIT_MOLE_GAME_FINISH = "TASK_HIT_MOLE_GAME_FINISH_" //后接id
    
    // 接金币任务接金币次数完成状态常量
    const val TASK_RECEIVE_COIN_FINISH = "TASK_RECEIVE_COIN_FINISH_" //后接id
    // 接金币任务游戏次数完成状态常量
    const val TASK_RECEIVE_COIN_GAME_FINISH = "TASK_RECEIVE_COIN_GAME_FINISH_" //后接id

    const val APP_QUIT_TIME = "APP_QUIT_TIME"  //APP退出时间

    const val NATIVE_CLICK_NUM = "native_click_time" //native点击次数

    const val CLAIM_NEW_USER_BENEFITS = "claim_new_user_benefits"

    const val IS_SOUND_OPEN = "is_sound_open" //声音开关
    const val IS_MUSIC_OPEN = "is_music_open" //音乐开关

    const val IS_FIRST_DISPLAY_MAIN = "is_first_display_main"

    const val ARRIVE_LEVEL2_RANDOM = "arrive_level2_random"
    const val ARRIVE_LEVEL3_RANDOM = "arrive_level3_random"

}