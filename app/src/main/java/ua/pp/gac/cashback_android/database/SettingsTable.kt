package ua.pp.gac.cashback_android.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log

object SettingsTable {
	private const val TABLE_SETTINGS = "settings"
	private const val COLUMN_ID = "id"
	private const val COLUMN_NAME = "name"
	private const val COLUMN_VALUE = "value"

	private const val KEY_PRODUCT_VERSION = "products_version"
	private const val KEY_RETAILER_VERSION = "retailers_version"
	private const val KEY_ECONOMY_MODE = "economy_mode"
	private const val KEY_THEME_MODE = "theme_mode"

	/*data class Settings(
		val name: String?,
		val value: String?,
	)*/

	fun onCreate(db: SQLiteDatabase) {
		val createTable = """
            CREATE TABLE $TABLE_SETTINGS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT UNIQUE,
                $COLUMN_VALUE TEXT
            )
        """
		db.execSQL(createTable)
		insertDefaultValues(db)
	}

	fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		Log.d("UpgradeSettings", "Old: $oldVersion, New: $newVersion")
		db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
		onCreate(db)
	}

	fun insertDefaultValues(db: SQLiteDatabase) {
		val defaultValues = listOf(
			Pair(KEY_PRODUCT_VERSION, "0"),
			Pair(KEY_RETAILER_VERSION, "0"),
			Pair(KEY_ECONOMY_MODE, "1"),
			Pair(KEY_THEME_MODE, "2")
		)

		defaultValues.forEach { (key, value) ->
			val contentValues = ContentValues()
			contentValues.put(COLUMN_NAME, key)
			contentValues.put(COLUMN_VALUE, value)
			db.insert(TABLE_SETTINGS, null, contentValues)
		}
	}

	fun getTableVersion(db: SQLiteDatabase, tableName: String): Int {
		val cursor = db.query(
			TABLE_SETTINGS,
			arrayOf(COLUMN_VALUE),
			"$COLUMN_NAME=?",
			arrayOf("${tableName}_version"),
			null,
			null,
			null
		)

		return if (cursor.moveToFirst()) {
			cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VALUE))
		} else {
			0
		}.also {
			cursor.close()
		}
	}

	fun updateTableVersion(db: SQLiteDatabase, tableName: String, newVersion: Int) {
		val contentValues = ContentValues()
		contentValues.put(COLUMN_VALUE, newVersion.toString())
		db.update(TABLE_SETTINGS, contentValues, "$COLUMN_NAME=?", arrayOf("${tableName}_version"))
	}

	fun isEconomyMode(db: SQLiteDatabase): Boolean {
		val cursor = db.query(
			TABLE_SETTINGS, arrayOf(COLUMN_VALUE), "$COLUMN_NAME=?", arrayOf(KEY_ECONOMY_MODE),
			null, null, null
		)

		return if (cursor.moveToFirst()) {
			cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VALUE)).toInt() == 1
//			cursor.getColumnIndexOrThrow(COLUMN_VALUE).toInt() == 1
		} else {
			false
		}.also {
			cursor.close()
		}
	}

	fun setEconomyMode(db: SQLiteDatabase, enabled: Boolean) {
		val contentValues = ContentValues()
		contentValues.put(COLUMN_VALUE, if (enabled) "1" else "0")
		db.update(TABLE_SETTINGS, contentValues, "$COLUMN_NAME=?", arrayOf(KEY_ECONOMY_MODE))
	}

	fun getThemeMode(db: SQLiteDatabase): Int {
		val cursor = db.query(
			TABLE_SETTINGS, arrayOf(COLUMN_VALUE), "$COLUMN_NAME=?", arrayOf(KEY_THEME_MODE),
			null, null, null
		)

		return if (cursor.moveToFirst()) {
			cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VALUE))
		} else {
			2
		}.also {
			cursor.close()
		}
	}

	fun setThemeMode(db: SQLiteDatabase, mode: Int) {
		val contentValues = ContentValues()
		contentValues.put(COLUMN_VALUE, mode)
		db.update(TABLE_SETTINGS, contentValues, "$COLUMN_NAME=?", arrayOf(KEY_THEME_MODE))
	}
}
