package com.example.billchecklist

import com.example.billchecklist.data.BillMonthlyRecord

/** Sealed type used by HistoryAdapter to handle month headers and bill rows */
sealed class HistoryItem {

    data class MonthHeader(
        val monthDisplay: String,   // e.g. "March 2024"
        val monthYear: String,      // raw "2024-03" for keying
        val paidCount: Int,
        val totalCount: Int
    ) : HistoryItem()

    data class BillRecord(
        val record: BillMonthlyRecord
    ) : HistoryItem()
}
