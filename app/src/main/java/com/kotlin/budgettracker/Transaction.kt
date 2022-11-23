package com.kotlin.budgettracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val label: String,
    val amount: Double,
    val description: String):java.io.Serializable {
    override fun toString(): String {
        return "id : $id "
    }
}