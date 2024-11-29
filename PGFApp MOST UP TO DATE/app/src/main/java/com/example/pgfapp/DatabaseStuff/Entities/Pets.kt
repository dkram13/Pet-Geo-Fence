package com.example.pgfapp.DatabaseStuff.Entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Pets(
    @PrimaryKey(autoGenerate = true) val PetId: Int = 0,
    val IMEI: String,
    val PetName: String,
    val UUID: String,
) : Serializable
