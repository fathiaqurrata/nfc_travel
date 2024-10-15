package com.example.travel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
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
    private lateinit var fullname: String
    private val memberList = mutableListOf<Member>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

        userId = sharedPreferences.getString("user_id", "Unknown User") ?: "Unknown User"
        val userIdTextView = findViewById<TextView>(R.id.user_id_text_view)
        userIdTextView.text = "User ID: $userId"

        fullname = sharedPreferences.getString("fullname", "Unknown User") ?: "Unknown User"
        val fullnameTextView = findViewById<TextView>(R.id.name_user)
        fullnameTextView.text = "$fullname"

        val sortTextView = findViewById<TextView>(R.id.sort_button)
        sortTextView.setOnClickListener {
            showSortDialog()
        }

        fetchStatus(userId)
    }

    private fun fetchStatus(userId: String) {
        val url = "http://travel.selada.id/api/member/locations"
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
                        val jsonObject = JSONObject(responseBody)
                        val participantsArray: JSONArray = jsonObject.getJSONArray("participants")

                        for (i in 0 until participantsArray.length()) {
                            val memberObject = participantsArray.getJSONObject(i)
                            val fullname = memberObject.optString("fullname", "Unknown")
                            val phone = memberObject.optString("phone", "Unknown")
                            val classMember = memberObject.optString("class", "Unknown")
                            val status = memberObject.optInt("status", 0)

                            val attendanceStatus = if (status == 1) "Check In" else "Check Out"

                            memberList.add(Member(fullname, phone, classMember, attendanceStatus))
                        }

                        runOnUiThread {
                            updateRecyclerView()
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

    private fun updateRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this@StatusActivity, 1)
        val adapter = MemberAdapter(memberList) { member ->
            showMemberDetails(member)
        }
        recyclerView.adapter = adapter
    }

    // Menampilkan dialog pilihan untuk sorting
    private fun showSortDialog() {
        val options = arrayOf("Sort by Name", "Sort by Status")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sort Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sortMemberListByName() // Sort by Name
                    1 -> sortMemberListByStatus() // Sort by Status
                }
            }
            .show()
    }

    private fun sortMemberListByName() {
        memberList.sortBy { it.fullname }
        updateRecyclerView()
    }

    private fun sortMemberListByStatus() {
        memberList.sortBy { it.attendanceStatus }
        updateRecyclerView()
    }

    private fun showMemberDetails(member: Member) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Member Details")

        val message = """
            Name: ${member.fullname}
            Phone: ${member.phone}
            Kelas: ${member.classMember}
            Status: ${member.attendanceStatus}
        """.trimIndent()

        builder.setMessage(message)
            .setCancelable(true)
            .setPositiveButton("Call") { dialog, _ ->
                showPhoneNumber(member)
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }

        val alert = builder.create()
        alert.show()
    }

    private fun showPhoneNumber(member: Member) {
        val phoneNumber = member.phone

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$phoneNumber")
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "WhatsApp tidak terinstal.", Toast.LENGTH_SHORT).show()
        }
    }
}
