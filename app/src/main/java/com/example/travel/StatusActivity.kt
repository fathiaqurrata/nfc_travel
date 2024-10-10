package com.example.travel

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import android.content.SharedPreferences
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class StatusActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private val memberList = mutableListOf<Member>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

        userId = sharedPreferences.getString("user_id", "Unknown User") ?: "Unknown User"
        val userIdTextView = findViewById<TextView>(R.id.user_id_text_view)
        userIdTextView.text = "User ID: $userId"

        fetchStatus(userId)
    }

    private fun fetchStatus(userId: String) {
        val url = "http://api.travel.selada.id/api/member/locations"
        val jsonBody = JSONObject().apply {
            put("user_id", userId)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonBody.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("StatusActivity", "Request failed: ${e.message}")
                runOnUiThread {
                    findViewById<TextView>(R.id.status_text).text = "Error: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    Log.d("StatusActivity", "Response: $responseBody")

                    try {
                        // Parse JSON response
                        val jsonObject = JSONObject(responseBody)
                        val membersArray: JSONArray = jsonObject.getJSONArray("members")

                        for (i in 0 until membersArray.length()) {
                            val memberObject = membersArray.getJSONObject(i)
                            val fullname = memberObject.getString("fullname") // Adjust this based on your JSON structure
                            val phone = memberObject.getString("phone") // Adjust this based on your JSON structure
                            val seat = memberObject.getString("seat") // Adjust this based on your JSON structure
                            memberList.add(Member(fullname, phone, seat, false))
                        }

                        runOnUiThread {
                            val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                            recyclerView.layoutManager = LinearLayoutManager(this@StatusActivity)
                            val adapter = MemberAdapter(memberList) { member -> showPhoneNumber(member) }
                            recyclerView.adapter = adapter
                        }
                    } catch (e: Exception) {
                        Log.e("StatusActivity", "Error parsing JSON: ${e.message}")
                        runOnUiThread {
                            findViewById<TextView>(R.id.status_text).text = "Failed to parse response."
                        }
                    }
                } else {
                    runOnUiThread {
                        findViewById<TextView>(R.id.status_text).text = "Failed to fetch status: ${response.message}"
                    }
                }
            }
        })
    }

    private fun showPhoneNumber(member: Member) {
        val message = "Phone: ${member.phone}"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
