package com.example.travel

data class CheckInResponse(
    val fullname: String,
    val email: String,
    val phone: String,
    val balance: String,
    val status: String,
    val card_number: String,
    val updated_at: String
)