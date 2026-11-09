package com.example.smartbmi

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    private lateinit var cbTerms: CheckBox
    private lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        // Views
        val btnBack = findViewById<Toolbar>(R.id.btnBackToWelcome)
        val btnFacebook = findViewById<FrameLayout>(R.id.btnFacebookSignIn)
        val btnGoogle = findViewById<FrameLayout>(R.id.btnGoogleSignIn)

        tilEmail = findViewById(R.id.tilSignUpEmail)
        tilPassword = findViewById(R.id.tilSignUpPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)

        etEmail = findViewById(R.id.etSignUpEmail)
        etPassword = findViewById(R.id.etSignUpPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        cbTerms = findViewById(R.id.cbTermsAgreement)
        btnSignUp = findViewById(R.id.btnSignUp)

        // Configure independent password visibility controls.
        setupPasswordVisibility(
            tilPassword,
            etPassword
        )

        setupPasswordVisibility(
            tilConfirmPassword,
            etConfirmPassword
        )

        // Clear field errors when the user starts correcting them.
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilEmail.error = null
        }

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilPassword.error = null
        }

        etConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilConfirmPassword.error = null
        }

        // Facebook callback.
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {

                override fun onSuccess(loginResult: LoginResult) {

                    if (!hasInternetConnection()) {
                        showInternetMessage()
                        return
                    }

                    val token = loginResult.accessToken.token

                    val credential =
                        FacebookAuthProvider.getCredential(token)

                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this@SignUpActivity) { task ->

                            if (task.isSuccessful) {

                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Facebook sign-in successful!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                openBMIActivity()

                            } else {

                                showAuthenticationError(
                                    task.exception?.localizedMessage
                                        ?: "Facebook sign-in failed."
                                )
                            }
                        }
                }

                override fun onCancel() {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Facebook sign-in cancelled.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(exception: FacebookException) {

                    if (!hasInternetConnection()) {
                        showInternetMessage()
                    } else {
                        Toast.makeText(
                            this@SignUpActivity,
                            exception.localizedMessage
                                ?: "Facebook sign-in failed.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )

        // Back to Welcome screen.
        btnBack.setNavigationOnClickListener {
            finish()
        }

        // Google sign-in.
        btnGoogle.setOnClickListener {
            if (hasInternetConnection()) {
                signInWithGoogle()
            } else {
                showInternetMessage()
            }
        }

        // Facebook sign-in.
        btnFacebook.setOnClickListener {
            if (hasInternetConnection()) {

                LoginManager.getInstance().logInWithReadPermissions(
                    this,
                    listOf("email", "public_profile")
                )

            } else {
                showInternetMessage()
            }
        }

        // Email registration.
        btnSignUp.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            tilEmail.error = null
            tilPassword.error = null
            tilConfirmPassword.error = null

            var isValid = true

            // Email validation.
            if (email.isEmpty()) {

                tilEmail.error = "Please enter your email address"
                isValid = false

            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                tilEmail.error = "Please enter a valid email address"
                isValid = false
            }

            // Password validation.
            if (password.isEmpty()) {

                tilPassword.error = "Please enter your password"
                isValid = false

            } else if (password.length < 6) {

                tilPassword.error =
                    "Password must be at least 6 characters"

                isValid = false
            }

            // Confirm password validation.
            if (confirmPassword.isEmpty()) {

                tilConfirmPassword.error =
                    "Please confirm your password"

                isValid = false

            } else if (password != confirmPassword) {

                tilConfirmPassword.error =
                    "Passwords do not match"

                isValid = false
            }

            // Terms agreement validation.
            if (!cbTerms.isChecked) {

                Toast.makeText(
                    this,
                    "Please accept the Terms of Agreement",
                    Toast.LENGTH_SHORT
                ).show()

                isValid = false
            }

            // Stop registration if validation fails.
            if (!isValid) {

                when {
                    tilEmail.error != null -> etEmail.requestFocus()

                    tilPassword.error != null -> etPassword.requestFocus()

                    tilConfirmPassword.error != null ->
                        etConfirmPassword.requestFocus()
                }

                return@setOnClickListener
            }

            // Check internet connection.
            if (!hasInternetConnection()) {
                showInternetMessage()
                return@setOnClickListener
            }

            btnSignUp.isEnabled = false
            btnSignUp.text = "Creating account..."

            // Firebase registration.
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@SignUpActivity) { task ->

                    if (task.isSuccessful) {

                        AlertDialog.Builder(this@SignUpActivity)
                            .setTitle("Registration Successful!")
                            .setMessage(
                                "Your Smart BMI account has been created successfully."
                            )
                            .setPositiveButton("Continue") { _, _ ->

                                showSavePasswordDialog(
                                    email,
                                    password
                                )
                            }
                            .setCancelable(false)
                            .show()

                    } else {

                        btnSignUp.isEnabled = true
                        btnSignUp.text = getString(R.string.sign_up)

                        if (!hasInternetConnection()) {

                            showInternetMessage()

                        } else {

                            Toast.makeText(
                                this@SignUpActivity,
                                task.exception?.localizedMessage
                                    ?: "Registration failed. Please try again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }
    }

    // Independent password visibility toggle.
    private fun setupPasswordVisibility(
        textInputLayout: TextInputLayout,
        editText: EditText
    ) {

        // Always start with the password hidden.
        editText.transformationMethod =
            PasswordTransformationMethod.getInstance()

        textInputLayout.setEndIconDrawable(R.drawable.ic_eye_off)

        var isPasswordVisible = false

        textInputLayout.setEndIconOnClickListener {

            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {

                // Reveal password.
                editText.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()

                textInputLayout.setEndIconDrawable(
                    R.drawable.ic_eye
                )

            } else {

                // Hide password.
                editText.transformationMethod =
                    PasswordTransformationMethod.getInstance()

                textInputLayout.setEndIconDrawable(
                    R.drawable.ic_eye_off
                )
            }

            // Keep cursor at the end of the password.
            editText.setSelection(
                editText.text?.length ?: 0
            )
        }
    }

    // Check for a validated internet connection.
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

    // Show internet connection message.
    private fun showInternetMessage() {

        Toast.makeText(
            this,
            "No internet connection. Please connect to the internet and try again.",
            Toast.LENGTH_LONG
        ).show()
    }

    // Show authentication error.
    private fun showAuthenticationError(message: String) {

        if (!hasInternetConnection()) {
            showInternetMessage()
        } else {
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Open BMI dashboard.
    private fun openBMIActivity() {

        startActivity(
            Intent(this, BMIActivity::class.java)
        )

        finish()
    }

    // Ask whether the user wants to save the password.
    private fun showSavePasswordDialog(
        email: String,
        password: String
    ) {

        AlertDialog.Builder(this)
            .setTitle("Save Password?")
            .setMessage(
                "Would you like to save your password securely " +
                        "on this device for easier sign-in next time?"
            )
            .setPositiveButton("Yes, Save Password") { _, _ ->

                offerToSavePassword(email, password) {
                    finish()
                }
            }
            .setNegativeButton("No, Thanks") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // Save password through Android Credential Manager.
    private fun offerToSavePassword(
        email: String,
        password: String,
        onComplete: () -> Unit
    ) {

        lifecycleScope.launch {

            try {

                val credentialManager =
                    CredentialManager.create(this@SignUpActivity)

                credentialManager.createCredential(
                    this@SignUpActivity,
                    CreatePasswordRequest(
                        id = email,
                        password = password
                    )
                )

            } catch (e: CreateCredentialException) {

                android.util.Log.e(
                    "SmartBMI",
                    "Credential save failed",
                    e
                )

            } catch (e: Exception) {

                android.util.Log.e(
                    "SmartBMI",
                    "Unexpected credential error",
                    e
                )

            } finally {

                onComplete()
            }
        }
    }

    // Forward Facebook results.
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        callbackManager.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }

    // Google sign-in.
    private fun signInWithGoogle() {

        val credentialManager =
            CredentialManager.create(this)

        val googleOption =
            GetSignInWithGoogleOption.Builder(
                getString(R.string.default_web_client_id)
            ).build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()

        lifecycleScope.launch {

            try {

                val result = credentialManager.getCredential(
                    context = this@SignUpActivity,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(
                            credential.data
                        )

                    val firebaseCredential =
                        GoogleAuthProvider.getCredential(
                            googleCredential.idToken,
                            null
                        )

                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this@SignUpActivity) { task ->

                            if (task.isSuccessful) {

                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Google sign-in successful!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                openBMIActivity()

                            } else {

                                showAuthenticationError(
                                    task.exception?.localizedMessage
                                        ?: "Google sign-in failed."
                                )
                            }
                        }

                } else {

                    Toast.makeText(
                        this@SignUpActivity,
                        "Please select a Google account.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: GetCredentialException) {

                showAuthenticationError(
                    e.localizedMessage
                        ?: "Google sign-in was cancelled or failed."
                )

            } catch (e: Exception) {

                showAuthenticationError(
                    e.localizedMessage
                        ?: "Something went wrong."
                )
            }
        }
    }
}
