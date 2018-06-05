package altcoin.br.decred.fragments

import altcoin.br.decred.MainActivity
import altcoin.br.decred.R
import altcoin.br.decred.adapter.AdapterExchanges
import altcoin.br.decred.model.ExchangeData
import altcoin.br.decred.utils.*
import altcoin.br.decred.utils.exchanges.EnumExchanges
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.NotificationCompat
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
        } catch (e: Exception) {
            e.printStackTrace()
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
                    loadDataAndCreateMarketCapNotification()
                } else {
                    val mNotificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    
                    mNotificationManager.cancel(hash("marketcap"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadDataAndCreateMarketCapNotification() {
        val url = "https://api.coinmarketcap.com/v1/ticker/decred/"
        
        val listener = Response.Listener<String> { response -> AtParseAndPrepareMarketCapNotification(response).execute() }
        
        val internetRequests = InternetRequests()
        
        internetRequests.executeGet(url, listener)
    }
    
    private inner class AtParseAndPrepareMarketCapNotification internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal var usdPrice: String = ""
        internal var btcPrice: String = ""
        internal var p24hChanges: String = ""
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val obj = JSONArray(response).getJSONObject(0)
                
                usdPrice = Utils.numberComplete(obj.getString("price_usd"), 4)
                btcPrice = Utils.numberComplete(obj.getString("price_btc"), 8)
                p24hChanges = Utils.numberComplete(obj.getString("percent_change_24h"), 2)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return null
        }
        
        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            val listener = Response.Listener<String> { response ->
                try {
                    val obj = JSONObject(response)
                    
                    val brlPrice = Utils.numberComplete(java.lang.Double.parseDouble(btcPrice) * obj.getDouble("last"), 4)
                    
                    prepareMarketCapNotification(btcPrice, p24hChanges, usdPrice, brlPrice)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            Bitcoin.convertBtcToBrl(listener)
        }
    }
    
    private fun prepareMarketCapNotification(bitcoinPrice: String, changes:String, usdPrice: String, brlPrice: String) {
        val context = activity
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(context, MainActivity::class.java)
        
        val stack = TaskStackBuilder.create(context)
        stack.addNextIntent(intent)
        
        val pendingIntent = stack.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT)
        
        val builder = NotificationCompat.Builder(context)
        builder.setContentTitle("Decred Price")
        
        builder.setContentText("BTC: ${bitcoinPrice.numberComplete(8)} | ${changes.numberComplete(1)}%")
        
        // builder.setSmallIcon(R.drawable.ic_monetization_on_white_36dp);
        builder.setSmallIcon(R.drawable.logo_notification)
        
        builder.setPriority(Notification.PRIORITY_MIN)
        builder.setContentIntent(pendingIntent)
        
        val inboxStyle = NotificationCompat.InboxStyle()
                .addLine("BTC: ${bitcoinPrice.numberComplete(8)} | ${changes.numberComplete(1)}%")
                .addLine("USD: ${usdPrice.numberComplete(4)}")
                .addLine("BRL: ${brlPrice.numberComplete(4)}")
                .setSummaryText("Decred")
        
        builder.setStyle(inboxStyle)
        
        val notification = builder.build()
        
        notification.flags = Notification.FLAG_NO_CLEAR
        
        notificationManager.notify(hash("marketcap"), notification)
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
