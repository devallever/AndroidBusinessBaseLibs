package app.allever.android.sample.im.http.response

data class UserInfoData(
    val userId: Long,
    val username: String,
    val online: Int,
    val createTime: Long
)