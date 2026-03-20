package com.example.billchecklist

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billchecklist.data.AppDatabase
import com.example.billchecklist.databinding.ActivityHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: HistoryAdapter

    private val currentMonthYear: String
        get() = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Payment History"

        db = AppDatabase.getDatabase(this)

        adapter = HistoryAdapter()
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistory.adapter = adapter

        observeHistory()
    }

    private fun observeHistory() {
        lifecycleScope.launch {
            db.billMonthlyRecordDao()
                .getPastMonths(currentMonthYear)
                .collectLatest { months ->
                    if (months.isEmpty()) {
                        binding.tvEmpty.visibility           = View.VISIBLE
                        binding.recyclerViewHistory.visibility = View.GONE
                        return@collectLatest
                    }

                    // Build a flat list: MonthHeader → BillRecord → BillRecord → …
                    val items = mutableListOf<HistoryItem>()
                    val sdfKey     = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    val sdfDisplay = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

                    for (month in months) {
                        val records = db.billMonthlyRecordDao().getRecordsForMonthOnce(month)
                        val paid    = records.count { it.isPaid }
                        val display = try {
                            sdfDisplay.format(sdfKey.parse(month)!!)
                        } catch (e: Exception) { month }

                        items += HistoryItem.MonthHeader(
                            monthDisplay = display,
                            monthYear    = month,
                            paidCount    = paid,
                            totalCount   = records.size
                        )
                        records.forEach { items += HistoryItem.BillRecord(it) }
                    }

                    binding.tvEmpty.visibility           = View.GONE
                    binding.recyclerViewHistory.visibility = View.VISIBLE
                    adapter.submitList(items)
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
