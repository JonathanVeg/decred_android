package altcoin.br.decred.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class CreateDatabase extends SQLiteOpenHelper {

    private static int CURRENT_DB_VERSION = 1;

    CreateDatabase(Context context, String dbName) {
        super(context, dbName, null, CURRENT_DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE if not exists bit_foo_coins(name string, visible_home string);");

            db.execSQL("CREATE TABLE if not exists wallets(" +
                    "_id integer primary key autoincrement, " +
                    "address varchar(100), " +
                    "last_balance double, " +
                    "balance varchar(20)" +
                    ")");

            db.execSQL("CREATE TABLE if not exists alerts(_id integer primary key autoincrement, " +
                    "awhen integer, " +
                    "value varchar(15), " +
                    "created_at datetime," +
                    "active boolean," +
                    "poloniex boolean," +
                    "bittrex boolean" +
                    ")");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            onUpgrade(db, 1, CURRENT_DB_VERSION);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion >= 3)
            onUpgrade(db, newVersion - 2, newVersion - 1);

        switch (newVersion) {
            case 1:
                break;
        }
    }

    SQLiteDatabase getDatabase(boolean isRead) {
        if (!isRead)
            return this.getWritableDatabase();
        else
            return this.getReadableDatabase();
    }

}
