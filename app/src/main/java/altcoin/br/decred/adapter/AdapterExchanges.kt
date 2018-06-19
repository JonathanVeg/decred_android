package altcoin.br.decred.adapter

import altcoin.br.decred.R
import altcoin.br.decred.model.ExchangeData
import altcoin.br.decred.utils.*
import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.Response
import org.json.JSONArray
import org.json.JSONObject

class AdapterExchanges(private val context: Activity, private val exchanges: ArrayList<ExchangeData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    
    override fun getItemViewType(position: Int) =
            if (exchanges[position].exchange.label.toLowerCase() == "CoinMarketCap".toLowerCase())
                0
            else
                1
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            val view = layoutInflater.inflate(R.layout.row_exchanges, parent, false)
            
            ViewHolderExchanges(view)
        } else {
            val view = layoutInflater.inflate(R.layout.coin_market_cap, parent, false)
            
            ViewHolderCoinMarketCap(view)
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        try {
            if (getItemViewType(position) == 1) {
                val h = holder as ViewHolderExchanges
                val exchange = exchanges[position].exchange
                val coin = exchanges[position].coin
                val market = exchanges[position].market
                
                h.tvExchangeTitle.text = "$exchange - (${coin.toUpperCase()} / ${market.toUpperCase()})"
                
                h.tvExchangeBaseVolumeLabel.text = "Volume (in ${market.toUpperCase()}): "
                h.tvExchangeCoinVolumeLabel.text = "Volume (in ${coin.toUpperCase()}): "
                
                log("onBindViewHolder $position")
                
                val exchangeData = exchanges[position]
                
                h.tvExchangeLast.text = exchangeData.last
                h.tvExchangeBaseVolume.text = exchangeData.baseVolume
                h.tvExchangeCoinVolume.text = exchangeData.coinVolume
                h.tvExchangeBid.text = exchangeData.bid
                h.tvExchangeAsk.text = exchangeData.ask
                h.tvExchangeLow.text = exchangeData.low
                h.tvExchangeHigh.text = exchangeData.high
                h.tvExchangeChanges.text = "(${String.format("%s%%", exchangeData.changes)})"
                
                if (exchangeData.changes.toDouble() >= 0)
                    h.tvExchangeChanges.setTextColor(ContextCompat.getColor(context, R.color.colorChangesUp))
                else
                    h.tvExchangeChanges.setTextColor(ContextCompat.getColor(context, R.color.colorChangesDown))
                
                val listLLayouts = listOf<View>(h.llExchangeLast, h.llExchangeBaseVolume, h.llExchangeCoinVolume, h.llExchangeBid, h.llExchangeLow, h.tvExchangeChanges)
                val listValues = listOf(exchangeData.last, exchangeData.baseVolume, exchangeData.coinVolume, exchangeData.bid, exchangeData.low, exchangeData.changes)
                
                listValues.forEachIndexed { index, s ->
                    listLLayouts[index].setVisibility(s != "-1", true)
                }
                
                if (position == 0)
                    h.ivExchangeUp.hide()
                else
                    h.ivExchangeUp.show()
                
                if (position == exchanges.size - 1)
                    h.ivExchangeDown.hide()
                else
                    h.ivExchangeDown.show()
            } else {
                val h = holder as ViewHolderCoinMarketCap
                val url = "https://api.coinmarketcap.com/v1/ticker/decred/"
                
                val listener = Response.Listener<String> { response -> AtParseMarketCapData(response, h).execute() }
                
                val internetRequests = InternetRequests()
                internetRequests.executeGet(url, listener)
                
                if (position == 0)
                    h.ivExchangeUp.hide()
                else
                    h.ivExchangeUp.show()
                
                if (position == exchanges.size - 1)
                    h.ivExchangeDown.hide()
                else
                    h.ivExchangeDown.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private inner class AtParseMarketCapData internal constructor(internal val response: String, internal val v: ViewHolderCoinMarketCap) : AsyncTask<Void?, Void?, Void?>() {
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
            
            try {
                v.tvSummaryBtcPrice.text = btcPrice
                v.tvSummaryUsdPrice.text = usdPrice
                v.tvSummaryUsd24hVolume.text = usdVolume24h
                v.tvSummaryUsdMarketCap.text = usdMarketCap
                v.tvSummary24hChanges.text = String.format("%s%%", p24hChanges)
                
                if (java.lang.Double.parseDouble(p24hChanges) >= 0)
                    v.tvSummary24hChanges.setTextColor(ContextCompat.getColor(context, R.color.colorChangesUp))
                else
                    v.tvSummary24hChanges.setTextColor(ContextCompat.getColor(context, R.color.colorChangesDown))
                
                val listener = Response.Listener<String> { response ->
                    try {
                        val obj = JSONObject(response) // .getJSONObject("ticker_24h").getJSONObject("total")
                        
                        v.tvSummaryBrlPrice.text = Utils.numberComplete(java.lang.Double.parseDouble(btcPrice) * obj.getDouble("last"), 4)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                Bitcoin.convertBtcToBrl(listener)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun getItemId(position: Int) =
            position.toLong()
    
    override fun getItemCount() =
            exchanges.size
    
    private inner class ViewHolderExchanges internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        internal val tvExchangeTitle = v.findViewById<CopyableTextView>(R.id.tvExchangeTitle)
        internal val tvExchangeBaseVolumeLabel = v.findViewById<TextView>(R.id.tvExchangeBaseVolumeLabel)
        internal val tvExchangeLast = v.findViewById<CopyableTextView>(R.id.tvExchangeLast)
        internal val tvExchangeBaseVolume = v.findViewById<CopyableTextView>(R.id.tvExchangeBaseVolume)
        internal val tvExchangeCoinVolume = v.findViewById<CopyableTextView>(R.id.tvExchangeCoinVolume)
        internal val tvExchangeCoinVolumeLabel = v.findViewById<TextView>(R.id.tvExchangeCoinVolumeLabel)
        internal val tvExchangeBid = v.findViewById<CopyableTextView>(R.id.tvExchangeBid)
        internal val tvExchangeAsk = v.findViewById<CopyableTextView>(R.id.tvExchangeAsk)
        internal val tvExchangeLow = v.findViewById<CopyableTextView>(R.id.tvExchangeLow)
        internal val tvExchangeHigh = v.findViewById<CopyableTextView>(R.id.tvExchangeHigh)
        internal val tvExchangeChanges = v.findViewById<CopyableTextView>(R.id.tvExchangeChanges)
        
        internal val llExchangeLast = v.findViewById<LinearLayout>(R.id.llExchangeLast)
        internal val llExchangeBaseVolume = v.findViewById<LinearLayout>(R.id.llExchangeBaseVolume)
        internal val llExchangeCoinVolume = v.findViewById<LinearLayout>(R.id.llExchangeCoinVolume)
        internal val llExchangeBid = v.findViewById<LinearLayout>(R.id.llExchangeBid)
        internal val llExchangeAsk = v.findViewById<LinearLayout>(R.id.llExchangeAsk)
        internal val llExchangeLow = v.findViewById<LinearLayout>(R.id.llExchangeLow)
        internal val llExchangeHigh = v.findViewById<LinearLayout>(R.id.llExchangeHigh)
        internal val llExchangeChanges = v.findViewById<LinearLayout>(R.id.llExchangeChanges)
        
        internal val ivExchangeUp = v.findViewById<ImageView>(R.id.ivExchangeUp)
        internal val ivExchangeDown = v.findViewById<ImageView>(R.id.ivExchangeDown)
        
        init {
            ivExchangeUp.setOnClickListener {
                if (adapterPosition > 0) {
                    val temp = exchanges[adapterPosition - 1]
                    
                    exchanges[adapterPosition - 1] = exchanges[adapterPosition]
                    
                    exchanges[adapterPosition] = temp
                    
                    notifyDataSetChanged()
                    
                    saveNewPositions()
                }
            }
            
            ivExchangeDown.setOnClickListener {
                if (adapterPosition < exchanges.size - 1) {
                    val temp = exchanges[adapterPosition + 1]
                    
                    exchanges[adapterPosition + 1] = exchanges[adapterPosition]
                    
                    exchanges[adapterPosition] = temp
                    
                    notifyDataSetChanged()
                    
                    saveNewPositions()
                }
            }
        }
        
        private fun saveNewPositions() {
            val orderLabels = ArrayList<String>()
            
            exchanges.map {
                orderLabels.add(it.exchange.label.toLowerCase())
            }
            
            Utils.writePreference(context, "summaryOrderExchanges", orderLabels.joinToString(separator = ","))
            
            log(Utils.readPreference(context, "summaryOrderExchanges", ""))
        }
    }
    
    private inner class ViewHolderCoinMarketCap internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        internal val tvSummaryBtcPrice = v.findViewById<CopyableTextView>(R.id.tvSummaryBtcPrice)
        internal val tvSummaryUsdPrice = v.findViewById<CopyableTextView>(R.id.tvSummaryUsdPrice)
        internal val tvSummaryBrlPrice = v.findViewById<CopyableTextView>(R.id.tvSummaryBrlPrice)
        internal val tvSummary24hChanges = v.findViewById<CopyableTextView>(R.id.tvSummary24hChanges)
        internal val tvSummaryUsd24hVolume = v.findViewById<CopyableTextView>(R.id.tvSummaryUsd24hVolume)
        internal val tvSummaryUsdMarketCap = v.findViewById<CopyableTextView>(R.id.tvSummaryUsdMarketCap)
        
        internal val ivExchangeUp = v.findViewById<ImageView>(R.id.ivExchangeUp)
        internal val ivExchangeDown = v.findViewById<ImageView>(R.id.ivExchangeDown)
        
        init {
            ivExchangeUp.setOnClickListener {
                if (adapterPosition > 0) {
                    val temp = exchanges[adapterPosition - 1]
                    
                    exchanges[adapterPosition - 1] = exchanges[adapterPosition]
                    
                    exchanges[adapterPosition] = temp
                    
                    notifyDataSetChanged()
                    
                    saveNewPositions()
                }
            }
            
            ivExchangeDown.setOnClickListener {
                if (adapterPosition < exchanges.size - 1) {
                    val temp = exchanges[adapterPosition + 1]
                    
                    exchanges[adapterPosition + 1] = exchanges[adapterPosition]
                    
                    exchanges[adapterPosition] = temp
                    
                    notifyDataSetChanged()
                    
                    saveNewPositions()
                }
            }
        }
        
        private fun saveNewPositions() {
            val orderLabels = ArrayList<String>()
            
            exchanges.map {
                orderLabels.add(it.exchange.label.toLowerCase())
            }
            
            Utils.writePreference(context, "summaryOrderExchanges", orderLabels.joinToString(separator = ","))
            
            log(Utils.readPreference(context, "summaryOrderExchanges", ""))
        }
    }
}