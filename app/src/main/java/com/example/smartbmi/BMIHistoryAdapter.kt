package com.example.smartbmi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.smartbmi.data.BMIEntity
import com.example.smartbmi.databinding.ItemBmiHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class BMIHistoryAdapter: ListAdapter<BMIEntity, BMIHistoryAdapter.BMIViewHolder>(DiffCallback) {

    class BMIViewHolder(private val binding: ItemBmiHistoryBinding): RecyclerView.ViewHolder(binding.root) {

        // ===== VIEW HOLDER USING VIEW BINDING =====
        fun bind(item: BMIEntity) {
            binding.tvBmiValue.text = "BMI: ${String.format(Locale.US, "%.2f", item.bmi)}"
            binding.tvCategory.text = item.category
            val dateFormat = SimpleDateFormat("dd MM yyyy", Locale.US)
            binding.tvDate.text = dateFormat.format(Date(item.date))

            // ====== CATEGORY COLOR BASED ON HEALTH STATUS =====
            val color = when (item.category) {
                "Underweight" -> android.R.color.holo_blue_dark
                "Normal" -> android.R.color.holo_green_dark
                "Overweight" -> android.R.color.holo_orange_dark
                else -> android.R.color.holo_red_dark
            }
            binding.tvCategory.setTextColor(
                ContextCompat.getColor(binding.root.context, color)
            )
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BMIViewHolder {

        // ===== INFLATE ITEM LAYOUT USING VIEW BINDING =====
        val binding = ItemBmiHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BMIViewHolder(binding)
    }
    override fun onBindViewHolder(holder: BMIViewHolder, position: Int) {

        holder.bind(getItem(position))
    }
    companion object {

        // ===== CHECKS WHETHER ITEMS OR CONTENTS HAVE CHANGED =====
        private val DiffCallback = object : DiffUtil.ItemCallback<BMIEntity>() {

            // ===== CHECK IF TWO ITEMS ARE THE SAME (by unique ID) =====
            override fun areItemsTheSame(oldItem: BMIEntity, newItem: BMIEntity): Boolean {
                return oldItem.id == newItem.id
            }
            // ===== CHECK IF TWO ITEMS ARE THE SAME =====
            override fun areContentsTheSame(oldItem: BMIEntity, newItem: BMIEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}