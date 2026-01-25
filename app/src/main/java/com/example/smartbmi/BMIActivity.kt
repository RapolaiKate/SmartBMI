package com.example.smartbmi

import android.os.Bundle
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartbmi.databinding.ActivityBmiBinding
import kotlinx.coroutines.launch
import android.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.example.smartbmi.data.BMIDatabase
import com.example.smartbmi.data.BMIEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay


class BMIActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // ===== VIEW BINDING =====
    private lateinit var binding: ActivityBmiBinding
    // ===== ROOM DATABASE =====
    private lateinit var bmiDatabase: BMIDatabase
    // ===== FIREBASE INSTANCE =====
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestoreDb: FirebaseFirestore
    // ===== TEXT-TO-SPEECH =====
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== VIEW BINDING SETUP =====
        binding = ActivityBmiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ===== INITIALIZE ROOM DATABASE & FIREBASE =====
        bmiDatabase = BMIDatabase.getDatabase(this)
        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDb = FirebaseFirestore.getInstance()

        // ===== INITIALIZE TEXT-TO-SPEECH =====
        tts = TextToSpeech(this, this)

        // =====  HIDE PROGRESSBAR INITIALLY ======
        binding.progressBar.visibility = View.GONE

        // ===== CALCULATE BUTTON =====
        binding.btnCalculate.setOnClickListener {
            calculateAndSaveBMI()
        }
        // ===== HISTORY BUTTON =====
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, BMIHistoryActivity::class.java))
        }
    }
    private fun calculateAndSaveBMI() {

        // ===== GET USER INPUT =====
        val weightText = binding.etWeight.text.toString().trim()
        val heightText = binding.etHeight.text.toString().trim()

        // ===== VALIDATION ======
        if (weightText.isEmpty() || heightText.isEmpty()) {
            Toast.makeText(this, "Please enter weight and height", Toast.LENGTH_SHORT).show()
            return
        }
        val weight = weightText.toDoubleOrNull()
        val height = heightText.toDoubleOrNull()

        if (weight == null || height == null) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
            return
        }
        // ===== CALCULATE BMI =====
        val bmi = weight / (height * height)
        val bmiRounded = String.format(Locale.US, "%.1f", bmi).toDouble()

        // ===== DETERMINE CATEGORY AND HEALTH =====
        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
        // ===== UPDATE UI =====
        binding.tvResult.text = "BMI: $bmiRounded"
        binding.tvCategory.text = category

        // ===== ANIMATE RULER INDICATOR =====
        animateIndicator(bmiRounded, category)

        // ===== SPEAK RESULT =====
        speakResult(bmiRounded, category)

        // ===== SHOW PROGRESSBAR =====
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCalculate.isEnabled = false
        val startTime = System.currentTimeMillis()

        // ===== SAVE TO ROOM =====
            val bmiEntity = BMIEntity(
                    bmi = bmiRounded,
                    category = category,
                    date = System.currentTimeMillis()
                )
        lifecycleScope.launch(Dispatchers.IO) {
            bmiDatabase.bmiDao().insertBMI(bmiEntity)

            // ===== SAVE TO FIREBASE =====
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                firestoreDb.collection("users")
                    .document(userId)
                    .collection("bmi_history")
                    .add(
                        mapOf(
                            "bmi" to bmiRounded,
                            "category" to category,
                            "date" to System.currentTimeMillis()
                        )
                    )
            }
            // ===== ENSURE PROGRESSBAR SHOWS AT LEAST 2 SECONDS =====
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = 2000 - elapsed
            if (remaining > 0)
                delay(remaining)

            withContext(Dispatchers.Main) {

                binding.progressBar.visibility = View.GONE
                binding.btnCalculate.isEnabled = true
                Toast.makeText(this@BMIActivity, "BMI saved successfully", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun animateIndicator(bmi: Double, category: String) {

        // ===== CONVERTING BMI TO FLOAT ONCE =====
        val bmiFloat = bmi.toFloat()

        val maxBMI = 40f
        val rulerWidth = binding.rulerImage.width.toFloat()
        val targetX = ((bmiFloat / maxBMI) * rulerWidth).coerceIn(0f, rulerWidth)

        // ===== SET INDICATOR COLOR BASED ON CATEGORY =====
        when (category) {
            "Underweight" -> binding.indicatorView.setColorFilter(Color.BLUE)
            "Normal" -> binding.indicatorView.setColorFilter(Color.GREEN)
            "Overweight" -> binding.indicatorView.setColorFilter(Color.YELLOW)
            else -> binding.indicatorView.setColorFilter(Color.RED)
        }
        binding.indicatorView.animate()
            .translationX(targetX)
            .setDuration(1000)
            .start()
    }
    // ===== SPEAK BMI & CATEGORY USING TEXT-TO-SPEECH =====
    private fun speakResult(bmi: Double, category: String) {
        val message = "Your BMI is $bmi. You are $category."
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH,null, null)
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }
    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}












































