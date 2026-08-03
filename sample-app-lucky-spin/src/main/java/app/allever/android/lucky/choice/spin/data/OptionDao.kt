package app.allever.android.lucky.choice.spin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OptionDao {
    @Insert
    suspend fun insertOptions(options: List<Option>)

    @Query("DELETE FROM options WHERE wheel_id = :wheelId")
    suspend fun deleteOptionsByWheelId(wheelId: Long)
}