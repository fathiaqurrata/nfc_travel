package com.example.travel

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StatusActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var students = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        listView = findViewById(R.id.list_students)

        val studentsJson = intent.getStringExtra("studentsList")
        if (studentsJson != null) {
            val type = object : TypeToken<MutableList<Student>>() {}.type
            students = Gson().fromJson(studentsJson, type)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, students.map {
            "${it.fullname} - ${if (it.checkedIn) "Hadir" else "Tidak Hadir"}"
        })
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val student = students[position]
            Toast.makeText(this, "Info: ${student.phone}, ${student.grade}", Toast.LENGTH_SHORT).show()
        }
    }
}
