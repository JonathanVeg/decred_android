package altcoin.br.decred.utils.exchanges

import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.os.AsyncTask
import com.android.volley.Response
import org.json.JSONObject

open class Huobi(val coin: String, val market: String) : Exchange() {
    override fun onValueLoaded() {
    }
    
    override fun loadData() {
        val url = "https://api.huobi.pro/market/detail/merged?symbol=$coin$market"
        
        val listener = Response.Listener<String> { response -> AtParseProfitfyData(response).execute() }
        
        val internetRequests = InternetRequests()
        internetRequests.executeGet(url, listener)
    }
    
    private inner class AtParseProfitfyData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                /*
                {"status":"ok","ch":"market.dcrbtc.detail.merged","ts":1529097794207,"tick":{"amount":19585.513609856718188402,"open":0.012001000000000000,"close":0.014149000000000000,"high":0.016800000000000000,"id":9553661447,"count":10354,"low":0.012001000000000000,"version":9553661447,"ask":[0.014155000000000000,1.409200000000000000],"vol":287.515633377438352316332989000000000000,"bid":[0.014143000000000000,1.409200000000000000]}}
                */
                
                val obj = JSONObject(response)
                
                if (obj.getString("status").equals(ignoreCase = true, other = "ok")) {
                    val data = obj.getJSONObject("tick")
                    
                    last = Utils.numberComplete(data.getJSONArray("ask").getString(0), 8)
                    baseVolume = Utils.numberComplete(data.getString("vol"), 8)
                    coinVolume = Utils.numberComplete(data.getString("amount"), 8)
                    ask = Utils.numberComplete(data.getJSONArray("ask").getString(0), 8)
                    bid = Utils.numberComplete(data.getJSONArray("bid").getString(0), 8)
                    high = Utils.numberComplete(data.getString("high"), 8)
                    low = Utils.numberComplete(data.getString("low"), 8)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return null
        }
        
        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            
            onValueLoaded()
        }
    }
}

