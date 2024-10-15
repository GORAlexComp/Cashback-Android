package ua.pp.gac.cashback_android

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import org.json.JSONObject
import ua.pp.gac.cashback_android.database.DatabaseHelper
import ua.pp.gac.cashback_android.database.SettingsTable
import java.io.File

@Suppress("DEPRECATION")
class UpdatesData(
	private val context: Context,
	private val dbHelper: SQLiteDatabase,
) {
	//	private var buttonOk: MaterialButton =
	private val storage = Firebase.storage
	private val localCacheDir = context.cacheDir

	fun startUpdate() {
		checkNetworkBeforeUpdate()
	}

	private fun checkNetworkBeforeUpdate() {
		val connectivityManager =
			context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val networkInfo = connectivityManager.activeNetworkInfo
		val isEconomyMode = SettingsTable.isEconomyMode(dbHelper)

		if (networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_MOBILE && isEconomyMode) {
			AlertDialog.Builder(context)
				.setTitle(R.string.economy_mode_title)
				.setMessage(R.string.economy_mode_text)
				.setPositiveButton(R.string.yes_button) { _, _ ->
					checkForUpdates()
				}
				.setNegativeButton(R.string.cancel_button, null)
				.show()
		} else {
			checkForUpdates()
		}
	}

	private fun checkForUpdates() {
		val storageRef = storage.reference.child("versions.json")

		storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
			val jsonString = String(bytes)
			val jsonObject = JSONObject(jsonString)

			val productVersionServer = jsonObject.getInt("products")
			val retailerVersionServer = jsonObject.getInt("retailers")

			val productVersionLocal = getLocalVersion("products")
			val retailerVersionLocal = getLocalVersion("retailers")

			val tablesToUpdate = mutableListOf<String>()
			if (productVersionServer > productVersionLocal) tablesToUpdate.add("products")
			if (retailerVersionServer > retailerVersionLocal) tablesToUpdate.add("retailers")

			if (tablesToUpdate.isNotEmpty()) {
				showUpdateDialog(tablesToUpdate, productVersionServer, retailerVersionServer)
			} else {
				Toast.makeText(context, R.string.no_find_updates, Toast.LENGTH_SHORT).show()
			}
		}.addOnFailureListener {
			Log.e("UpdateData", "Failed to fetch versions: ${it.message}")
		}
	}

	private fun getLocalVersion(table: String): Int {
		return SettingsTable.getTableVersion(dbHelper, table)
	}

	private fun showUpdateDialog(
		tablesToUpdate: List<String>,
		productVersionServer: Int,
		retailerVersionServer: Int,
	) {
		val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_tables, null)
		val dialog = AlertDialog.Builder(context)
			.setView(dialogView)
			.setCancelable(false)
			.create()

		val buttonOk = dialog.findViewById<MaterialButton>(R.id.ok_button_dialog)
		buttonOk?.isClickable = false

		buttonOk?.setOnClickListener {
			dialog.dismiss()
		}

		val updateIcons = listOf(
			dialogView.findViewById<ImageView>(R.id.product_update_icon),
			dialogView.findViewById<ImageView>(R.id.retailer_update_icon)
		)
		val updateTexts = listOf(
			dialogView.findViewById<LinearLayout>(R.id.product_update_block),
			dialogView.findViewById<LinearLayout>(R.id.retailer_update_block)
		)

		val rotate = RotateAnimation(
			0f, 360f,
			Animation.RELATIVE_TO_SELF, 0.5f,
			Animation.RELATIVE_TO_SELF, 0.5f
		)
		rotate.duration = 1000
		rotate.repeatCount = Animation.INFINITE

		if (!tablesToUpdate.contains("products")) {
			updateTexts[0].visibility = View.GONE
			updateIcons[0].startAnimation(rotate)
		}
		if (!tablesToUpdate.contains("retailers")) {
			updateTexts[1].visibility = View.GONE
			updateIcons[1].startAnimation(rotate)
		}

		dialog.show()

		for (table in tablesToUpdate) {
			updateTable(table) { success ->
				val index = if (table == "products") 0 else 1
				if (success) {
					updateIcons[index].clearAnimation()
					updateIcons[index].setImageResource(R.drawable.ic_update_done)
					updateLocalVersion(
						table,
						if (table == "products") productVersionServer else retailerVersionServer
					)
				} else {
					updateIcons[index].clearAnimation()
					updateIcons[index].setImageResource(R.drawable.ic_check_cancel)
				}
//				if (tablesToUpdate.all { checkIfUpdated(it) }) {
				if (tablesToUpdate.all { true }) {
					dialog.setCancelable(true)
					buttonOk?.isClickable = true
					val intent = Intent("ua.pp.gac.cashback_android.UPDATE_TABLES_COUNT")
					sendBroadcast(intent)
					MoreFunctions(context).updateCountWidget(context)
				}
			}
		}
	}

	private fun updateTable(table: String, callback: (Boolean) -> Unit) {
		val storageRef = storage.reference.child("$table.sql")
		val localFile = File(localCacheDir, "$table.sql")

		storageRef.getFile(localFile).addOnSuccessListener {
			executeSqlUpdate(localFile)
			localFile.delete()
			callback(true)
		}.addOnFailureListener {
			callback(false)
		}
	}

	private fun executeSqlUpdate(file: File) {
		val db = DatabaseHelper(context)
		db.executeSQLFile(dbHelper, file)
	}

	private fun updateLocalVersion(table: String, newVersion: Int) {
		SettingsTable.updateTableVersion(dbHelper, table, newVersion)
	}

//	private fun checkIfUpdated(table: String): Boolean { return true }

	fun sendBroadcast(intent: Intent) {
		context.sendBroadcast(intent)
	}
}
