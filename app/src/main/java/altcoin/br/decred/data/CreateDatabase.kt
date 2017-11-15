package altcoin.br.decred.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class CreateDatabase(context: Context, dbName: String) : SQLiteOpenHelper(context, dbName, null, CURRENT_DB_VERSION) {

	override fun onCreate(db: SQLiteDatabase) {
		try {
			db.execSQL("CREATE TABLE if not exists bit_foo_coins(name string, visible_home string);")

			db.execSQL("CREATE TABLE if not exists wallets(" +
					"_id integer primary key autoincrement, " +
					"address varchar(100), " +
					"last_balance double, " +
					"balance varchar(20)" +
					")")

			db.execSQL("CREATE TABLE if not exists alerts(_id integer primary key autoincrement, " +
					"awhen integer, " +
					"value varchar(15), " +
					"created_at datetime," +
					"active boolean," +
					"poloniex boolean," +
					"bittrex boolean" +
					")")

		} catch (e: Exception) {
			e.printStackTrace()
		} finally {
			onUpgrade(db, 1, CURRENT_DB_VERSION)
		}
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

		if (newVersion >= 3)
			onUpgrade(db, newVersion - 2, newVersion - 1)

		when (newVersion) {
			2 -> try {
				db.execSQL("CREATE TABLE if not exists coin_widgets(_id integer primary key autoincrement, " +
						"widget_id integer," +
						"exchange varchar(30)" +
						")")
			} catch (ignored: Exception) {
			}

			3 -> try {
				db.execSQL("ALTER TABLE coin_widgets ADD COLUMN fiat varchar(5)")
			} catch (ignored: Exception) {
			}

		}
	}

	fun getDatabase(isRead: Boolean): SQLiteDatabase {
		return if (!isRead)
			this.writableDatabase
		else
			this.readableDatabase
	}

	companion object {

		private val CURRENT_DB_VERSION = 3
	}

}
