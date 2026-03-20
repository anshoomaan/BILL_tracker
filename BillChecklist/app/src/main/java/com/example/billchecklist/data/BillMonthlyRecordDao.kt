package com.example.billchecklist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillMonthlyRecordDao {

    /** Live list of records for a given month (drives the checklist UI) */
    @Query("SELECT * FROM bill_monthly_records WHERE monthYear = :monthYear ORDER BY billName ASC")
    fun getRecordsForMonth(monthYear: String): Flow<List<BillMonthlyRecord>>

    /** One-shot fetch used during month initialisation */
    @Query("SELECT * FROM bill_monthly_records WHERE monthYear = :monthYear ORDER BY billName ASC")
    suspend fun getRecordsForMonthOnce(monthYear: String): List<BillMonthlyRecord>

    /** All distinct past months (excluding current), newest first */
    @Query("""
        SELECT DISTINCT monthYear FROM bill_monthly_records
        WHERE monthYear != :currentMonth
        ORDER BY monthYear DESC
    """)
    fun getPastMonths(currentMonth: String): Flow<List<String>>

    /** How many records already exist for a month (used to detect new month) */
    @Query("SELECT COUNT(*) FROM bill_monthly_records WHERE monthYear = :monthYear")
    suspend fun getCountForMonth(monthYear: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BillMonthlyRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<BillMonthlyRecord>)

    @Query("UPDATE bill_monthly_records SET isPaid = :isPaid WHERE id = :recordId")
    suspend fun updatePaidStatus(recordId: Int, isPaid: Boolean)

    @Query("DELETE FROM bill_monthly_records WHERE monthYear = :monthYear AND billId = :billId")
    suspend fun deleteRecordForMonth(monthYear: String, billId: Int)
}
