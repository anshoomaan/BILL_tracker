package com.example.billchecklist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.billchecklist.data.BillMonthlyRecord

class BillAdapter(
    private val onCheckedChange: (BillMonthlyRecord, Boolean) -> Unit,
    private val onDeleteClick: (BillMonthlyRecord) -> Unit
) : ListAdapter<BillMonthlyRecord, BillAdapter.BillViewHolder>(DiffCallback()) {

    inner class BillViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBillName: TextView      = view.findViewById(R.id.tvBillName)
        val cbPaid: CheckBox          = view.findViewById(R.id.cbPaid)
        val tvStatus: TextView        = view.findViewById(R.id.tvStatus)
        val btnDelete: ImageButton    = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val record = getItem(position)

        holder.tvBillName.text = record.billName

        // Detach listener before programmatic update to avoid false triggers
        holder.cbPaid.setOnCheckedChangeListener(null)
        holder.cbPaid.isChecked = record.isPaid

        if (record.isPaid) {
            holder.tvBillName.paintFlags =
                holder.tvBillName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvStatus.text = "✓ Paid"
            holder.tvStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.paid_green)
            )
            holder.itemView.alpha = 0.75f
        } else {
            holder.tvBillName.paintFlags =
                holder.tvBillName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvStatus.text = "Pending"
            holder.tvStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.pending_red)
            )
            holder.itemView.alpha = 1.0f
        }

        holder.cbPaid.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChange(record, isChecked)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(record)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BillMonthlyRecord>() {
        override fun areItemsTheSame(old: BillMonthlyRecord, new: BillMonthlyRecord) =
            old.id == new.id

        override fun areContentsTheSame(old: BillMonthlyRecord, new: BillMonthlyRecord) =
            old == new
    }
}
