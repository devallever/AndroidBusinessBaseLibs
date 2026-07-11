package app.allever.android.sample.im.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import app.allever.android.sample.im.database.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM user WHERE username = :username LIMIT 1")
    fun getUserByUsername(username: String): UserEntity?

    // 更新单个用户在线状态
    @Query("UPDATE user SET online = :status WHERE username = :username")
    fun updateOnlineStatus(username: String, status: Int)

    // 重置所有用户为离线状态（服务重启时调用）
    @Query("UPDATE user SET online = 0")
    fun resetAllOnlineStatus()

    // 查询所有在线用户
    @Query("SELECT * FROM user WHERE online = 1")
    fun getOnlineUsers(): List<UserEntity>
}