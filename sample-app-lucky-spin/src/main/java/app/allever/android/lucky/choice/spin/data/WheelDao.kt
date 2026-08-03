package app.allever.android.lucky.choice.spin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WheelDao {
    @Insert
    suspend fun insertWheel(wheel: Wheel): Long

    @Update fun updateWheel(wheel: Wheel)

    @Delete fun deleteWheel(wheel: Wheel)

    @Transaction
    @Query("SELECT * FROM wheels ORDER BY created_at DESC")
    fun getWheelsAndOptions(): Flow<List<WheelWithOptions>>

    @Query("DELETE FROM wheels WHERE id = :wheelId")
    suspend fun deleteWheelById(wheelId: Long)

    @Query("UPDATE wheels SET name = :name WHERE id = :wheelId")
    suspend fun updateWheelNameById(wheelId: Long, name: String)
}