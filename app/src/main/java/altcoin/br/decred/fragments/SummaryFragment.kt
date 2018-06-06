package altcoin.br.decred.fragments

import altcoin.br.decred.R
import altcoin.br.decred.adapter.AdapterExchanges
import altcoin.br.decred.model.ExchangeData
import altcoin.br.decred.services.NotificationCoinService
import altcoin.br.decred.utils.*
import altcoin.br.decred.utils.exchanges.EnumExchanges
import android.app.Fragment
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Response
import kotlinx.android.synthetic.main.coin_market_cap.*
import kotlinx.android.synthetic.main.fragment_summary.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONObject

class SummaryFragment : Fragment() {
    private var running: Boolean =
            false
    
    private var adapterExchanges: AdapterExchanges? = null
    
    private val exchanges =
            ArrayList<ExchangeData>()
    
    override fun onStart() {
        super.onStart()
        
        instanceObjects()
        
        loadCoinMarketCapData()
        
        prepareListeners()
        
        running = true
        
        try {
            EventBus.getDefault().register(this)
        } catch (_: Exception) {
        }
    }
    
    override fun onStop() {
        super.onStop()
        
        running = false
    }
    
    private fun prepareListeners() {
        cbSummaryPinMarketCapPriceInNotifications.setOnCheckedChangeListener { _, b ->
            try {
                activity.writePreference("pinMarketCapPriceInNotifications", b)
                
                if (b) {
                    activity.startService(Intent(activity, NotificationCoinService::class.java))
                } else {
                    val mNotificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    
                    mNotificationManager.cancel(hash("marketcap"))
                    
                    activity.stopService(Intent(activity, NotificationCoinService::class.java))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun instanceObjects() {
        exchanges.add(ExchangeData(EnumExchanges.BITTREX))
        exchanges.add(ExchangeData(EnumExchanges.POLONIEX))
        exchanges.add(ExchangeData(EnumExchanges.BLEUTRADE))
        exchanges.add(ExchangeData(EnumExchanges.PROFITFY))
        
        adapterExchanges = AdapterExchanges(activity, exchanges)
        
        rvExchanges.setHasFixedSize(true)
        
        // use a linear layout manager
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        
        rvExchanges.layoutManager = linearLayoutManager
        rvExchanges.adapter = adapterExchanges
        
        cbSummaryPinMarketCapPriceInNotifications.isChecked = activity.readPreference("pinMarketCapPriceInNotifications", false)
    }
    
    @Subscribe
    fun eventBusReceiver(obj: JSONObject) {
        try {
            if (obj.has("tag") && obj.getString("tag").equals("update", ignoreCase = true) && running) {
                loadCoinMarketCapData()
                
                Utils.log("update ::: SummaryFragment")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadCoinMarketCapData() {
        val url = "https://api.coinmarketcap.com/v1/ticker/decred/"
        
        val listener = Response.Listener<String> { response -> AtParseMarketCapData(response).execute() }
        
        val internetRequests = InternetRequests()
        internetRequests.executeGet(url, listener)
    }
    
    private inner class AtParseMarketCapData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
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
    
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_summary, container, false)
    }
}
