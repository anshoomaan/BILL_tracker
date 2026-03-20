package com.example.billchecklist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Query("SELECT * FROM bills WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActiveBillsOnce(): List<Bill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Update
    suspend fun updateBill(bill: Bill)

    /** Soft-delete: keeps bill records in history intact */
    @Query("UPDATE bills SET isActive = 0 WHERE id = :billId")
    suspend fun softDeleteBill(billId: Int)

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Int): Bill?
}
