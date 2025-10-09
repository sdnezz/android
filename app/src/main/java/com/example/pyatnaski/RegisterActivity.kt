package com.example.pyatnaski

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.pyatnaski.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRegister.setOnClickListener {
            if (validateInput()) {
                registerUser()
            }
        }

        binding.textViewLoginPrompt.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInput(): Boolean {
        binding.textFieldEmail.error = null
        binding.textFieldPassword.error = null
        binding.textFieldConfirmPassword.error = null

        val email = binding.textFieldEmail.editText?.text.toString()
        val password = binding.textFieldPassword.editText?.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.editText?.text.toString()

        if (email.isEmpty()) {
            binding.textFieldEmail.error = getString(R.string.error_empty_field)
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textFieldEmail.error = getString(R.string.error_invalid_email)
            return false
        }

        if (password.isEmpty()) {
            binding.textFieldPassword.error = getString(R.string.error_empty_field)
            return false
        }

        if (password.length < 6) {
            binding.textFieldPassword.error = getString(R.string.error_password_too_short)
            return false
        }

        if (confirmPassword != password) {
            binding.textFieldConfirmPassword.error = getString(R.string.error_passwords_do_not_match)
            return false
        }

        return true
    }

    private fun registerUser() {
        val email = binding.textFieldEmail.editText?.text.toString()
        val password = binding.textFieldPassword.editText?.text.toString()

        val prefs = getSharedPreferences(AppPreferences.PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(AppPreferences.KEY_USER_EMAIL, email)
        editor.putString(AppPreferences.KEY_USER_PASSWORD, password)
        editor.apply()

        Snackbar.make(binding.root, R.string.registration_successful, Snackbar.LENGTH_LONG).show()

        binding.root.postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1500)
    }
}
