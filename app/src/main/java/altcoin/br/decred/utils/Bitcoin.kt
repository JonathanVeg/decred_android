package altcoin.br.decred.utils

import com.android.volley.Response

object Bitcoin {
    fun convertBtcToBrl(listener: Response.Listener<String>) {
        val internetRequests = InternetRequests()

        val url = "https://api.blinktrade.com/api/v1/BRL/ticker"

        internetRequests.executeGet(url, listener)
    }

    fun convertBtcToUsd(listener: Response.Listener<String>) {
        val internetRequests = InternetRequests()

        val url = "https://www.bitstamp.net/api/v2/ticker/btcusd/"

        internetRequests.executeGet(url, listener)
    }

}
