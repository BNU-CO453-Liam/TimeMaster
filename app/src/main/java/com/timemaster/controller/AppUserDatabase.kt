package com.timemaster.controller

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.timemaster.model.AppUser

class AppUserDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UserDatabase"
        private const val TABLE_PROFILES = "UserTable"

        private const val KEY_ID = "_id"
        private const val KEY_USERNAME = "username"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // create table and fields
        val CREATE_ACCOUNTS_TABLE = ("CREATE TABLE " + TABLE_PROFILES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_USERNAME + " TEXT)")
        db?.execSQL(CREATE_ACCOUNTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, v1: Int, v2: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILES")
        onCreate(db)
    }

    /**
     * Create an account and add to database
     */
    fun addProfile(profile: AppUser): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_USERNAME, profile.username)

        // Insert account details using insert query.
        val success = db.insert(TABLE_PROFILES, null, contentValues)

        // Close database connection
        db.close()
        return success
    }

    /**
     * Read the records from database in form of ArrayList
     */
    @SuppressLint("Range")
    fun viewProfile(): ArrayList<AppUser> {

        val profileList: ArrayList<AppUser> = ArrayList<AppUser>()
        var id: Int
        var username: String

        // Query to select all the records from the table.
        val selectQuery = "SELECT  * FROM $TABLE_PROFILES"

        val db = this.readableDatabase

        // Cursor is used to read the record one by one. Add them to data model class.
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                username = cursor.getString(cursor.getColumnIndex(KEY_USERNAME))
                //passwd = cursor.getString(cursor.getColumnIndex(KEY_PASSWD))

                val profile = AppUser(id = id, username = username)
                profileList.add(profile)

            } while (cursor.moveToNext())
        }
        // Close database connection
        db.close()
        return profileList
    }

    /**
     * Update data in database
     */
    fun updateProfile(profile: AppUser): Int {

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_USERNAME, profile.username)

        // update row
        val success = db.update(TABLE_PROFILES, contentValues, KEY_ID + "=" + profile.id, null)
        db.close()
        return success
    }

    /**
     * Get row count
     */
    fun getCount(): Int {
        val count: Long = DatabaseUtils.queryNumEntries(this.readableDatabase, TABLE_PROFILES)
        return count.toInt()
    }

    /**
     * Delete all profiles
     */
    fun deleteAll() {
        val db = this.writableDatabase
        val removeProfile = db.execSQL("DELETE FROM $TABLE_PROFILES")
    }

    /**
     * drop table
     */
    fun reCreateTable() {
        val db = this.writableDatabase
        val dropTable = db.execSQL("DROP TABLE UserTable")
        onCreate(db)
        db.close()
    }
}