package com.example.criminalintent_.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date
import java.util.UUID

@TypeConverters
class CrimeTypeConverters {

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millisecondEpoch: Long?): Date? {
        return millisecondEpoch?.let { Date(it) }
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

}