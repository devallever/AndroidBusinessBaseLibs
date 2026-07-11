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
}