package altcoin.br.decred.utils;

import com.android.volley.Response;

public class Bitcoin {

    public static void convertBtcToBrl(Response.Listener<String> listener) {
        InternetRequests internetRequests = new InternetRequests();

        String url = "https://api.blinktrade.com/api/v1/BRL/ticker";

        internetRequests.executeGet(url, listener);
    }

}
