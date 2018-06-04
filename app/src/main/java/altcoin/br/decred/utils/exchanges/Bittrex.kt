package altcoin.br.decred.utils.exchanges

import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.os.AsyncTask
import com.android.volley.Response
import org.json.JSONObject

open class Bittrex : Exchange() {
    override fun onValueLoaded() {
    }
    
    override fun loadData() {
        val url = "https://bittrex.com/api/v1.1/public/getmarketsummary?market=BTC-DCR"
        
        val listener = Response.Listener<String> { response -> AtParseBittrexData(response).execute() }
        
        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }
    
    private inner class AtParseBittrexData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        
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
            
            onValueLoaded()
            
            // if (!running) return
            
            // tvBittrexLast.text = last
            // tvBittrexBaseVolume.text = baseVolume
            // tvBittrexBid.text = bid
            // tvBittrexAsk.text = ask
            // tvBittrexChanges.text = String.format("%s%%", changes)
            // 
            // if (changes == null) changes = "0"
            // 
            // if (java.lang.Double.parseDouble(changes) >= 0)
            //     tvBittrexChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesUp))
            // else
            //     tvBittrexChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesDown))
        }
    }
}
