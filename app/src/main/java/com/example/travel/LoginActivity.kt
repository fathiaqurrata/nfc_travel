package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.edit_username)
        passwordEditText = findViewById(R.id.edit_password)
        loginButton = findViewById(R.id.button_login)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(username, password)
        }
    }

    private fun login(username: String, password: String) {
        val url = "http://api.travel.selada.id/api/auth/login"

        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        Log.d("LoginRequest", "Sending login request to: $url with username: $username") // Log request details

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LoginError", "Failed to login: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("LoginResponse", "Received response from: $url") // Log response details

                runOnUiThread {
                    if (response.isSuccessful) {
                        // Handle successful login
                        response.body?.string()?.let { responseBody ->
                            Log.i("LoginSuccess", "Response: $responseBody")
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        Log.w("LoginFailed", "Login failed with status: ${response.code} - ${response.message}")
                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle("Login Gagal")
                            .setMessage("Username atau password salah.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        })
    }
}
