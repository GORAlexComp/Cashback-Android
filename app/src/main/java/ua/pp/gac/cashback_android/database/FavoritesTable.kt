package ua.pp.gac.cashback_android.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import ua.pp.gac.cashback_android.database.ProductsTable.Product
import kotlin.apply

object FavoritesTable {
	private const val TABLE_FAVORITES = "favorites"
	private const val COLUMN_ID = "id"
	private const val COLUMN_NAME = "name"
	private const val COLUMN_EAN = "ean"
	private const val COLUMN_BRAND = "brand"

	data class Favorite(
		val id: Int?,
		val name: String?,
		val ean: Long?,
		val brand: String?,
	)

	fun onCreate(db: SQLiteDatabase) {
		val createFavoritesTable = """ CREATE TABLE $TABLE_FAVORITES (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT,
            $COLUMN_EAN INTEGER UNIQUE,
            $COLUMN_BRAND TEXT)
        """
		db.execSQL(createFavoritesTable)

		val indexationFavoritesName =
			"CREATE INDEX idx_favorites_name ON $TABLE_FAVORITES ($COLUMN_NAME)"
		val indexationFavoritesBrand =
			"CREATE INDEX idx_favorites_brand ON $TABLE_FAVORITES ($COLUMN_BRAND)"
		val indexationFavoritesEan =
			"CREATE INDEX idx_favorites_ean ON $TABLE_FAVORITES ($COLUMN_EAN)"

		db.execSQL(indexationFavoritesName)
		db.execSQL(indexationFavoritesBrand)
		db.execSQL(indexationFavoritesEan)
	}

	fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		Log.d("UpgradeFavorites", "Old: $oldVersion, New: $newVersion")
		db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
		onCreate(db)
	}

	fun isFavorite(db: SQLiteDatabase, ean: Long?): Boolean {
		val query = "SELECT * FROM $TABLE_FAVORITES WHERE $COLUMN_EAN = ?"
		val cursor = db.rawQuery(query, arrayOf(ean.toString()))

		val exists = cursor.moveToFirst()
		cursor.close()

		return exists
	}

	fun getFavorites(db: SQLiteDatabase, offset: Int, limit: Int): List<Favorite> {
		val favoritesList = mutableListOf<Favorite>()

		val cursor: Cursor = db.rawQuery(
			"SELECT $COLUMN_NAME, $COLUMN_EAN, $COLUMN_BRAND FROM $TABLE_FAVORITES LIMIT ? OFFSET ?",
			arrayOf(limit.toString(), offset.toString())
		)

		if (cursor.moveToFirst()) {
			do {
				val favorite = Favorite(
					null,
					name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
					ean = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EAN)),
					brand = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND))
				)
				favoritesList.add(favorite)
			} while (cursor.moveToNext())
		}
		cursor.close()

		return favoritesList
	}

	fun setFavorite(db: SQLiteDatabase, product: Product): Boolean {
		val contentValues = ContentValues().apply {
			put(COLUMN_EAN, product.ean.toString())
			if (product.brand != null) put(COLUMN_BRAND, product.brand)
			else put(COLUMN_BRAND, product.manufacture)
			put(COLUMN_NAME, product.name)
		}
		var result = db.insert(TABLE_FAVORITES, null, contentValues)

		return result != -1L
	}

	fun getFavoritesCount(db: SQLiteDatabase): Int {
		val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_FAVORITES", null)
		var count = 0
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0)
		}
		cursor.close()

		return count
	}

	fun delFavorite(db: SQLiteDatabase, ean: Long?): Boolean {
		val result = db.delete(TABLE_FAVORITES, "$COLUMN_EAN = ?", arrayOf(ean.toString()))

		return result > 0
	}
}
