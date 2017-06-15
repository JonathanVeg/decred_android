package altcoin.br.decred.model;

import android.content.ContentValues;
import android.content.Context;

import altcoin.br.decred.data.DBTools;

public class Alert {
    public static final int GREATER = 1;
    public static final int LOWER = 2;

    private final Context context;

    private int id;
    private int when;
    private String value;
    private String createdAt; // timestamp
    private boolean active;
    private boolean bittrex;
    private boolean poloniex;

    public Alert(Context context) {
        this.context = context;

        setId("0");
        setWhen(0);
        setValue("0");
        setCreatedAt(String.valueOf(System.currentTimeMillis() / 1000));
        setActive(true);
        setPoloniex(true);
        setBittrex(true);
    }

    public boolean save() {
        DBTools db = new DBTools(context);

        try {
            ContentValues cv = new ContentValues();

            cv.put("AWHEN", getWhen());
            cv.put("VALUE", getValue());
            cv.put("CREATED_AT", getCreatedAt());
            cv.put("ACTIVE", isActive());
            cv.put("BITTREX", isBittrex());
            cv.put("POLONIEX", isPoloniex());

            return db.update("alerts", cv, "_id = " + getId(), null) != 0 || db.insert("alerts", cv);

        } catch (Exception e) {
            e.printStackTrace();

            return false;
        } finally {
            db.close();
        }
    }

    public int getWhen() {
        return when;
    }

    public void setWhen(int when) {
        this.when = when;
    }

    public void setWhen(String when) {
        this.when = Integer.parseInt(when);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Integer.parseInt(id);
    }

    public String getWhenText() {
        if (getWhen() == GREATER)
            return "greater than";

        return "lower than";
    }

    public double getValueDouble() {
        try {
            return Double.parseDouble(getValue());
        } catch (Exception e) {
            e.printStackTrace();

            return -1;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBittrex() {
        return bittrex;
    }

    public void setBittrex(boolean bittrex) {
        this.bittrex = bittrex;
    }

    public boolean isPoloniex() {
        return poloniex;
    }

    public void setPoloniex(boolean poloniex) {
        this.poloniex = poloniex;
    }
}
