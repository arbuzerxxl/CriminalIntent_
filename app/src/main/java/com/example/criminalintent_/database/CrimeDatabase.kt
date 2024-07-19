package com.example.criminalintent_.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.criminalintent_.Crime

@Database(entities = [Crime::class], version = 4, exportSchema = false)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase() {
    abstract fun CrimeDao(): CrimeDao
}

val migration_1_2 = object : Migration(1,2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Crime ADD COLUMN requiresPolice INTEGER NOT NULL")
    }
}
val migration_2_3 = object : Migration(2,3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''")
    }
}
val migration_3_4 = object : Migration(3,4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Crime ADD COLUMN suspectPhoneNumber TEXT NOT NULL DEFAULT ''")
    }
}