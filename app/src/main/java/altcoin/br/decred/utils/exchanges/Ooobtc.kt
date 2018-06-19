package altcoin.br.decred.utils.exchanges

import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.os.AsyncTask
import com.android.volley.Response
import org.json.JSONObject

open class Ooobtc(val coin: String, val market: String) : Exchange() {
    override fun onValueLoaded() {
    }
    
    override fun loadData() {
        val url = "https://api.ooobtc.com/open/getticker?kv=${coin}_$market"
        
        val listener = Response.Listener<String> { response -> AtParseOoobtcData(response).execute() }
        
        val internetRequests = InternetRequests()
        internetRequests.executeGet(url, listener)
    }
    
    private inner class AtParseOoobtcData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val obj = JSONObject(response)
                
                if (obj.getInt("status") == 200) {
                    val data = obj.getJSONObject("data")
                    
                    last = Utils.numberComplete(data.getString("lastprice"), 8)
                    coinVolume = Utils.numberComplete(data.getString("volume"), 8)
                    ask = Utils.numberComplete(data.getString("ask"), 8)
                    bid = Utils.numberComplete(data.getString("bid"), 8)
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
