package altcoin.br.decred.utils.exchanges

import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.os.AsyncTask
import com.android.volley.Response
import org.json.JSONObject

open class Bleutrade : Exchange() {
    override fun onValueLoaded() {
    }
    
    override fun loadData() {
        val url = "https://bleutrade.com/api/v2/public/getmarketsummaries"
        
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
                    
                    if (item.getString("MarketName").toLowerCase() == "dcr_btc")
                        obj = item
                }
                
                if (obj != null) {
                    last = Utils.numberComplete(obj.getString("Last"), 8)
                    baseVolume = Utils.numberComplete(obj.getString("BaseVolume"), 8)
                    ask = Utils.numberComplete(obj.getString("Ask"), 8)
                    bid = Utils.numberComplete(obj.getString("Bid"), 8)
                    
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
