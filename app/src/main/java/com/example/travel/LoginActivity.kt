package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.edit_username)
        passwordEditText = findViewById(R.id.edit_password)

        findViewById<Button>(R.id.button_login).setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Username dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                login(username, password)
            }
        }
    }

    private fun login(username: String, password: String) {
        val url = "http://api.travel.selada.id/api/auth/login" // Ganti dengan URL login yang tepat

        val requestBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Kesalahan jaringan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("LOGIN_ACTIVITY", "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val message = jsonResponse.getString("message") // Ambil pesan dari respons

                        // Ambil ID pengguna dari respons
                        val userId = jsonResponse.getJSONObject("user").getString("id") // Ambil ID pengguna

                        // Simpan ID pengguna ke SharedPreferences
                        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        sharedPreferences.edit().putString("user_id", userId).apply() // Simpan ID pengguna

                        // Log isi SharedPreferences
                        logSharedPreferences()

                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    } catch (e: JSONException) {
                        Log.e("LOGIN_ACTIVITY", "JSON Parsing Error: ${e.message}")
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Kesalahan dalam respons dari server", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("LOGIN_ACTIVITY", "Error: ${response.message}, Body: $responseBody") // Log detail error
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Login gagal: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun logSharedPreferences() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        Log.d("SHARED_PREFERENCES", "User ID stored: $userId")
    }
}
