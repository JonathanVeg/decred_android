package altcoin.br.decred.utils.exchanges

import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.os.AsyncTask
import com.android.volley.Response
import org.json.JSONObject

open class Poloniex : Exchange() {
    override fun onValueLoaded() {
    }
    
    override fun loadData() {
        val url = "https://poloniex.com/public?command=returnTicker"
        
        val listener = Response.Listener<String> { response -> AtParsePoloniexData(response).execute() }
        
        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }
    
    private inner class AtParsePoloniexData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
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
            
            onValueLoaded()
        }
    }
}
