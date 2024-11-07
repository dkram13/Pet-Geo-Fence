package com.example.pgfapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bounds(
    @PrimaryKey
    val BoundsName: String,
    val b_Area: String,
    val UUID: String

)
