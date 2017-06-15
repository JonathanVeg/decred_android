package altcoin.br.decred.adapter.bitcoin;

import android.content.Context;

import org.json.JSONObject;

import java.util.ArrayList;

import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class FiatPriceFiat extends FiatCoinPrice {
    private final String fiat;

    private final Context context;

    private static String usdUrl() {
        return "https://www.bitstamp.net/api/v2/ticker/btcusd/";
    }

    private static String eurUrl() {
        return "https://www.bitstamp.net/api/v2/ticker/btceur/";
    }

    private static String audUrl() {
        return "https://api.btcmarkets.net/market/BTC/AUD/tick";
    }

    private static String brlUrl() {
        // return "https://api.blinktrade.com/api/v1/BRL/ticker";
        return "https://www.mercadobitcoin.net/api/v2/ticker/";
    }

    private static String gbpUrl() {
        return "https://webapi.coinfloor.co.uk:8090/bist/XBT/GBP/ticker/";
    }

    private static String cadUrl() {
        return "https://api.cbix.ca/v1/index";
    }

    private static String yenUrl() {
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
