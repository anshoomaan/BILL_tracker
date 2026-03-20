package com.example.billchecklist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a recurring bill the user wants to track each month.
 * Bills are soft-deleted (isActive = false) so history is preserved.
 */
@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val isActive: Boolean = true
)
