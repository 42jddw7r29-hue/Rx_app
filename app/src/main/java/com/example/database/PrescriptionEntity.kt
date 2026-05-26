package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val resultText: String,
    val imageUrl: String? = null,
    val type: String // "PRESCRIPTION" or "TEST"
)
