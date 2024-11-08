package com.example.pgfapp

import androidx.room.PrimaryKey

data class Pets(
    @PrimaryKey
    val IMEI: Int,
    val PetName: String,
)
