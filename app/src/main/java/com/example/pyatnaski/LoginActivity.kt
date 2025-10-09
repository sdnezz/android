package com.example.pyatnaski

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pyatnaski.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            loginUser()
        }

        binding.textViewRegisterPrompt.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val email = binding.textFieldEmail.editText?.text.toString()
        val password = binding.textFieldPassword.editText?.text.toString()

        val prefs = getSharedPreferences(AppPreferences.PREFS_NAME, MODE_PRIVATE)
        val savedEmail = prefs.getString(AppPreferences.KEY_USER_EMAIL, null)
        val savedPassword = prefs.getString(AppPreferences.KEY_USER_PASSWORD, null)

        if (email == savedEmail && password == savedPassword) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        } else {
            Snackbar.make(binding.root, R.string.login_failed, Snackbar.LENGTH_LONG).show()
        }
    }
}
