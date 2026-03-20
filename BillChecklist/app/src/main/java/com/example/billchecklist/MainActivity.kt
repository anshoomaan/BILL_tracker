package com.example.billchecklist

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billchecklist.data.AppDatabase
import com.example.billchecklist.data.Bill
import com.example.billchecklist.data.BillMonthlyRecord
import com.example.billchecklist.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: BillAdapter

    /** "yyyy-MM" string for the currently running month, e.g. "2024-03" */
    private val currentMonthYear: String
        get() = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        db = AppDatabase.getDatabase(this)

        setupRecyclerView()

        // Show "March 2024" in the action bar subtitle
        val displayMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        supportActionBar?.subtitle = displayMonth

        // On every launch: ensure this month has records; auto-resets happen
        // naturally because a new monthYear key is used for every new month.
        lifecycleScope.launch { initCurrentMonth() }

        observeCurrentMonthRecords()

        binding.fabAddBill.setOnClickListener { showAddBillDialog() }
    }

    // ── Month Initialisation ─────────────────────────────────────────────────

    /**
     * If no records exist for the current month yet, seed one row per active bill.
     * This is the "auto-clear on the 1st" mechanism — the old month's data
     * stays untouched while a fresh set of unpaid rows is created for today's month.
     */
    private suspend fun initCurrentMonth() {
        val count = db.billMonthlyRecordDao().getCountForMonth(currentMonthYear)
        if (count == 0) {
            val activeBills = db.billDao().getActiveBillsOnce()
            if (activeBills.isNotEmpty()) {
                val records = activeBills.map { bill ->
                    BillMonthlyRecord(
                        billId    = bill.id,
                        billName  = bill.name,
                        monthYear = currentMonthYear,
                        isPaid    = false
                    )
                }
                db.billMonthlyRecordDao().insertRecords(records)
            }
        }
    }

    // ── RecyclerView ─────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = BillAdapter(
            onCheckedChange = { record, isPaid ->
                lifecycleScope.launch {
                    db.billMonthlyRecordDao().updatePaidStatus(record.id, isPaid)
                }
            },
            onDeleteClick = { record -> confirmDeleteBill(record) }
        )
        binding.recyclerViewBills.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewBills.adapter = adapter
    }

    private fun observeCurrentMonthRecords() {
        lifecycleScope.launch {
            db.billMonthlyRecordDao()
                .getRecordsForMonth(currentMonthYear)
                .collectLatest { records ->
                    adapter.submitList(records)

                    val paid  = records.count { it.isPaid }
                    val total = records.size

                    binding.tvSummary.text =
                        if (total == 0) "No bills yet — tap ＋ to add one"
                        else            "Paid: $paid / $total  |  Remaining: ${total - paid}"

                    // Progress bar
                    binding.progressBills.max      = total.coerceAtLeast(1)
                    binding.progressBills.progress = paid
                }
        }
    }

    // ── Add Bill Dialog ───────────────────────────────────────────────────────

    private fun showAddBillDialog() {
        val editText = EditText(this).apply {
            hint = "e.g. Electricity, Rent, Netflix…"
            setPadding(48, 24, 48, 8)
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Bill")
            .setMessage("This bill will appear every month automatically.")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val name = editText.text.toString().trim()
                when {
                    name.isEmpty() -> Toast.makeText(this,
                        "Please enter a bill name", Toast.LENGTH_SHORT).show()
                    else           -> addBill(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addBill(name: String) {
        lifecycleScope.launch {
            // 1. Persist the bill definition
            val billId = db.billDao().insertBill(Bill(name = name)).toInt()

            // 2. Also add an unpaid row for the current month immediately
            db.billMonthlyRecordDao().insertRecord(
                BillMonthlyRecord(
                    billId    = billId,
                    billName  = name,
                    monthYear = currentMonthYear,
                    isPaid    = false
                )
            )
            Toast.makeText(this@MainActivity, "\"$name\" added!", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Delete Bill Dialog ────────────────────────────────────────────────────

    private fun confirmDeleteBill(record: BillMonthlyRecord) {
        AlertDialog.Builder(this)
            .setTitle("Remove \"${record.billName}\"?")
            .setMessage("This removes it from future months. History will still be saved.")
            .setPositiveButton("Remove") { _, _ ->
                lifecycleScope.launch {
                    // Soft-delete the bill (history stays intact)
                    db.billDao().softDeleteBill(record.billId)
                    // Also remove from the current (unfinished) month so it disappears
                    db.billMonthlyRecordDao()
                        .deleteRecordForMonth(currentMonthYear, record.billId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
