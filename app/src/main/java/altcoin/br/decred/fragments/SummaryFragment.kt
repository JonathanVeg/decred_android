package altcoin.br.decred.fragments

import altcoin.br.decred.R
import altcoin.br.decred.utils.Bitcoin
import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.app.Fragment
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Response
import kotlinx.android.synthetic.main.bittrex.*
import kotlinx.android.synthetic.main.bleutrade.*
import kotlinx.android.synthetic.main.coin_market_cap.*
import kotlinx.android.synthetic.main.poloniex.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONObject

class SummaryFragment : Fragment() {
    private var running: Boolean = false

    override fun onStart() {
        super.onStart()

        loadCoinMarketCapData()

        loadPoloniexData()

        loadBittrexData()

        loadBleutradeData()

        running = true

        try {
            EventBus.getDefault().register(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onPause() {
        super.onPause()

        running = false
    }

    @Subscribe
    fun eventBusReceiver(obj: JSONObject) {
        try {
            if (obj.has("tag") && obj.getString("tag").equals("update", ignoreCase = true) && running) {
                loadCoinMarketCapData()

                loadPoloniexData()

                loadBittrexData()

                loadBleutradeData()

                Utils.log("update ::: SummaryFragment")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun loadCoinMarketCapData() {
        val url = "https://api.coinmarketcap.com/v1/ticker/decred/"

        val listener = Response.Listener<String> { response -> AtParseSummaryData(response).execute() }

        val internetRequests = InternetRequests()
        internetRequests.executeGet(url, listener)
    }

    private inner class AtParseSummaryData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal var usdPrice: String = ""
        internal var btcPrice: String = ""
        internal var usdVolume24h: String = ""
        internal var p24hChanges: String = ""
        internal var usdMarketCap: String = ""

        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val obj = JSONArray(response).getJSONObject(0)

                usdPrice = Utils.numberComplete(obj.getString("price_usd"), 4)
                btcPrice = Utils.numberComplete(obj.getString("price_btc"), 8)
                p24hChanges = Utils.numberComplete(obj.getString("percent_change_24h"), 2)
                usdVolume24h = Utils.numberComplete(obj.getString("24h_volume_usd"), 4)
                usdMarketCap = Utils.numberComplete(obj.getString("market_cap_usd"), 4)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            if (!running) return

            tvSummaryBtcPrice.text = btcPrice
            tvSummaryUsdPrice.text = usdPrice
            tvSummaryUsd24hVolume.text = usdVolume24h
            tvSummaryUsdMarketCap.text = usdMarketCap
            tvSummary24hChanges.text = String.format("%s%%", p24hChanges)

            if (java.lang.Double.parseDouble(p24hChanges) >= 0)
                tvSummary24hChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesUp))
            else
                tvSummary24hChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesDown))

            val listener = Response.Listener<String> { response ->
                try {
                    val obj = JSONObject(response)// .getJSONObject("ticker_24h").getJSONObject("total")

                    tvSummaryBrlPrice.text = Utils.numberComplete(java.lang.Double.parseDouble(btcPrice) * obj.getDouble("last"), 4)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Bitcoin.convertBtcToBrl(listener)

            // tvLastUpdate.setText(Utils.now());
        }
    }

    private fun loadBittrexData() {
        val url = "https://bittrex.com/api/v1.1/public/getmarketsummary?market=BTC-DCR"

        val listener = Response.Listener<String> { response -> AtParseBittrexData(response).execute() }

        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }

    private inner class AtParseBittrexData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal var last: String = ""
        internal var baseVolume: String = ""
        internal var ask: String = ""
        internal var bid: String = ""
        internal var changes: String? = null

        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                var obj = JSONObject(response)

                if (obj.getBoolean("success")) {
                    obj = obj.getJSONArray("result").getJSONObject(0)

                    last = Utils.numberComplete(obj.getString("Last"), 8)
                    baseVolume = Utils.numberComplete(obj.getString("BaseVolume"), 8)
                    ask = Utils.numberComplete(obj.getString("Ask"), 8)
                    bid = Utils.numberComplete(obj.getString("Bid"), 8)

                    // the api does not give the % changes, but we can calculate it using the prevDay and last values
                    val prev = obj.getDouble("PrevDay")

                    val c = (prev - java.lang.Double.parseDouble(last)) / prev * -100

                    changes = Utils.numberComplete("" + c, 2)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            if (!running) return

            tvBittrexLast.text = last
            tvBittrexBaseVolume.text = baseVolume
            tvBittrexBid.text = bid
            tvBittrexAsk.text = ask
            tvBittrexChanges.text = String.format("%s%%", changes)

            if (changes == null) changes = "0"

            if (java.lang.Double.parseDouble(changes) >= 0)
                tvBittrexChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesUp))
            else
                tvBittrexChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesDown))
        }
    }

    private fun loadPoloniexData() {
        val url = "https://poloniex.com/public?command=returnTicker"

        val listener = Response.Listener<String> { response -> AtParsePoloniexData(response).execute() }

        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }

    private inner class AtParsePoloniexData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal var last: String = ""
        internal var baseVolume: String = ""
        internal var ask: String = ""
        internal var bid: String = ""
        internal var changes: String = ""

        internal fun getSpecificSummary(response: String): JSONObject? {
            try {
                val coin = "DCR"

                val jObject = JSONObject(response)

                val keys = jObject.keys()

                var jsonObj: JSONObject

                while (keys.hasNext()) {
                    val key = keys.next() as String
                    if (jObject.get(key) is JSONObject) {
                        jsonObj = jObject.get(key) as JSONObject

                        if (key.startsWith("BTC_") && key.toLowerCase().contains(coin.toLowerCase())) {
                            return jsonObj

                        }
                    }
                }

                return null
            } catch (e: Exception) {
                e.printStackTrace()

                return null
            }

        }

        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val obj = getSpecificSummary(response)

                last = Utils.numberComplete(obj!!.getString("last"), 8)
                baseVolume = Utils.numberComplete(obj.getString("baseVolume"), 8)
                ask = Utils.numberComplete(obj.getString("lowestAsk"), 8)
                bid = Utils.numberComplete(obj.getString("highestBid"), 8)
                changes = Utils.numberComplete(obj.getDouble("percentChange") * 100, 2)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            if (!running) return

            tvPoloniexLast.text = last
            tvPoloniexBaseVolume.text = baseVolume
            tvPoloniexBid.text = bid
            tvPoloniexAsk.text = ask
            tvPoloniexChanges.text = String.format("%s%%", changes)

            if (java.lang.Double.parseDouble(changes) >= 0)
                tvPoloniexChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesUp))
            else
                tvPoloniexChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesDown))
        }
    }

    private fun loadBleutradeData() {
        val url = "https://bleutrade.com/api/v2/public/getmarketsummary?market=DCR_BTC"

        val listener = Response.Listener<String> { response -> AtParseBleutradeData(response).execute() }

        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }

    private inner class AtParseBleutradeData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal var last: String = ""
        internal var baseVolume: String = ""
        internal var ask: String = ""
        internal var bid: String = ""
        internal var changes: String? = null

        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val obj = JSONObject(response).getJSONArray("result").getJSONObject(0)

                last = Utils.numberComplete(obj.getString("Last"), 8)
                baseVolume = Utils.numberComplete(obj.getString("BaseVolume"), 8)
                ask = Utils.numberComplete(obj.getString("Ask"), 8)
                bid = Utils.numberComplete(obj.getString("Bid"), 8)

                val prev = obj.getDouble("PrevDay")

                val c = (prev - java.lang.Double.parseDouble(last)) / prev * -100

                changes = Utils.numberComplete("" + c, 2)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            if (!running) return

            tvBleutradeLast.text = last
            tvBleutradeBaseVolume.text = baseVolume
            tvBleutradeBid.text = bid
            tvBleutradeAsk.text = ask
            tvBleutradeChanges.text = String.format("%s%%", changes)

            if (changes != null && java.lang.Double.parseDouble(changes) >= 0)
                tvBleutradeChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesUp))
            else
                tvBleutradeChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesDown))
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_summary, container, false)
    }
}
