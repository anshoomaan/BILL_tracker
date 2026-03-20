package com.example.billchecklist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One record per bill per month. This table drives both the current checklist
 * and the full history. monthYear format: "2024-03" (ISO yyyy-MM).
 */
@Entity(tableName = "bill_monthly_records")
data class BillMonthlyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val billId: Int,
    val billName: String,
    val monthYear: String,   // e.g. "2024-03"
    val isPaid: Boolean = false
)
