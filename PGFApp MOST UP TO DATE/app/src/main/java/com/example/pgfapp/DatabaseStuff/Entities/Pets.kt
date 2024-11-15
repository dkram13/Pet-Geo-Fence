package com.example.pgfapp.DatabaseStuff.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Pets(
    @PrimaryKey(autoGenerate = true) val PetId: Int = 0,
    val IMEI: Int,
    val PetName: String,
)
