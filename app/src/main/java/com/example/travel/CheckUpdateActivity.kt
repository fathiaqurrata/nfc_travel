package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CheckUpdateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_update)

        findViewById<Button>(R.id.button_continue).setOnClickListener {
            // Berpindah ke halaman login
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Menutup activity check update agar tidak kembali ke halaman ini
        }
    }
}
