package com.example.smartbmi

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // ===== FIREBASE AUTH INSTANCE =====
        auth = FirebaseAuth.getInstance()

        // ===== UI REFERENCES =====
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)

        // ===== REGISTER BUTTON CLICK =====
        btnRegister.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            // ===== VALIDATION =====
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password)
            }
        }
        // ===== LOGIN BUTTON CLICK =====
        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            // ===== VALIDATION =====
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }
    }
    // ===== REGISTER USER =====
    private fun registerUser(email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                goToBMIActivity()
            } else {
                Toast.makeText(this, task.exception?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // ===== USER LOGIN =====
    private fun loginUser(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                goToBMIActivity()
            } else {
                Toast.makeText(this, task.exception?.message?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // ===== NAVIGATE TO BMI SCREEN =====
    private fun goToBMIActivity() {
        val intent = Intent(this, BMIActivity::class.java)
        startActivity(intent)
        finish()
    }
}

