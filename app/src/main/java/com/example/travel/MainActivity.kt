package com.example.travel

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.nfc.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var listView: ListView
    private var students = mutableListOf<Student>()
    private var isCheckInMode = true
    private val client = OkHttpClient()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        listView = findViewById(R.id.list_students)

        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

        updateListView()

        findViewById<Button>(R.id.check_in).setOnClickListener {
            isCheckInMode = true
            Toast.makeText(this, "Check-in mode active, tap NFC", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.check_out).setOnClickListener {
            isCheckInMode = false
            Toast.makeText(this, "Check-out mode active, tap NFC", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.check_out_all).setOnClickListener {
            students.forEach { it.checkedIn = false }
            updateListView()
            Toast.makeText(this, "All students checked out", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.status).setOnClickListener {
            val userId = sharedPreferences.getString("user_id", null)
            Log.d("MAIN_ACTIVITY", "User ID: $userId") // Tambahkan log
            if (userId != null) {
                val statusIntent = Intent(this, StatusActivity::class.java)
                statusIntent.putExtra("user_id", userId)
                startActivity(statusIntent)
            } else {
                Toast.makeText(this, "User ID not found in preferences", Toast.LENGTH_SHORT).show()
            }
        }

        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
    }

    private fun updateListView() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, students.map {
            "${it.fullname} - ${if (it.checkedIn) "Hadir" else "Tidak Hadir"}"
        })
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val student = students[position]
            Toast.makeText(this, "Info: ${student.phone}, ${student.grade}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        tag?.let {
            handleNfcTag(it, intent)
        }
    }

    private fun handleNfcTag(tag: Tag, intent: Intent) {
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMessages != null) {
            val messages = rawMessages.map { it as NdefMessage }
            var dataRead = false
            for (message in messages) {
                for (record in message.records) {
                    if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_TEXT)) {
                        val textEncoding = if (record.payload[0].toInt() and 128 == 0) "UTF-8" else "UTF-16"
                        val languageCodeLength = record.payload[0].toInt() and 63
                        val name = String(record.payload, languageCodeLength + 1, record.payload.size - languageCodeLength - 1, Charset.forName(textEncoding))

                        Log.d("NFC_TAG", "Data read from NFC: Name = $name")
                        showCheckInCheckOutDialog(name, null)
                        dataRead = true
                    }
                }
            }
            if (!dataRead) {
                Log.d("NFC_TAG", "No readable NDEF messages found in the NFC tag.")
            }
        } else {
            val tagId = tag.id.joinToString("") { String.format("%02X", it) }
            Log.d("NFC_TAG", "NFC Tag ID: $tagId")
            if (isCheckInMode) {
                checkInWithTag(tagId)
            } else {
                checkOutWithTag(tagId)
            }
        }
    }

    private fun checkInWithTag(tagId: String) {
        val url = "http://api.travel.selada.id/api/members/checkin"

        val requestBody = FormBody.Builder()
            .add("tag_nfc", tagId)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("NFC_TAG", "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val fullname = jsonResponse.getString("fullname")
                    val status = jsonResponse.getString("status")

                    runOnUiThread {
                        showCheckInCheckOutDialog(fullname, tagId)
                        Log.d("NFC_TAG", "Check-in response: $fullname, Status: $status")
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Failed to check-in: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("NFC_TAG", "Error: ${response.message}")
                }
            }
        })
    }

    private fun checkOutWithTag(tagId: String) {
        val url = "http://api.travel.selada.id/api/members/checkout"

        val requestBody = FormBody.Builder()
            .add("tag_nfc", tagId)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to check-out", Toast.LENGTH_SHORT).show()
                }
                Log.e("NFC_TAG", "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val fullname = jsonResponse.getString("fullname")
                    val status = jsonResponse.getString("status")

                    runOnUiThread {
                        showCheckInCheckOutDialog(fullname, tagId, false)
                        Log.d("NFC_TAG", "Check-out response: $fullname, Status: $status")
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Failed to check-out: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("NFC_TAG", "Error: ${response.message}")
                }
            }
        })
    }

    private fun showCheckInCheckOutDialog(name: String, tagId: String?, isCheckIn: Boolean = true) {
        val status = if (isCheckIn) "Check-In" else "Check-Out"
        val backgroundColor = if (isCheckIn) {
            Color.parseColor("#4CAF50")
        } else {
            Color.parseColor("#D75656")
        }

        val toast = Toast.makeText(this, "$status Successful: $name", Toast.LENGTH_SHORT)
        val toastView = layoutInflater.inflate(R.layout.toast_custom, null)
        val toastMessage = toastView.findViewById<TextView>(R.id.toast_message)
        toastMessage.text = "$status Successful $name"
        toastView.setBackgroundColor(backgroundColor)
        toast.view = toastView
        toast.show()

        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, 4000)

        val student = students.find { it.fullname == name }
        if (student != null) {
            student.checkedIn = isCheckIn
        } else {
            students.add(Student(name, "Unknown Phone", "Unknown Grade", isCheckIn, tagId))
        }
        updateListView()
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }
}
