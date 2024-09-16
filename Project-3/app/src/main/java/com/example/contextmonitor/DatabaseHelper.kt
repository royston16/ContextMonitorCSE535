package com.example.contextmonitor

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "symptoms.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "symptoms"
        private const val ID = "id"
        private const val HEART_RATE = "heartRate"
        private const val RESPIRATORY_RATE = "respiratoryRate"
        private const val FEVER = "fever"
        private const val NAUSEA = "nausea"
        private const val HEADACHE = "headache"
        private const val DIARRHEA = "diarrhea"
        private const val SOAR_THROAT = "soarThroat"
        private const val MUSCLE_ACHE = "muscleAche"
        private const val LOSS_OF_SMELL_OR_TASTE = "lossOfSmellOrTaste"
        private const val COUGH = "cough"
        private const val SHORTNESS_OF_BREATH = "shortnessOfBreath"
        private const val FEELING_TIRED = "feelingTired"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$HEART_RATE INTEGER, " +
                "$RESPIRATORY_RATE INTEGER, " +
                "$FEVER INTEGER, " +
                "$NAUSEA INTEGER, " +
                "$HEADACHE INTEGER, " +
                "$DIARRHEA INTEGER, " +
                "$SOAR_THROAT INTEGER, " +
                "$MUSCLE_ACHE INTEGER, " +
                "$LOSS_OF_SMELL_OR_TASTE INTEGER, " +
                "$COUGH INTEGER, " +
                "$SHORTNESS_OF_BREATH INTEGER, " +
                "$FEELING_TIRED INTEGER)"
                )
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertSymptoms(heartRate: Int, respiratoryRate: Int, ratings: Map<String, Int>): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HEART_RATE, heartRate)
        contentValues.put(RESPIRATORY_RATE, respiratoryRate)
        contentValues.put(FEVER, ratings["Fever"])
        contentValues.put(NAUSEA, ratings["Nausea"])
        contentValues.put(HEADACHE, ratings["Headache"])
        contentValues.put(DIARRHEA, ratings["Diarrhea"])
        contentValues.put(SOAR_THROAT, ratings["Soar Throat"])
        contentValues.put(MUSCLE_ACHE, ratings["Muscle Ache"])
        contentValues.put(LOSS_OF_SMELL_OR_TASTE, ratings["Loss of Smell or Taste"])
        contentValues.put(COUGH, ratings["Cough"])
        contentValues.put(SHORTNESS_OF_BREATH, ratings["Shortness of Breath"])
        contentValues.put(FEELING_TIRED, ratings["Feeling Tired"])

        return db.insert(TABLE_NAME, null, contentValues)
    }
}
