package com.example.smartbmi

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.example.smartbmi.data.BMIDatabase
import com.example.smartbmi.data.BMIEntity
import com.example.smartbmi.databinding.ActivityBmiHistoryBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BMIHistoryActivity: AppCompatActivity() {

    // ===== VIEW BINDING =====
    private lateinit var binding: ActivityBmiHistoryBinding

    // ===== DATABASE INSTANCE =====
    private lateinit var bmiDatabase: BMIDatabase

    // ===== RECYCLERVIEW ADAPTER =====
    private lateinit var adapter: BMIHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== INFLATE THE LAYOUT USING VIEW BINDING =====
        binding = ActivityBmiHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ===== INITIALIZE BACK ARROW =====
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "BMI History"

        // ===== INITIALIZE ROOM DATABASE =====
        bmiDatabase = BMIDatabase.getDatabase(this)

        // ===== SETUP RECYCLERVIEW =====
        adapter = BMIHistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        loadBMIHistory()

        // ===== SETUP LOGOUT BUTTON =====
        binding.btnLogout.setOnClickListener {
            // ===== SIGN OUT USER =====
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }
    // ===== HANDLE BACK ARROW =====
    override fun onSupportNavigateUp(): Boolean {
        finish()     // goes back to BMIActivity
        return true
    }
    // ===== LOAD BMI HISTORY =====
    private fun loadBMIHistory() {

        lifecycleScope.launch {

            val bmiList = withContext(Dispatchers.IO) {
                bmiDatabase.bmiDao().getAllBMI()
            }

            // ===== SHOW EMPTY MESSAGE IF NO DATA EXISTS ======
            if (bmiList.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
                binding.tvHealthMessage.visibility = View.GONE
            } else {
                // ===== DATA EXISTS =====
                binding.tvEmpty.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE

                // ===== SEND DATA TO ADAPTER =====
                adapter.submitList(bmiList)

                // ===== SHOW HEALTH MESSAGE =====
                val latestBMI = bmiList.first()
                binding.tvHealthMessage.text = getHealthMessage(latestBMI.category)
                // ===== SETUP CHART =====
                setupChart(bmiList)
            }
        }
    }

    private fun getHealthMessage(category: String): String {

        return when (category) {
            "Underweight" -> "You are underweight. Try to eat nutritious meals and consult a professional i needed."
            "Normal" -> "You are in a healthy range. Keep eating well and staying active!"
            "Overweight" -> "You are slightly overweight. Consider regular exercise and balanced meals."
            "Obese" -> "Your BMI is high. It is advised to consult a healthcare professional."
            else -> "Keep tracking your BMI to stay healthy."
        }
    }

    // ===== SETUP BMI LINE CHART USING MP ANDROID CHART =====
    private fun setupChart(bmiList: List<BMIEntity>) {

        // ===== CONVERT BMI LIST TO CHART ENTRIES =====
        val entries = bmiList.reversed().mapIndexed { index, bmi ->
            Entry(index.toFloat(), bmi.bmi.toFloat())
        }
        val dataSet = LineDataSet(entries, "BMI History").apply {
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
        }
        // ===== SET CHART DATA =====
        binding.bmiChart.data = LineData(dataSet)
        binding.bmiChart.description.text = "BMI Progress"
        binding.bmiChart.invalidate()   // Refresh chart
    }
}