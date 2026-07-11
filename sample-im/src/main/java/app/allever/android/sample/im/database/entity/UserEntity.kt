package app.allever.android.sample.im.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val createTime: Long = System.currentTimeMillis(),
    // 新增：在线状态 0=离线 1=在线
    val online: Int = 0
)