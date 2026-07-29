package app.allever.android.learning.project.compose.module.wechat.data

import androidx.annotation.DrawableRes
import z.compose.app.allever.android.sample.compose.project.R

data class User(val id: String, val nickname: String, @DrawableRes val avatar: Int) {
    companion object {
        val Me = User("alleve", "Allever", R.drawable.zcp_avatar_me)
    }
}