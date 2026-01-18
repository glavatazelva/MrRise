package com.example.android.tvz.hr.mrrise.ui.alarms

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.tvz.hr.mrrise.R
import com.example.android.tvz.hr.mrrise.data.database.AlarmEntity
import com.example.android.tvz.hr.mrrise.databinding.ItemAlarmBinding
import java.util.*

class AlarmAdapter(
    private val onToggle: (AlarmEntity) -> Unit,
    private val onEdit: (AlarmEntity) -> Unit

) : ListAdapter<AlarmEntity, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlarmViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: AlarmEntity) {
            binding.tvTime.text = String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)

            binding.tvLabel.text = alarm.label

            binding.tvDays.text = formatDays(alarm)
            val context = binding.root.context

            binding.tvPuzzleType.text = when (alarm.puzzleType) {
                "SIMON_SAYS" -> context.getString(R.string.puzzle_simon)
                "MATH" -> context.getString(R.string.puzzle_math)
                "QR_CODE" -> context.getString(R.string.puzzle_qr)
                else -> "❓"
            }

            binding.switchEnabled.isChecked = alarm.isEnabled

            binding.switchEnabled.setOnCheckedChangeListener { _, _ ->
                onToggle(alarm)
            }

            binding.root.setOnClickListener {
                onEdit(alarm)
            }
        }

        private fun formatDays(alarm: AlarmEntity): String {
            val context = binding.root.context

            val days = mutableListOf<String>()
            if (alarm.monday) days.add(context.getString(R.string.monday))
            if (alarm.tuesday) days.add(context.getString(R.string.tuesday))
            if (alarm.wednesday) days.add(context.getString(R.string.wednesday))
            if (alarm.thursday) days.add(context.getString(R.string.thursday))
            if (alarm.friday) days.add(context.getString(R.string.friday))
            if (alarm.saturday) days.add(context.getString(R.string.saturday))
            if (alarm.sunday) days.add(context.getString(R.string.sunday))

            return when {
                days.size == 7 -> context.getString(R.string.every_day)
                days.isEmpty() -> context.getString(R.string.one_time)
                else -> days.joinToString(", ")
            }
        }
    }

    class AlarmDiffCallback : DiffUtil.ItemCallback<AlarmEntity>() {
        override fun areItemsTheSame(oldItem: AlarmEntity, newItem: AlarmEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AlarmEntity, newItem: AlarmEntity): Boolean {
            return oldItem == newItem
        }
    }
}