package hr.isinkovic.myapplication.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val cardId: Int,
    val cardNumber: String,
    val expiryDate: String,
    //0 - visa, 1 - mastercard
    val cardProvider: Int
)