package altcoin.br.decred.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import altcoin.br.decred.application.MyApplication;

public class DBTools {

    private final CreateDatabase dbCreate;
    private SQLiteDatabase db;
    private Cursor cursor;
    private String _lastError = null;
    private String _lastSearch = "";

    private DBTools(Context context, String dbName) {
        if (context == null)
            context = MyApplication.getInstance().getApplicationContext();

        dbCreate = new CreateDatabase(context, dbName);

        if (context == null)
            Log.e("DBTools", "Constructor error: context is null. Any request will get an error.");
    }

    public DBTools(Context context) {
        this(context, "dcr_android_db");
    }

    public boolean exec(String sql) {
        try {
            open(false);

            db.execSQL(sql);

            return true;
        } catch (Exception e) {
            _lastError = e.toString();

            Log.e("DBTools Error", "Exec " + _lastError);
            Log.e("DBTools Error", "Exec " + sql);

            return false;
        } finally {
            close();
        }
    }

    private void open(boolean isRead) {
        try {
            db = dbCreate.getDatabase(isRead);
        } catch (Exception e) {
            _lastError = e.toString();

            Log.e("DBTools Error", "Open " + _lastError);
        }
    }

    public void close() {
        try {

            if (db != null)
                db.close();

        } catch (Exception e) {
            _lastError = e.toString();

            Log.e("DBTools Error", "Close " + _lastError);
        }
    }

    public boolean insert(String table, ContentValues values) {
        try {
            open(false);

            db.insert(table, null, values);

            close();

            return true;
        } catch (Exception e) {
            _lastError = e.toString();

            Log.e("DBTools Error", "Insert " + _lastError);

            return false;
        } finally {
            close();
        }
    }

    public int update(String table, ContentValues values, String whereClause) {
        return update(table, values, whereClause, null);
    }

    @SuppressWarnings("unused")
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        try {
            open(false);

            return db.update(table, values, whereClause, whereArgs);

        } catch (Exception e) {
            _lastError = e.toString();

            Log.e("DBTools Error", "Update " + _lastError);

            return 0;
        } finally {
            close();
        }
    }

    @SuppressWarnings("unused")
    public boolean remove(String table, String whereClause, String[] whereArgs) {
        try {
            open(false);

            db.delete(table, whereClause, whereArgs);

            return true;
        } catch (Exception e) {
            _lastError = e.toString();

            Log.e("DBTools Error", _lastError);

            return false;
        } finally {
            close();
        }
    }

    public int search(boolean isDistinct, String table, String[] columns) {
        try {
            open(true);

            cursor = db.query(isDistinct, table, columns, null, null, null,
                    null, null, null, null);

            return cursor.getCount();
        } catch (Exception e) {
            _lastError = e.toString();

            Log.e("DBTools Error", "Search " + _lastError);

            return -1;
        } finally {
            close();
        }
    }

    public int search(String sql) {
        try {
            open(true);

            if (cursor != null)
                cursor.close();

            cursor = db.rawQuery(sql, null);

            _lastSearch = sql;

            return cursor.getCount();
        } catch (Exception e) {
            _lastError = e.toString();

            Log.e("DBTools Error", "Search " + sql + " " + _lastError);

            return -1;
        } finally {
            close();
        }
    }

    @SuppressWarnings("unused")
    public int getRowLength(int row) {
        cursor.moveToPosition(row);

        return cursor.getColumnCount();
    }

    public String getData(int record, int column) {
        try {
            cursor.moveToPosition(record);

            return cursor.getString(column);
        } catch (Exception e) {
            _lastError = e.toString();

            Log.e("DBTools Error", "GetData " + _lastError);
            Log.e("DBTools Error", "GetData " + _lastSearch);

            if (cursor != null)
                cursor.close();

            return "";
        }
    }

    public String getData(int column) {
        try {
            cursor.moveToPosition(0);

            return cursor.getString(column);
        } catch (Exception e) {
            _lastError = e.toString();

            Log.e("DBTools Error", "GetData " + _lastError);
            Log.e("DBTools Error", "GetData " + _lastSearch);

            return "";
        }
    }

    @SuppressWarnings("unused")
    public String getError() {
        return _lastError;
    }

    public List<String> getRow(int index) {
        List<String> row = new ArrayList<>();

        for (int i = 0; i < getRowLength(i); i++)
            row.add(getData(index, i));

        return row;
    }
}
