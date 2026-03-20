package com.example.billchecklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter : ListAdapter<HistoryItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_BILL   = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is HistoryItem.MonthHeader -> TYPE_HEADER
            is HistoryItem.BillRecord  -> TYPE_BILL
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> MonthHeaderVH(
                inflater.inflate(R.layout.item_history_month, parent, false)
            )
            else -> BillRecordVH(
                inflater.inflate(R.layout.item_history_bill, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HistoryItem.MonthHeader -> (holder as MonthHeaderVH).bind(item)
            is HistoryItem.BillRecord  -> (holder as BillRecordVH).bind(item)
        }
    }

    // ── View Holders ──────────────────────────────────────────────────────────

    class MonthHeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMonth:   TextView = view.findViewById(R.id.tvMonth)
        private val tvSummary: TextView = view.findViewById(R.id.tvMonthlySummary)

        fun bind(item: HistoryItem.MonthHeader) {
            tvMonth.text   = item.monthDisplay
            tvSummary.text = "Paid ${item.paidCount} / ${item.totalCount}"
        }
    }

    class BillRecordVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName:   TextView = view.findViewById(R.id.tvHistoryBillName)
        private val tvStatus: TextView = view.findViewById(R.id.tvHistoryStatus)

        fun bind(item: HistoryItem.BillRecord) {
            tvName.text = item.record.billName
            if (item.record.isPaid) {
                tvStatus.text = "✓ Paid"
                tvStatus.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.paid_green)
                )
            } else {
                tvStatus.text = "✗ Not Paid"
                tvStatus.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.pending_red)
                )
            }
        }
    }

    // ── Diff ──────────────────────────────────────────────────────────────────

    class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(old: HistoryItem, new: HistoryItem): Boolean =
            when {
                old is HistoryItem.MonthHeader && new is HistoryItem.MonthHeader ->
                    old.monthYear == new.monthYear
                old is HistoryItem.BillRecord && new is HistoryItem.BillRecord ->
                    old.record.id == new.record.id
                else -> false
            }

        override fun areContentsTheSame(old: HistoryItem, new: HistoryItem) = old == new
    }
}
