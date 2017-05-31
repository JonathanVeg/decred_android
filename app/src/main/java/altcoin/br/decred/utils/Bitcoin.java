package altcoin.br.decred.utils;

import com.android.volley.Response;

public class Bitcoin {

    public static void convertBtcToBrl(Response.Listener<String> listener) {
        InternetRequests internetRequests = new InternetRequests();

        String url = "http://api.bitvalor.com/v1/ticker.json";

        internetRequests.executeGet(url, listener);
    }

    public static void convertBtcToUsd(Response.Listener<String> listener) {
        InternetRequests internetRequests = new InternetRequests();

        String url = "https://www.bitstamp.net/api/v2/ticker/btcusd/";

        internetRequests.executeGet(url, listener);
    }

}
