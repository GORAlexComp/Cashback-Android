package ua.pp.gac.cashback_android.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class DatabaseHelper(context: Context) :
	SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
	companion object {
		const val DATABASE_NAME = "cashback.db"
		const val DATABASE_VERSION = 1
	}

	override fun onCreate(db: SQLiteDatabase) {
		ProductsTable.onCreate(db)
		RetailersTable.onCreate(db)
		FavoritesTable.onCreate(db)
		SettingsTable.onCreate(db)
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		ProductsTable.onUpgrade(db, oldVersion, newVersion)
		RetailersTable.onUpgrade(db, oldVersion, newVersion)
		FavoritesTable.onUpgrade(db, oldVersion, newVersion)
		SettingsTable.onUpgrade(db, oldVersion, newVersion)
		onCreate(db)
	}

	fun executeSQLFile(db: SQLiteDatabase, file: File) {
		try {
			val reader = BufferedReader(FileReader(file))

			try {
				db.beginTransaction()
				reader.forEachLine { line ->
					if ((line.trim()).isNotEmpty()) {
						db.execSQL(line)
					}
				}
				db.setTransactionSuccessful()
			} catch (e: Exception) {
				Log.e("UpdateProducts", "Failed to execute SQL file: ${e.message}")
				e.printStackTrace()
			} finally {
				db.endTransaction()
				reader.close()
			}
		} catch (e: Exception) {
			Log.e("UpdateProducts", "Failed to read SQL file: ${e.message}")
			e.printStackTrace()
		}
	}
}
