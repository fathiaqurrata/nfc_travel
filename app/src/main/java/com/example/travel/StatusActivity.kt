package com.example.travel

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import android.content.SharedPreferences
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.jsoup.Jsoup

class StatusActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        // Initialize sharedPreferences
        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

        // Get user ID from SharedPreferences and display it
        userId = sharedPreferences.getString("user_id", "Unknown User") ?: "Unknown User"
        val userIdTextView = findViewById<TextView>(R.id.user_id_text_view)
        userIdTextView.text = "User ID: $userId"

        // Fetch status information from the API
        fetchStatus(userId) // Pass the userId to the fetchStatus function
    }

    private fun fetchStatus(userId: String) {
        val url = "http://api.travel.selada.id/api/member/locations" // API endpoint without user_id in the URL

        // Create JSON object with the user ID to send in the request body
        val jsonBody = JSONObject()
        jsonBody.put("user_id", userId)

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonBody.toString()
        )

        // Create a POST request with the JSON body
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Log the request details
        Log.d("StatusActivity", "Sending request to URL: $url with body: $jsonBody")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle the error response
                Log.e("StatusActivity", "Request failed: ${e.message}")
                runOnUiThread {
                    findViewById<TextView>(R.id.status_text).text = "Error: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Log the response details
                Log.d("StatusActivity", "Response received: ${response.code} - ${response.message}")

                // Check if the response is successful
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""

                    // Check if the response is HTML
                    if (responseBody.startsWith("<!DOCTYPE html>")) {
                        // Parse the HTML response using Jsoup
                        val document = Jsoup.parse(responseBody)
                        val members = document.select("li") // Adjust this selector based on your HTML structure
                        val statusList = StringBuilder()

                        for (member in members) {
                            // Extract the text content from each member list item
                            statusList.append(member.text()).append("\n")
                        }

                        // Update the UI on the main thread
                        runOnUiThread {
                            if (statusList.isNotEmpty()) {
                                findViewById<TextView>(R.id.status_text).text = statusList.toString()
                            } else {
                                findViewById<TextView>(R.id.status_text).text = "No matching members found."
                            }
                        }
                    } else {
                        // Handle the case where the response is not HTML
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val statusArray = jsonResponse.getJSONArray("data")
                            val statusList = StringBuilder()

                            // Iterate through the status array and build the status list
                            for (i in 0 until statusArray.length()) {
                                val statusObj = statusArray.getJSONObject(i)
                                val fullname = statusObj.getString("fullname")
                                val checkInStatus = if (statusObj.getBoolean("checked_in")) "Hadir" else "Tidak Hadir"
                                statusList.append("$fullname - $checkInStatus\n")
                            }

                            // Update the UI on the main thread
                            runOnUiThread {
                                if (statusList.isNotEmpty()) {
                                    findViewById<TextView>(R.id.status_text).text = statusList.toString()
                                } else {
                                    findViewById<TextView>(R.id.status_text).text = "No matching members found."
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("StatusActivity", "Error parsing JSON: ${e.message}")
                            runOnUiThread {
                                findViewById<TextView>(R.id.status_text).text = "Failed to parse response."
                            }
                        }
                    }
                } else {
                    // Handle the unsuccessful response
                    runOnUiThread {
                        findViewById<TextView>(R.id.status_text).text = "Failed to fetch status: ${response.message}"
                    }
                }
            }
        })
    }
}
