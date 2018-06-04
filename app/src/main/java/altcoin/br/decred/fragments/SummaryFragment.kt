package altcoin.br.decred.fragments

import altcoin.br.decred.R
import altcoin.br.decred.utils.Bitcoin
import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import altcoin.br.decred.utils.exchanges.AbstractExchange
import altcoin.br.decred.utils.exchanges.EnumExchanges
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
import kotlinx.android.synthetic.main.profitfy.*
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
        
        loadProfitfyData()
        
        running = true
        
        try {
            EventBus.getDefault().register(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onStop() {
        super.onStop()
        
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
                
                loadProfitfyData()
                
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
                    val obj = JSONObject(response) // .getJSONObject("ticker_24h").getJSONObject("total")
                    
                    tvSummaryBrlPrice.text = Utils.numberComplete(java.lang.Double.parseDouble(btcPrice) * obj.getDouble("last"), 4)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            Bitcoin.convertBtcToBrl(listener)
        }
    }
    
    private fun loadBittrexData() {
        object : AbstractExchange(EnumExchanges.BITTREX) {
            override fun onValueLoaded() {
                if (!running) return
                
                try {
                    tvBittrexLast.text = last
                    tvBittrexBaseVolume.text = baseVolume
                    tvBittrexBid.text = bid
                    tvBittrexAsk.text = ask
                    tvBittrexChanges.text = String.format("%s%%", changes)
                    
                    if (java.lang.Double.parseDouble(changes) >= 0)
                        tvBittrexChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesUp))
                    else
                        tvBittrexChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesDown))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun loadPoloniexData() {
        object : AbstractExchange(EnumExchanges.POLONIEX) {
            override fun onValueLoaded() {
                try {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun loadProfitfyData() {
        object : AbstractExchange(EnumExchanges.PROFITFY) {
            override fun onValueLoaded() {
                try {
                    if (!running) return
                    
                    tvProfitfyLast.text = last
                    tvProfitfyBaseVolume.text = baseVolume
                    tvProfitfyBid.text = bid
                    tvProfitfyAsk.text = ask
                    tvProfitfyChanges.text = String.format("%s%%", changes)
                    
                    if (java.lang.Double.parseDouble(changes) >= 0)
                        tvProfitfyChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesUp))
                    else
                        tvProfitfyChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesDown))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun loadBleutradeData() {
        object : AbstractExchange(EnumExchanges.BLEUTRADE) {
            override fun onValueLoaded() {
                try {
                    if (!running) return
                    
                    tvBleutradeLast.text = last
                    tvBleutradeBaseVolume.text = baseVolume
                    tvBleutradeBid.text = bid
                    tvBleutradeAsk.text = ask
                    tvBleutradeChanges.text = String.format("%s%%", changes)
                    
                    if (java.lang.Double.parseDouble(changes) >= 0)
                        tvBleutradeChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesUp))
                    else
                        tvBleutradeChanges.setTextColor(ContextCompat.getColor(activity, R.color.colorChangesDown))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_summary, container, false)
    }
}
