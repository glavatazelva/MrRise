package com.example.android.tvz.hr.mrrise

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.tvz.hr.mrrise.databinding.ActivityMainBinding
import com.example.android.tvz.hr.mrrise.ui.alarms.AlarmAdapter
import com.example.android.tvz.hr.mrrise.ui.alarms.AlarmViewModel
import com.example.android.tvz.hr.mrrise.utils.SwipeToDeleteCallback
import com.example.android.tvz.hr.mrrise.utils.AlarmForegroundService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: AlarmViewModel
    private lateinit var adapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AlarmForegroundService.start(this)


        viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        setupRecyclerView()

        observeAlarms()

        binding.fabAddAlarm.setOnClickListener {
            val intent = android.content.Intent(this, AddAlarmActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = AlarmAdapter(
            onToggle = { alarm ->
                viewModel.toggleAlarm(alarm)
            },
            onEdit = { alarm ->
                val intent = android.content.Intent(this, AddAlarmActivity::class.java)
                intent.putExtra("ALARM_ID", alarm.id)
                startActivity(intent)            }
        )

        binding.rvAlarms.layoutManager = LinearLayoutManager(this)
        binding.rvAlarms.adapter = adapter

        val swipeHandler = SwipeToDeleteCallback { position ->
            val alarm = adapter.currentList[position]
            viewModel.deleteAlarm(alarm)
            Toast.makeText(this, getString(R.string.alarm_deleted), Toast.LENGTH_SHORT).show()
        }

        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.rvAlarms)
    }

    private fun observeAlarms() {
        viewModel.allAlarms.observe(this) { alarms ->
            adapter.submitList(alarms)

            if (alarms.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvAlarms.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvAlarms.visibility = View.VISIBLE
            }
        }
    }


}