package altcoin.br.decred.data

import altcoin.br.decred.application.MyApplication
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class DBTools private constructor(var context: Context?, dbName: String) {
    
    private val dbCreate: CreateDatabase
    private var db: SQLiteDatabase? = null
    private var cursor: Cursor? = null
    private var error: String = ""
    private var _lastSearch = ""
    
    init {
        if (context == null)
            context = MyApplication.getInstance().applicationContext
        
        dbCreate = CreateDatabase(context!!, dbName)
    }
    
    constructor(context: Context) : this(context, "dcr_android_db")
    
    fun exec(sql: String): Boolean {
        try {
            open(false)
            
            db!!.execSQL(sql)
            
            return true
        } catch (e: Exception) {
            error = e.toString()
            
            Log.e("DBTools Error", "Exec $error")
            Log.e("DBTools Error", "Exec $sql")
            
            return false
        } finally {
            close()
        }
    }
    
    private fun open(isRead: Boolean) {
        try {
            db = dbCreate.getDatabase(isRead)
        } catch (e: Exception) {
            error = e.toString()
            
            Log.e("DBTools Error", "Open $error")
        }
    }
    
    fun close() {
        try {
            
            if (db != null)
                db!!.close()
        } catch (e: Exception) {
            error = e.toString()
            
            Log.e("DBTools Error", "Close $error")
        }
    }
    
    fun insert(table: String, values: ContentValues): Boolean {
        try {
            open(false)
            
            db!!.insert(table, null, values)
            
            close()
            
            return true
        } catch (e: Exception) {
            error = e.toString()
            
            Log.e("DBTools Error", "Insert $error")
            
            return false
        } finally {
            close()
        }
    }
    
    fun update(table: String, values: ContentValues, whereClause: String, whereArgs: Array<String>?): Int {
        return try {
            open(false)
            
            db!!.update(table, values, whereClause, whereArgs)
        } catch (e: Exception) {
            error = e.toString()
            
            Log.e("DBTools Error", "Update $error")
            
            0
        } finally {
            close()
        }
    }
    
    fun search(sql: String): Int {
        try {
            open(true)
            
            if (cursor != null)
                cursor!!.close()
            
            cursor = db!!.rawQuery(sql, null)
            
            _lastSearch = sql
            
            return cursor!!.count
        } catch (e: Exception) {
            error = e.toString()
            
            Log.e("DBTools Error", "Search $sql $error")
            
            return -1
        } finally {
            close()
        }
    }
    
    fun getData(record: Int, column: Int): String {
        return try {
            cursor!!.moveToPosition(record)
            
            cursor!!.getString(column)
        } catch (e: Exception) {
            error = e.toString()
            
            Log.e("DBTools Error", "GetData $error")
            Log.e("DBTools Error", "GetData $_lastSearch")
            
            if (cursor != null)
                cursor!!.close()
            
            ""
        }
    }
    
    fun getData(column: Int): String {
        return try {
            cursor!!.moveToPosition(0)
            
            cursor!!.getString(column)
        } catch (e: Exception) {
            error = e.toString()
            
            Log.e("DBTools Error", "GetData $error")
            Log.e("DBTools Error", "GetData $_lastSearch")
            
            ""
        }
    }
}
