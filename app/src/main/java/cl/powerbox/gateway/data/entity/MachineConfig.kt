package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "machine_config")
data class MachineConfig(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long
)
