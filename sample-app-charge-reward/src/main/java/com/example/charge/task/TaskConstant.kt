package com.example.charge.task

class TaskId {
    companion object {
        const val CHARGE_COLLECT_20 = "${TaskType.CHARGE}1"
        const val CHARGE_COLLECT_100 = "${TaskType.CHARGE}2"
        const val CHARGE_COLLECT_200 = "${TaskType.CHARGE}3"
        const val CHARGE_COLLECT_400 = "${TaskType.CHARGE}4"
        const val CHARGE_SIGN_1 = "${TaskType.CHARGE}5"
        const val CHARGE_SIGN_2 = "${TaskType.CHARGE}6"
        const val CHARGE_SIGN_3 = "${TaskType.CHARGE}7"
        const val CHARGE_SIGN_4 = "${TaskType.CHARGE}8"
        const val CHARGE_SIGN_5 = "${TaskType.CHARGE}9"
        const val CHARGE_SIGN_6 = "${TaskType.CHARGE}10"
        const val CHARGE_SIGN_7 = "${TaskType.CHARGE}11"
        const val CHARGE_SIGN_8 = "${TaskType.CHARGE}12"
        const val CHARGE_SIGN_9 = "${TaskType.CHARGE}13"
        const val CHARGE_SIGN_10 = "${TaskType.CHARGE}14"

        // 打地鼠任务ID
        const val HIT_MOLE_50 = "${TaskType.HIT_MOLE}1"
        const val HIT_MOLE_300 = "${TaskType.HIT_MOLE}2"
        const val HIT_MOLE_1000 = "${TaskType.HIT_MOLE}3"
        const val HIT_MOLE_3000 = "${TaskType.HIT_MOLE}4"
        const val HIT_MOLE_10000 = "${TaskType.HIT_MOLE}5"
        const val HIT_MOLE_GAME_5 = "${TaskType.HIT_MOLE}6"
        const val HIT_MOLE_GAME_15 = "${TaskType.HIT_MOLE}7"
        const val HIT_MOLE_GAME_30 = "${TaskType.HIT_MOLE}8"
        const val HIT_MOLE_GAME_50 = "${TaskType.HIT_MOLE}9"
        const val HIT_MOLE_GAME_80 = "${TaskType.HIT_MOLE}10"

        // 接金币任务ID
        const val RECEIVE_COIN_200 = "${TaskType.RECEIVE_COIN}1"
        const val RECEIVE_COIN_1000 = "${TaskType.RECEIVE_COIN}2"
        const val RECEIVE_COIN_2500 = "${TaskType.RECEIVE_COIN}3"
        const val RECEIVE_COIN_5000 = "${TaskType.RECEIVE_COIN}4"
        const val RECEIVE_COIN_10000 = "${TaskType.RECEIVE_COIN}5"
        const val RECEIVE_COIN_GAME_5 = "${TaskType.RECEIVE_COIN}6"
        const val RECEIVE_COIN_GAME_15 = "${TaskType.RECEIVE_COIN}7"
        const val RECEIVE_COIN_GAME_30 = "${TaskType.RECEIVE_COIN}8"
        const val RECEIVE_COIN_GAME_50 = "${TaskType.RECEIVE_COIN}9"
        const val RECEIVE_COIN_GAME_80 = "${TaskType.RECEIVE_COIN}10"
    }
}

class TaskType {
    companion object {
        const val CHARGE = "1"
        const val HIT_MOLE = "2"
        const val RECEIVE_COIN = "3"
    }
}

class TaskCategory {
    companion object {
        const val CHARGE_COLLECT = "1"
        const val CHARGE_SIGN = "2"
        const val HIT_MOLE = "3"
        const val HIT_MOLE_GAME_COUNT = "4"
        const val RECEIVE_COIN = "5"
        const val RECEIVE_COIN_GAME_COUNT = "6"
    }
}



