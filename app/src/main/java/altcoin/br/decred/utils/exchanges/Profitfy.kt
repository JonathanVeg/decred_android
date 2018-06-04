package altcoin.br.decred.utils.exchanges

import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.os.AsyncTask
import com.android.volley.Response
import org.json.JSONArray

open class Profitfy : Exchange() {
    override fun onValueLoaded() {
    }
    
    override fun loadData() {
        val url = "https://profitfy.trade/api/v1/ticker/btc/dcr"
        
        val listener = Response.Listener<String> { response -> AtParseProfitfyData(response).execute() }
        
        val internetRequests = InternetRequests()
        internetRequests.executeGet(url, listener)
    }
    
    private inner class AtParseProfitfyData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val obj = JSONArray(response).getJSONObject(0)
                
                last = Utils.numberComplete(obj.getString("last"), 8)
                baseVolume = Utils.numberComplete(obj.getString("volume"), 8)
                ask = Utils.numberComplete(obj.getString("sell"), 8)
                bid = Utils.numberComplete(obj.getString("buy"), 8)
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
