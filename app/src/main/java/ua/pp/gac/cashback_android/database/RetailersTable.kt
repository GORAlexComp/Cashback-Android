package ua.pp.gac.cashback_android.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

object RetailersTable {
	private const val TABLE_RETAILERS = "retailers"
	private const val COLUMN_ID = "id"
	private const val COLUMN_BRAND = "brand"
	private const val COLUMN_MANUFACTURE = "manufacture"
	private const val COLUMN_ADDRESS = "address"
	private const val COLUMN_EDRPOU = "edrpou"
	private const val COLUMN_RNOKPP = "rnokpp"
	private const val COLUMN_UPDATED_AT = "updatedAt"

	data class Retailer(
		val id: Int?,
		val brand: String?,
		val manufacture: String?,
		val address: String,
		val edrpou: Long?,
		val rnokpp: Long?,
		val updatedAt: String?,
	)

	fun onCreate(db: SQLiteDatabase) {
		val createRetailersTable = """ CREATE TABLE $TABLE_RETAILERS (
			$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_BRAND TEXT,
            $COLUMN_MANUFACTURE TEXT,
            $COLUMN_ADDRESS TEXT,
            $COLUMN_EDRPOU INTEGER,
            $COLUMN_RNOKPP INTEGER,
            $COLUMN_UPDATED_AT TEXT)
        """

		db.execSQL(createRetailersTable)

		val indexationRetailersBrand =
			"CREATE INDEX idx_retailers_brand ON $TABLE_RETAILERS ($COLUMN_BRAND)"
		val indexationRetailersManufacture =
			"CREATE INDEX idx_retailers_manufacture ON $TABLE_RETAILERS ($COLUMN_MANUFACTURE)"
		val indexationRetailersAddress =
			"CREATE INDEX idx_retailers_address ON $TABLE_RETAILERS ($COLUMN_ADDRESS)"

		db.execSQL(indexationRetailersBrand)
		db.execSQL(indexationRetailersManufacture)
		db.execSQL(indexationRetailersAddress)
	}

	fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		Log.d("UpgradeRetailers", "Old: $oldVersion, New: $newVersion")
		db.execSQL("DROP TABLE IF EXISTS $TABLE_RETAILERS")
		onCreate(db)
	}

	fun getRetailersCount(db: SQLiteDatabase): Int {
		val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_RETAILERS", null)
		var count = 0
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0)
		}
		cursor.close()

		return count
	}

	fun getSearchRetailers(
		db: SQLiteDatabase,
		brandQuery: String,
		addressQuery: String,
		offset: Int,
		limit: Int,
	): List<Retailer> {
		val retailersList = mutableListOf<Retailer>()
		val cursor: Cursor

		if (brandQuery.isEmpty() && addressQuery.isEmpty()) {
			cursor = db.rawQuery(
				"SELECT $COLUMN_BRAND, $COLUMN_MANUFACTURE, $COLUMN_ADDRESS FROM $TABLE_RETAILERS LIMIT ? OFFSET ?",
				arrayOf(limit.toString(), offset.toString())
			)
		} else {
			val addressParts = addressQuery.split(" ").filter { it.isNotEmpty() }
			val addressLikeClauses = if (addressParts.isNotEmpty()) {
				addressParts.joinToString(separator = " AND ") { "$COLUMN_ADDRESS LIKE ?" }
			} else {
				"1=1"
			}

			val addressArgs = addressParts.map { "%$it%" }.toTypedArray()

			val query = """
				SELECT $COLUMN_BRAND, $COLUMN_MANUFACTURE, $COLUMN_ADDRESS FROM $TABLE_RETAILERS
				WHERE ($COLUMN_BRAND LIKE ? OR $COLUMN_MANUFACTURE LIKE ?)
				AND ($addressLikeClauses) LIMIT ? OFFSET ?
			"""

			val args = arrayOf("%$brandQuery%", "%$brandQuery%") + addressArgs + arrayOf(
				limit.toString(), offset.toString()
			)

			cursor = db.rawQuery(query, args)
		}

		if (cursor.moveToFirst()) {
			do {
				val retailer = Retailer(
					null,
					cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND)),
					cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURE)),
					cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
					null,
					null,
					null
				)
				retailersList.add(retailer)
			} while (cursor.moveToNext())
		}
		cursor.close()

		return retailersList
	}
}
