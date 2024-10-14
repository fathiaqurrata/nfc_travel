package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val buttonRegister: Button = findViewById(R.id.button_register)
        val buttonDashboard: Button = findViewById(R.id.button_dashboard)

        buttonRegister.setOnClickListener {
            // Navigate to registration activity (you need to create RegisterActivity)
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        buttonDashboard.setOnClickListener {
            // Navigate to MainActivity (dashboard)
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Optional: finish this activity so it's removed from the back stack
        }
    }
}
