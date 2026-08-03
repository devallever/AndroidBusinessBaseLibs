package app.allever.android.lucky.choice.spin.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "options",
    foreignKeys = [ForeignKey(
        entity = Wheel::class,
        parentColumns = ["id"],
        childColumns = ["wheel_id"],
        onDelete = CASCADE,
        onUpdate = CASCADE
    )]
)
data class Option(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "wheel_id")
    val wheelId: Long
)
