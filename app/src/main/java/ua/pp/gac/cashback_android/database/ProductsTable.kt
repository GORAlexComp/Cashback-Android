package ua.pp.gac.cashback_android.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

object ProductsTable {
	private const val TABLE_PRODUCTS = "products"
	private const val COLUMN_ID = "id"
	private const val COLUMN_EAN = "ean"
	private const val COLUMN_BRAND = "brand"
	private const val COLUMN_NAME = "name"
	private const val COLUMN_MANUFACTURE = "manufacture"
	private const val COLUMN_UPDATED_AT = "updatedAt"

	data class Product(
		val id: Int?,
		val ean: Long?,
		val brand: String?,
		val name: String?,
		val manufacture: String?,
		val updatedAt: String?,
	)

	fun onCreate(db: SQLiteDatabase) {
		val createProductsTable = """ CREATE TABLE $TABLE_PRODUCTS (
			$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_EAN INTEGER,
            $COLUMN_BRAND TEXT,
            $COLUMN_NAME TEXT,
            $COLUMN_MANUFACTURE TEXT,
            $COLUMN_UPDATED_AT TEXT)
        """

		db.execSQL(createProductsTable)

		val indexationProductsEan = "CREATE INDEX idx_products_ean ON $TABLE_PRODUCTS ($COLUMN_EAN)"
		val indexationProductsName =
			"CREATE INDEX idx_products_name ON $TABLE_PRODUCTS ($COLUMN_NAME)"

		db.execSQL(indexationProductsEan)
		db.execSQL(indexationProductsName)
	}

	fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		Log.d("UpgradeProducts", "Old: $oldVersion, New: $newVersion")
		db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
		onCreate(db)
	}

	fun getProduct(db: SQLiteDatabase, ean: Long): Product? {
		val query = "SELECT * FROM $TABLE_PRODUCTS WHERE ean = ?"
		val cursor = db.rawQuery(query, arrayOf(ean.toString()))

		var product: Product? = null
		if (cursor.moveToFirst()) {
			val idC = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
			val eanC = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EAN))
			val brandC = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND))
			val nameC = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
			val manufactureC = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURE))
			val updatedAtC = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))

			product = Product(idC, eanC, brandC, nameC, manufactureC, updatedAtC)
		}
		cursor.close()

		return product
	}

	fun getProductsCount(db: SQLiteDatabase): Int {
		val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PRODUCTS", null)
		var count = 0
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0)
		}
		cursor.close()

		return count
	}

	fun getSearchProducts(
		db: SQLiteDatabase,
		query: String,
		offset: Int,
		limit: Int,
	): List<Product> {
		val productsList = mutableListOf<Product>()

		val cursor: Cursor = if (query.isEmpty()) {
			db.rawQuery(
				"SELECT $COLUMN_EAN, $COLUMN_BRAND, $COLUMN_NAME, $COLUMN_MANUFACTURE FROM $TABLE_PRODUCTS LIMIT ? OFFSET ?",
				arrayOf(limit.toString(), offset.toString())
			)
		} else {
			db.rawQuery(
				"SELECT $COLUMN_EAN, $COLUMN_BRAND, $COLUMN_NAME, $COLUMN_MANUFACTURE FROM $TABLE_PRODUCTS WHERE ($COLUMN_NAME LIKE ? OR $COLUMN_EAN LIKE ?) LIMIT ? OFFSET ?",
				arrayOf("%$query%", "%$query%", limit.toString(), offset.toString())
			)
		}

		if (cursor.moveToFirst()) {
			do {
				val product = Product(
					null,
					cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EAN)),
					cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND)),
					cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
					cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURE)),
					null
				)
				productsList.add(product)
			} while (cursor.moveToNext())
		}
		cursor.close()

		return productsList
	}
}
