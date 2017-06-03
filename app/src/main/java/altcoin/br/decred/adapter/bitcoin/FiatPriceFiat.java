package altcoin.br.decred.adapter.bitcoin;

import android.content.Context;

import org.json.JSONObject;

import java.util.ArrayList;

import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class FiatPriceFiat extends FiatCoinPrice {
    private String fiat;

    private Context context;

    static String usdUrl() {
        return "https://www.bitstamp.net/api/v2/ticker/btcusd/";
    }

    static String eurUrl() {
        return "https://www.bitstamp.net/api/v2/ticker/btceur/";
    }

    static String audUrl() {
        return "https://api.btcmarkets.net/market/BTC/AUD/tick";
    }

    static String brlUrl() {
        // return "https://api.blinktrade.com/api/v1/BRL/ticker";
        return "https://www.mercadobitcoin.net/api/v2/ticker/";
    }

    static String gbpUrl() {
        return "https://webapi.coinfloor.co.uk:8090/bist/XBT/GBP/ticker/";
    }

    static String cadUrl() {
        return "https://api.cbix.ca/v1/index";
    }

    static String yenUrl() {
        return "https://api.bitflyer.jp/v1/getticker";
    }

    protected FiatPriceFiat(Context context, String fiat) {
        this.context = context;

        fiat = fiat.toLowerCase();

        this.fiat = fiat;

        InternetRequests internetRequests = new InternetRequests();

        String url = "";

        if (fiat.equalsIgnoreCase("brl"))
            url = brlUrl();
        else if (fiat.equalsIgnoreCase("usd"))
            url = usdUrl();
        else if (fiat.equalsIgnoreCase("eur"))
            url = eurUrl();
        else if (fiat.equalsIgnoreCase("aud"))
            url = audUrl();
        else if (fiat.equalsIgnoreCase("gbp"))
            url = gbpUrl();
        else if (fiat.equalsIgnoreCase("cad"))
            url = cadUrl();
        else if (fiat.equalsIgnoreCase("yen"))
            url = yenUrl();

        internetRequests.executeGet(url, this);
    }

    public static ArrayList<String> availableFiats() {
        ArrayList<String> available = new ArrayList<>();

        available.add("USD");
        available.add("BRL");
        available.add("EUR");
        available.add("AUD");
        available.add("GBP");
        available.add("CAD");
        available.add("YEN");

        return available;
    }

    public static String getMainFiat1(Context context) {
        try {
            return Utils.readPreference(context, "fiatCurrency1", "USD");
        } catch (Exception e) {
            e.printStackTrace();

            return "USD";
        }
    }

    public static String getMainFiat2(Context context) {
        try {
            return Utils.readPreference(context, "fiatCurrency2", "EUR");
        } catch (Exception e) {
            e.printStackTrace();

            return "EUR";
        }
    }

    public void onValueLoaded() {

    }

    public void onResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);

            if (fiat.equalsIgnoreCase("eur") || fiat.equalsIgnoreCase("usd") || fiat.equalsIgnoreCase("gbp")) {
                setPrice(obj.getDouble("last"));
                setHigh(obj.getDouble("high"));
                setLow(obj.getDouble("low"));
            } else if (fiat.equalsIgnoreCase("aud")) {
                setPrice(obj.getDouble("lastPrice"));
                setHigh(obj.getDouble("bestAsk"));
                setLow(obj.getDouble("bestBid"));
            } else if (fiat.equalsIgnoreCase("brl")) {
                obj = obj.getJSONObject("ticker");

                setPrice(obj.getDouble("last"));
                setHigh(obj.getDouble("high"));
                setLow(obj.getDouble("low"));
            } else if (fiat.equalsIgnoreCase("cad")) {
                obj = obj.getJSONObject("index");

                setPrice(obj.getDouble("value"));
                setHigh(obj.getDouble("high_24hour"));
                setLow(obj.getDouble("low_24hour"));
            } else if (fiat.equalsIgnoreCase("yen")) {
                setPrice(obj.getDouble("ltp"));
                setHigh(obj.getDouble("best_ask"));
                setLow(obj.getDouble("best_bid"));
            }

            Utils.writePreference(context, "last" + fiat + "price", "" + getPrice());
            Utils.writePreference(context, "last" + fiat + "low", "" + getLow());
            Utils.writePreference(context, "last" + fiat + "high", "" + getHigh());

            Utils.writePreference(context, "last" + fiat + "read", Utils.timestampLong());

            onValueLoaded();
        } catch (Exception e) {

            setPrice(-1);
            setHigh(-1);
            setLow(-1);

            Utils.writePreference(context, "last" + fiat + "read", 0L);

            onValueLoaded();

            e.printStackTrace();
        }
    }

}
