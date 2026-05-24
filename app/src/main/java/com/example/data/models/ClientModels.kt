package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val joinDateMillis: Long,
    val businessType: String, // Gym, Coaching, Tutoring, Beauty, etc.
    val feePlanName: String, // Starter, Growth, Enterprise, Custom
    val feeAmount: Double,
    val notes: String = "",
    val nextDueDateMillis: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // Default 30 days due
)

@Entity(tableName = "payment_records")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val amount: Double,
    val paymentDateMillis: Long,
    val notes: String = ""
)

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val completedDateMillis: Long = 0L
)
