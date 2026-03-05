package com.accountability.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "secrets")
data class SecretKeyEntity(
    @PrimaryKey val id: String,
    val label: String,
    val secret: String,
    val duration: Int
)

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val limitMinutes: Int,
    val usedMinutes: Int,
    val secretIds: List<String>,
    val unlockUntil: Long = 0
)

class Converters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(",")
    }
}
