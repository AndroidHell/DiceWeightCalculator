package com.androidhell.diceweightcalculator.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "dice_goblin.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_ROLLS = "rolls"
        private const val COLUMN_SESSION_NAME = "session_id"
        private const val COLUMN_DICE_INDEX = "dice_index"
        private const val COLUMN_DICE_TYPE = "dice_type"
        private const val COLUMN_ROLL_NUMBER = "roll_number"
        private const val COLUMN_ROLL_VALUE = "roll_value"

        private const val TABLE_STATS = "statistics"
        private const val COLUMN_MEAN = "mean"
        private const val COLUMN_MEDIAN = "median"
        private const val COLUMN_MODE = "mode"
        private const val COLUMN_ODD_ROLLS = "odd_rolls"
        private const val COLUMN_EVEN_ROLLS = "even_rolls"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createRollsTableSQL = """
        CREATE TABLE $TABLE_ROLLS (
            $COLUMN_SESSION_NAME TEXT,
            $COLUMN_DICE_INDEX INTEGER,
            $COLUMN_DICE_TYPE STRING,
            $COLUMN_ROLL_NUMBER INTEGER,
            $COLUMN_ROLL_VALUE INTEGER,
            PRIMARY KEY ($COLUMN_SESSION_NAME, $COLUMN_DICE_INDEX, $COLUMN_ROLL_NUMBER)
        )
    """.trimIndent()
        db.execSQL(createRollsTableSQL)

        val createStatisticsTableSQL = """
        CREATE TABLE $TABLE_STATS (
            $COLUMN_SESSION_NAME TEXT,
            $COLUMN_DICE_TYPE STRING,
            $COLUMN_MEAN DOUBLE, 
            $COLUMN_MEDIAN DOUBLE,
            $COLUMN_MODE DOUBLE,
            $COLUMN_ODD_ROLLS INT,
            $COLUMN_EVEN_ROLLS INT,
            PRIMARY KEY ($COLUMN_SESSION_NAME, $COLUMN_DICE_TYPE)
        )
    """.trimIndent()
        db.execSQL(createStatisticsTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database schema upgrades here if needed
    }

    fun insertRoll(rollData: RollData) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SESSION_NAME, rollData.sessionName)
            put(COLUMN_DICE_INDEX, rollData.diceIndex)
            put(COLUMN_DICE_TYPE, rollData.diceType)
            put(COLUMN_ROLL_NUMBER, rollData.rollNumber)
            put(COLUMN_ROLL_VALUE, rollData.rollValue)
        }
        db.insertWithOnConflict(TABLE_ROLLS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    @SuppressLint("Recycle", "Range")
    fun calculateAndInsertStatistics(sessionName: String) {
        val db = writableDatabase

        val query = """
            SELECT $COLUMN_DICE_TYPE, $COLUMN_ROLL_VALUE
            FROM $TABLE_ROLLS
            WHERE $COLUMN_SESSION_NAME = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(sessionName))
        val statisticsMap = mutableMapOf<String, RollStatsMath>()

        while (cursor.moveToNext()) {
            val diceType = cursor.getString(cursor.getColumnIndex(COLUMN_DICE_TYPE))
            val rollValue =cursor.getInt(cursor.getColumnIndex(COLUMN_ROLL_VALUE))

            val rollStatistics = statisticsMap[diceType] ?: RollStatsMath()
            rollStatistics.addRoll(rollValue)
            statisticsMap[diceType] =rollStatistics
        }

        for ((diceType, rollStatistic) in statisticsMap) {
            val values = ContentValues().apply {
                put(COLUMN_SESSION_NAME, sessionName)
                put(COLUMN_DICE_TYPE, diceType)
                put(COLUMN_MEAN, rollStatistic.mean())
                put(COLUMN_MEDIAN, rollStatistic.median())
                put(COLUMN_MODE, rollStatistic.mode())
                put(COLUMN_ODD_ROLLS, rollStatistic.oddRolls())
                put(COLUMN_EVEN_ROLLS, rollStatistic.evenRolls())
            }

            db.insertWithOnConflict(TABLE_STATS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }

        cursor.close()
        db.close()
    }

    @SuppressLint("Range")
    fun getRollStats(sessionName: String): List<RollStatistics> {
        val resultsList = mutableListOf<RollStatistics>()
        val db = readableDatabase

        val query = """
            SELECT $COLUMN_DICE_TYPE, $COLUMN_MEAN, $COLUMN_MEDIAN, $COLUMN_MODE, $COLUMN_ODD_ROLLS, $COLUMN_EVEN_ROLLS
            FROM $TABLE_STATS
            WHERE $COLUMN_SESSION_NAME = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(sessionName))

        while (cursor.moveToNext()) {
            val diceType = cursor.getString(cursor.getColumnIndex(COLUMN_DICE_TYPE))
            val mean = cursor.getDouble(cursor.getColumnIndex(COLUMN_MEAN))
            val median = cursor.getDouble(cursor.getColumnIndex(COLUMN_MEDIAN))
            val mode = cursor.getDouble(cursor.getColumnIndex(COLUMN_MODE))
            val oddRolls = cursor.getInt(cursor.getColumnIndex(COLUMN_ODD_ROLLS))
            val evenRolls = cursor.getInt(cursor.getColumnIndex(COLUMN_EVEN_ROLLS))
            val rollStatistics = RollStatistics(sessionName, diceType, mean, median, mode, oddRolls, evenRolls)
            resultsList.add(rollStatistics)
        }

        cursor.close()
        db.close()
        return resultsList
    }

    @SuppressLint("Range")
    fun getAllSessionsWithStats(): List<Pair<String, List<RollStatistics>>> {
        val resultsList = mutableListOf<Pair<String, List<RollStatistics>>>()
        val db = readableDatabase

        val query = """
        SELECT DISTINCT $COLUMN_SESSION_NAME
        FROM $TABLE_STATS
    """.trimIndent()

        val sessionCursor = db.rawQuery(query, null)

        while (sessionCursor.moveToNext()) {
            val sessionName = sessionCursor.getString(sessionCursor.getColumnIndex(COLUMN_SESSION_NAME))
            val stats = getRollStats(sessionName) // Use your existing getRollStats function to retrieve stats for each session
            resultsList.add(Pair(sessionName, stats))
        }

        sessionCursor.close()
        db.close()
        return resultsList
    }

    @SuppressLint("Range")
    fun getAllRollStatistics(): List<RollStatistics> {
        val rollStatisticsList = mutableListOf<RollStatistics>()
        val db = readableDatabase

        val query = "SELECT $COLUMN_SESSION_NAME, $COLUMN_DICE_TYPE, $COLUMN_MEAN, $COLUMN_MEDIAN, " +
                "$COLUMN_MODE, $COLUMN_EVEN_ROLLS, $COLUMN_ODD_ROLLS FROM $TABLE_STATS"

        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {
                val sessionName = it.getString(it.getColumnIndex(COLUMN_SESSION_NAME))
                val diceType = it.getString(it.getColumnIndex(COLUMN_DICE_TYPE))
                val mean = it.getDouble(it.getColumnIndex(COLUMN_MEAN))
                val median = it.getDouble(it.getColumnIndex(COLUMN_MEDIAN))
                val mode = it.getDouble(it.getColumnIndex(COLUMN_MODE))
                val evenRolls = it.getInt(it.getColumnIndex(COLUMN_EVEN_ROLLS))
                val oddRolls = it.getInt(it.getColumnIndex(COLUMN_ODD_ROLLS))

                val rollStatistics = RollStatistics(
                    sessionName,
                    diceType,
                    mean,
                    median,
                    mode,
                    evenRolls,
                    oddRolls
                )

                rollStatisticsList.add(rollStatistics)
            }
        }

        db.close()

        return rollStatisticsList
    }

    @SuppressLint("Range")
    fun getGlobalRollStatistics(): List<RollStatistics> {
        val db = readableDatabase
        val query = "SELECT DISTINCT $COLUMN_DICE_TYPE FROM $TABLE_ROLLS"
        val cursor = db.rawQuery(query, null)

        val statisticsList = mutableListOf<RollStatistics>()

        while (cursor.moveToNext()) {
            val diceType = cursor.getString(cursor.getColumnIndex(COLUMN_DICE_TYPE))
            val rollsForDiceType = getRollsForDiceType(diceType)

            if (rollsForDiceType.isNotEmpty()) {
                val rollStatsMath = RollStatsMath()
                for (rollValue in rollsForDiceType) {
                    rollStatsMath.addRoll(rollValue)
                }

                val statistics = RollStatistics(
                    sessionName = "",
                    diceType = diceType,
                    mean = rollStatsMath.mean(),
                    median = rollStatsMath.median(),
                    mode = rollStatsMath.mode(),
                    oddRolls = rollStatsMath.oddRolls(),
                    evenRolls = rollStatsMath.evenRolls()
                )

                statisticsList.add(statistics)
            }
        }

        cursor.close()
        db.close()

        return statisticsList
    }

    @SuppressLint("Range")
    private fun getRollsForDiceType(diceType: String): List<Int> {
        val db = readableDatabase
        val query = "SELECT $COLUMN_ROLL_VALUE FROM $TABLE_ROLLS WHERE $COLUMN_DICE_TYPE = ?"
        val cursor = db.rawQuery(query, arrayOf(diceType))

        val rollsList = mutableListOf<Int>()

        while (cursor.moveToNext()) {
            val rollValue = cursor.getInt(cursor.getColumnIndex(COLUMN_ROLL_VALUE))
            rollsList.add(rollValue)
        }

        cursor.close()
        db.close()

        return rollsList
    }

    @SuppressLint("Range")
    fun getAllRolls(sessionName: String): List<RollData> {
        val rollList = mutableListOf<RollData>()
        val db = readableDatabase

        val query = """
            SELECT $COLUMN_DICE_TYPE, $COLUMN_MEAN, $COLUMN_MEDIAN, $COLUMN_MODE, $COLUMN_ODD_ROLLS, $COLUMN_EVEN_ROLLS
            FROM $TABLE_STATS
            WHERE $COLUMN_SESSION_NAME = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(sessionName))

        while (cursor.moveToNext()) {
            val diceIndex = cursor.getInt(cursor.getColumnIndex(COLUMN_DICE_INDEX))
            val diceType = cursor.getString(cursor.getColumnIndex(COLUMN_DICE_TYPE))
            val rollNumber = cursor.getInt(cursor.getColumnIndex(COLUMN_ROLL_NUMBER))
            val rollValue = cursor.getInt(cursor.getColumnIndex(COLUMN_ROLL_VALUE))
            val rollData = RollData(sessionName, diceIndex, diceType, rollNumber, rollValue)
            rollList.add(rollData)
        }
        cursor.close()
        db.close()
        return rollList
    }

    @SuppressLint("Range")
    fun getPreviousRoll(sessionName: String, diceIndex: Int, diceType: String?, currentRollNumber: Int): RollData? {
        val db = readableDatabase
        val query = """
        SELECT * FROM $TABLE_ROLLS
        WHERE $COLUMN_SESSION_NAME = ? AND $COLUMN_DICE_INDEX = ? AND $COLUMN_ROLL_NUMBER = ?
    """.trimIndent()

        val previousRollNumber = currentRollNumber - 1

        val cursor = db.rawQuery(query, arrayOf(sessionName, diceIndex.toString(), previousRollNumber.toString()))
        val previousRoll: RollData? = if (cursor.moveToFirst()) {
            val rollValue = cursor.getInt(cursor.getColumnIndex(COLUMN_ROLL_VALUE))
            RollData(sessionName, diceIndex, diceType, previousRollNumber, rollValue)
        } else {
            null
        }
        cursor.close()
        db.close()
        return previousRoll
    }

    @SuppressLint("Range")
    fun getNextRoll(sessionName: String, diceIndex: Int, diceType: String?, currentRollNumber: Int): RollData? {
        val db = readableDatabase
        val query = """
        SELECT * FROM $TABLE_ROLLS
        WHERE $COLUMN_SESSION_NAME = ? AND $COLUMN_DICE_INDEX = ? AND $COLUMN_ROLL_NUMBER = ?
    """.trimIndent()

        val nextRollNumber = currentRollNumber + 1

        val cursor = db.rawQuery(query, arrayOf(sessionName, diceIndex.toString(), nextRollNumber.toString()))
        val nextRoll: RollData? = if (cursor.moveToFirst()) {
            val rollValue = cursor.getInt(cursor.getColumnIndex(COLUMN_ROLL_VALUE))
            RollData(sessionName, diceIndex, diceType, nextRollNumber, rollValue)
        } else {
            null
        }
        cursor.close()
        db.close()
        return nextRoll
    }

    fun deleteSessionRollsData(sessionName: String){
        val db = writableDatabase
        val whereClause = "$COLUMN_SESSION_NAME = ?"
        val whereArgs = arrayOf(sessionName)
        db.delete(TABLE_ROLLS, whereClause, whereArgs)
        db.close()
    }

    fun deleteSessionStatsData(sessionName: String){
        val db = writableDatabase
        val whereClause = "$COLUMN_SESSION_NAME = ?"
        val whereArgs = arrayOf(sessionName)
        db.delete(TABLE_STATS, whereClause, whereArgs)
        db.close()
    }

    fun sessionNameExists(sessionName: String): Boolean {
        val db = readableDatabase
        val query = """
        SELECT COUNT(*) FROM $TABLE_ROLLS
        WHERE $COLUMN_SESSION_NAME = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(sessionName))
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()

        return count > 0
    }

    fun deleteAllData() {
        val db = writableDatabase
        try {
            db.beginTransaction()
            db.delete(TABLE_ROLLS, null, null)
            db.delete(TABLE_STATS, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    @SuppressLint("Range")
    fun logAllRolls() {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ROLLS", null)

        if (cursor.moveToFirst()) {
            do {
                val sessionName = cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_NAME))
                val diceIndex = cursor.getInt(cursor.getColumnIndex(COLUMN_DICE_INDEX))
                val diceType = cursor.getString(cursor.getColumnIndex(COLUMN_DICE_TYPE)) // Retrieve dice type
                val rollNumber = cursor.getInt(cursor.getColumnIndex(COLUMN_ROLL_NUMBER))
                val rollValue = cursor.getInt(cursor.getColumnIndex(COLUMN_ROLL_VALUE))

                val rollData = RollData(sessionName, diceIndex, diceType, rollNumber, rollValue)

                // Log each row of data
                Log.d("MyApp", "Stored Roll: $rollData")
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
    }
}

class RollStatsMath {
    private val rolls = mutableListOf<Int>()

    fun  addRoll(rollValue: Int) {
        rolls.add(rollValue)
    }

    fun mean(): Double {
        if (rolls.isEmpty()) return 0.0
        return rolls.average()
    }

    fun median(): Double {
        if (rolls.isEmpty()) return 0.0
        val sortedRolls = rolls.sorted()
        val middle = sortedRolls.size / 2
        if (sortedRolls.size % 2 == 0) {
            return (sortedRolls[middle - 1] + sortedRolls[middle] / 2.0)
        }
        return sortedRolls[middle].toDouble()
    }

    fun mode(): Double {
        if (rolls.isEmpty()) return 0.0
        val frequencyMap = mutableMapOf<Int, Int>()
        for (roll in rolls) {
            frequencyMap[roll] = frequencyMap.getOrDefault(roll, 0) + 1
        }
        val modeValue = frequencyMap.maxByOrNull { it.value }?.key ?: 0
        return modeValue.toDouble()
    }

    fun oddRolls(): Int {
        return rolls.count { it % 2 != 0 }
    }

    fun evenRolls(): Int {
        return rolls.count { it % 2 == 0 }
    }
}