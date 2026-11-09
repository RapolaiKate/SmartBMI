package com.example.smartbmi

import android.os.Bundle
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartbmi.databinding.ActivityBmiBinding
import com.example.smartbmi.data.BMIDatabase
import com.example.smartbmi.data.BMIEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class BMIActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // View Binding
    private lateinit var binding: ActivityBmiBinding

    // Room Database
    private lateinit var bmiDatabase: BMIDatabase

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestoreDb: FirebaseFirestore

    // Text-to-Speech
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBmiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Room and Firebase
        bmiDatabase = BMIDatabase.getDatabase(this)
        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDb = FirebaseFirestore.getInstance()

        // Initialize Text-to-Speech
        tts = TextToSpeech(this, this)

        // Hide ProgressBar initially
        binding.progressBar.visibility = View.GONE

        // Calculate BMI button
        binding.btnCalculate.setOnClickListener {
            calculateAndSaveBMI()
        }

        // BMI History button
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, BMIHistoryActivity::class.java))
        }
    }

    // Calculate and save BMI
    private fun calculateAndSaveBMI() {

        // Get user input
        val weightText = binding.etWeight.text.toString().trim()
        val heightText = binding.etHeight.text.toString().trim()

        // Clear previous validation errors
        binding.tilWeight.error = null
        binding.tilHeight.error = null

        // Validate Weight
        if (weightText.isEmpty()) {
            binding.tilWeight.error = "Please enter your weight"
            return
        }

        val weight = weightText.toDoubleOrNull()

        if (weight == null || !weight.isFinite() || weight !in 1.0..500.0) {
            binding.tilWeight.error = "Enter a valid weight in kg"
            return
        }

        // Validate Height
        if (heightText.isEmpty()) {
            binding.tilHeight.error = "Please enter your height"
            return
        }

        val heightCm = heightText.toDoubleOrNull()

        if (heightCm == null || !heightCm.isFinite() || heightCm !in 50.0..300.0) {
            binding.tilHeight.error = "Enter a valid height in cm"
            return
        }

        // Convert centimetres to metres
        val heightMetres = heightCm / 100.0

        // Calculate BMI
        val bmi = weight / (heightMetres * heightMetres)

        if (!bmi.isFinite()) {
            Toast.makeText(
                this,
                "Unable to calculate BMI. Please check your measurements.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val bmiRounded = String.format(
            Locale.US,
            "%.1f",
            bmi
        ).toDouble()

        // Determine BMI category
        val category = when {
            bmiRounded < 18.5 -> "Underweight"
            bmiRounded < 25.0 -> "Normal"
            bmiRounded < 30.0 -> "Overweight"
            else -> "Obese"
        }

        // Update BMI result
        binding.tvBmiResult.text = "Your BMI: $bmiRounded"
        binding.tvCategory.text = category

        // Update semicircular BMI gauge
        binding.bmiGauge.setBmi(bmiRounded.toFloat())

        // Speak BMI result
        speakResult(bmiRounded, category)

        // Show saving progress
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCalculate.isEnabled = false

        val startTime = System.currentTimeMillis()

        // Create BMI record
        val bmiEntity = BMIEntity(
            bmi = bmiRounded,
            category = category,
            date = System.currentTimeMillis()
        )

        // Save BMI record
        lifecycleScope.launch {

            try {
                // Save to local Room database
                withContext(Dispatchers.IO) {
                    bmiDatabase.bmiDao().insertBMI(bmiEntity)
                }

                // Save to Firebase if user is signed in
                val userId = firebaseAuth.currentUser?.uid

                if (userId != null) {

                    val firebaseRecord = mapOf(
                        "bmi" to bmiRounded,
                        "category" to category,
                        "date" to bmiEntity.date
                    )

                    firestoreDb.collection("users")
                        .document(userId)
                        .collection("bmi_history")
                        .add(firebaseRecord)
                        .addOnFailureListener {
                            Toast.makeText(
                                this@BMIActivity,
                                "Saved on this device, but cloud sync failed.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }

                Toast.makeText(
                    this@BMIActivity,
                    "BMI saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {

                Toast.makeText(
                    this@BMIActivity,
                    "Unable to save BMI. Please try again.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                // Keep ProgressBar visible for at least 2 seconds
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = 2000L - elapsed

                if (remaining > 0) {
                    delay(remaining)
                }

                binding.progressBar.visibility = View.GONE
                binding.btnCalculate.isEnabled = true
            }
        }
    }

    // Text-to-Speech
    private fun speakResult(bmi: Double, category: String) {

        if (!isTtsReady) return

        val message = "Your BMI is $bmi. Your category is $category."

        tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "BMI_RESULT"
        )
    }

    // Text-to-Speech initialization
    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = tts?.setLanguage(Locale.US)

            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    // Clean up Text-to-Speech
    override fun onDestroy() {

        tts?.stop()
        tts?.shutdown()
        tts = null

        super.onDestroy()
    }
}
