package altcoin.br.decred.utils.exchanges

import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.os.AsyncTask
import com.android.volley.Response
import org.json.JSONObject

open class Bleutrade(val coin: String, val market: String) : Exchange() {
    override fun onValueLoaded() {
    }
    
    override fun loadData() {
        // val url = "https://bleutrade.com/api/v2/public/getmarketsummaries"
        val url = "https://bleutrade.com/api/v2/public/getmarketsummary?market=${coin.toUpperCase()}_${market.toUpperCase()}"
        
        val listener = Response.Listener<String> { response -> AtParseBleutradeData(response).execute() }
        
        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }
    
    private inner class AtParseBleutradeData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val arr = JSONObject(response).getJSONArray("result")
                
                var obj: JSONObject? = null
                
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    
                    if (item.getString("MarketName").toLowerCase() == "${coin}_$market")
                        obj = item
                }
                
                if (obj != null) {
                    last = Utils.numberComplete(obj.getString("Last"), 8)
                    baseVolume = Utils.numberComplete(obj.getString("BaseVolume"), 8)
                    coinVolume = Utils.numberComplete(obj.getString("Volume"), 8)
                    ask = Utils.numberComplete(obj.getString("Ask"), 8)
                    bid = Utils.numberComplete(obj.getString("Bid"), 8)
                    high = Utils.numberComplete(obj.getString("High"), 8)
                    low = Utils.numberComplete(obj.getString("Low"), 8)
                    
                    val prev = obj.getDouble("PrevDay")
                    
                    val c = (prev - java.lang.Double.parseDouble(last)) / prev * -100
                    
                    changes = Utils.numberComplete(c, 2)
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
