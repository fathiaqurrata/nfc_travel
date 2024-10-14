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
        val url = "http://travel.selada.id/api/auth/login"

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
                        val message = jsonResponse.getString("message")

                        val user = jsonResponse.getJSONObject("user")
                        val userId = user.getString("id")
                        val username = user.getString("username")
                        val email = user.getString("email")
                        val phone = user.getString("phone")
                        val fullname = user.getString("fullname")
                        val roleId = user.getString("role_id")
                        val idBus = user.getString("id_bus")

                        // Save all user data in SharedPreferences
                        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        sharedPreferences.edit()
                            .putString("user_id", userId)
                            .putString("username", username)
                            .putString("email", email)
                            .putString("phone", phone)
                            .putString("fullname", fullname)
                            .putString("role_id", roleId)
                            .putString("id_bus", idBus)
                            .apply()

                        // Log all saved data for verification
                        logSharedPreferences()

                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, LandingActivity::class.java))
                            finish()
                        }
                    } catch (e: JSONException) {
                        Log.e("LOGIN_ACTIVITY", "JSON Parsing Error: ${e.message}")
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Kesalahan dalam respons dari server", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("LOGIN_ACTIVITY", "Error: ${response.message}, Body: $responseBody")
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
        val username = sharedPreferences.getString("username", null)
        val email = sharedPreferences.getString("email", null)
        val phone = sharedPreferences.getString("phone", null)
        val fullname = sharedPreferences.getString("fullname", null)
        val roleId = sharedPreferences.getString("role_id", null)
        val idBus = sharedPreferences.getString("id_bus", null)

        Log.d("SHARED_PREFERENCES", "User Data Stored: \n" +
                "ID: $userId, \nUsername: $username, \nEmail: $email, \nPhone: $phone, \n" +
                "Fullname: $fullname, \nRole ID: $roleId, \nBus ID: $idBus")
    }
}
