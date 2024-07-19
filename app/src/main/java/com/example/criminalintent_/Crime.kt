package com.example.criminalintent_

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

// model - M
@Entity
data class Crime(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var requiresPolice: Boolean = false,
    var suspect: String = "",
    var suspectPhoneNumber: String = ""
) {
    val photoFileName
        get() = "IMG_$id.jpg"
}