package app.allever.android.lucky.choice.spin.finger.utilities

import app.allever.android.lucky.choice.spin.R


enum class Mode {
    SINGLE, GROUP, ORDER;

    fun next(): Mode = when (this) {
        SINGLE -> GROUP
        GROUP -> ORDER
        ORDER -> SINGLE
    }

    fun initialCount(): Int = when (this) {
        SINGLE, ORDER -> 1
        GROUP -> 2
    }

    fun nextCount(count: Int): Int = when (this) {
        SINGLE -> count % 5 + 1
        GROUP -> (count - 1) % 4 + 2
        ORDER -> 1
    }

    fun drawable(): Int = when (this) {
        SINGLE -> R.drawable.ls_single_icon
        GROUP -> R.drawable.ls_group_icon
        ORDER -> R.drawable.ls_order_icon
    }
}