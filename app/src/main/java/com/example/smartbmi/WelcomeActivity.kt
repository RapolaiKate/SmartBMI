package com.example.smartbmi

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()

        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)

        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)

        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val tvForgotPassword =
            findViewById<TextView>(R.id.tvForgotPassword)

        // Configure password visibility.
        setupPasswordVisibility()

        // Clear validation messages when the user starts typing.
        editEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                tilEmail.error = null
            }
        }

        editPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                tilPassword.error = null
            }
        }

        // LOGIN BUTTON
        btnLogin.setOnClickListener {

            tilEmail.error = null
            tilPassword.error = null

            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString()

            if (email.isEmpty()) {
                tilEmail.error = "Please enter your email address"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Please enter a valid email address"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                tilPassword.error = "Please enter your password"
                return@setOnClickListener
            }

            if (!hasInternetConnection()) {
                showInternetMessage()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        // SIGN-UP LINK
        tvSignUp.setOnClickListener {
            openSignUpScreen()
        }

        // FORGOT PASSWORD
        tvForgotPassword.setOnClickListener {

            tilEmail.error = null

            val email = editEmail.text.toString().trim()

            if (email.isEmpty()) {
                tilEmail.error = "Enter your email address first"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Please enter a valid email address"
                return@setOnClickListener
            }

            if (!hasInternetConnection()) {
                showInternetMessage()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "If an eligible account exists, password reset instructions will be sent.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        showFirebaseError(
                            task.exception?.localizedMessage
                                ?: "Unable to process the password reset. Please try again."
                        )
                    }
                }
        }
    }

    // PASSWORD VISIBILITY
    private fun setupPasswordVisibility() {

        editPassword.transformationMethod =
            PasswordTransformationMethod.getInstance()

        tilPassword.setEndIconDrawable(R.drawable.ic_eye_off)

        var isPasswordVisible = false

        tilPassword.setEndIconOnClickListener {

            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {

                editPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()

                tilPassword.setEndIconDrawable(R.drawable.ic_eye)

            } else {

                editPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()

                tilPassword.setEndIconDrawable(R.drawable.ic_eye_off)
            }

            editPassword.setSelection(
                editPassword.text?.length ?: 0
            )
        }
    }

    // CHECK INTERNET CONNECTION
    private fun hasInternetConnection(): Boolean {

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

        val network = connectivityManager.activeNetwork
            ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(network)
                ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) && capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        )
    }

    // INTERNET MESSAGE
    private fun showInternetMessage() {

        Toast.makeText(
            this,
            "No internet connection. Please connect to the internet and try again.",
            Toast.LENGTH_LONG
        ).show()
    }

    // LOGIN WITH FIREBASE
    private fun loginUser(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful && auth.currentUser != null) {

                    Toast.makeText(
                        this,
                        "Login successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    goToBMIActivity()

                } else {

                    val exception = task.exception

                    if (!hasInternetConnection()) {

                        showInternetMessage()

                    } else if (
                        exception is FirebaseAuthInvalidUserException &&
                        exception.errorCode == "ERROR_USER_NOT_FOUND"
                    ) {

                        Toast.makeText(
                            this,
                            "No account found. Please sign up first.",
                            Toast.LENGTH_LONG
                        ).show()

                        openSignUpScreen(email)

                    } else {

                        Toast.makeText(
                            this,
                            "Login failed. Please check your email and password, or sign up.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    // DISPLAY FIREBASE ERROR
    private fun showFirebaseError(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    // OPEN SIGN-UP SCREEN
    private fun openSignUpScreen(email: String = "") {

        val intent = Intent(this, SignUpActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
    }

    // OPEN BMI SCREEN AFTER SUCCESSFUL AUTHENTICATION
    private fun goToBMIActivity() {

        val intent = Intent(this, BMIActivity::class.java)
        startActivity(intent)
        finish()
    }
}
