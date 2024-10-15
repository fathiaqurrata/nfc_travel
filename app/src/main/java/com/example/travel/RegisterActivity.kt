package com.example.travel

import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullnameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var cardNumberEditText: EditText
    private lateinit var seatEditText: EditText
    private lateinit var classEditText: EditText
    private val client = OkHttpClient()
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fullnameEditText = findViewById(R.id.edit_fullname)
        phoneNumberEditText = findViewById(R.id.edit_phone_number)
        cardNumberEditText = findViewById(R.id.edit_card_number)
        seatEditText = findViewById(R.id.edit_seat)
        classEditText = findViewById(R.id.edit_class)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showNfcDialog()

        findViewById<Button>(R.id.button_register).setOnClickListener {
            registerUser()
        }
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }

    private fun enableNfcForegroundDispatch() {
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, null)
    }

    private fun disableNfcForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            val nfcId = tag?.id?.let { bytesToHex(it) }
            if (nfcId != null) {
                saveCardNumber(nfcId)
                nfcDialog.dismiss()
            }
        }
    }

    private fun showNfcDialog() {
        nfcDialog = Dialog(this)
        nfcDialog.setContentView(R.layout.dialog_tap_nfc)
        nfcDialog.setCancelable(false)

        val closeButton = nfcDialog.findViewById<Button>(R.id.button_close)
        closeButton.setOnClickListener {
            nfcDialog.dismiss()
        }

        nfcDialog.show()
    }

    private fun saveCardNumber(cardNumber: String) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("card_number", cardNumber).apply()
        cardNumberEditText.setText(cardNumber)
        Toast.makeText(this, "Card number saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }

    private fun registerUser() {
        val fullName = fullnameEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val cardNumber = cardNumberEditText.text.toString().trim()
        val seatNumber = seatEditText.text.toString().trim()
        val classNumber = classEditText.text.toString().trim()

        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val busId = sharedPreferences.getString("id_bus", null)

        if (fullName.isEmpty() || phoneNumber.isEmpty() || cardNumber.isEmpty() || seatNumber.isEmpty() || classNumber.isEmpty() || busId.isNullOrEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObject = JSONObject()
        try {
            jsonObject.put("fullname", fullName)
            jsonObject.put("phone_number", phoneNumber)
            jsonObject.put("card_number", cardNumber)
            jsonObject.put("seat", seatNumber)
            jsonObject.put("class", classNumber)
            jsonObject.put("bus_location", busId)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonObject.toString())
        val url = "http://travel.selada.id/api/auth/register/member"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration failed: ${response.body?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
