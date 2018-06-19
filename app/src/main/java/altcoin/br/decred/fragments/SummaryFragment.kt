package altcoin.br.decred.fragments

import altcoin.br.decred.R
import altcoin.br.decred.adapter.AdapterExchanges
import altcoin.br.decred.model.ExchangeData
import altcoin.br.decred.services.NotificationCoinService
import altcoin.br.decred.utils.*
import altcoin.br.decred.utils.exchanges.AbstractExchange
import altcoin.br.decred.utils.exchanges.EnumExchanges
import android.app.Fragment
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_summary.*
import org.greenrobot.eventbus.EventBus

class SummaryFragment : Fragment() {
    
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater?.inflate(R.layout.fragment_summary, container, false)
    
    private var running: Boolean = false
    
    private var adapterExchanges: AdapterExchanges? = null
    
    private val exchanges =
            ArrayList<ExchangeData>()
    
    override fun onStart() {
        super.onStart()
        
        instanceObjects()
        
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
        srSummaryRefresh?.setOnRefreshListener {
            loadData()
            
            srSummaryRefresh?.isRefreshing = false
        }
        
        cbSummaryPinMarketCapPriceInNotifications?.setOnCheckedChangeListener { _, b ->
            try {
                sSummaryPinMarketCapPriceInNotificationsCoin.setVisibility(b)
                
                activity.writePreference("pinMarketCapPriceInNotifications", b)
                activity.writePreference("pinMarketCapPriceInNotificationsExchange", "bittrex")
                
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
        
        sSummaryPinMarketCapPriceInNotificationsCoin?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val exchange = sSummaryPinMarketCapPriceInNotificationsCoin.selectedItem.toString().toLowerCase()
                
                activity.writePreference("pinMarketCapPriceInNotificationsExchange", exchange)
                
                activity.stopService(Intent(activity, NotificationCoinService::class.java))
                activity.startService(Intent(activity, NotificationCoinService::class.java))
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
    
    private fun instanceObjects() {
        exchanges.add(ExchangeData(EnumExchanges.BITTREX, "dcr", "btc"))
        exchanges.add(ExchangeData(EnumExchanges.OOOBTC, "dcr", "btc"))
        exchanges.add(ExchangeData(EnumExchanges.HUOBI, "dcr", "btc"))
        exchanges.add(ExchangeData(EnumExchanges.POLONIEX, "dcr", "btc"))
        exchanges.add(ExchangeData(EnumExchanges.BLEUTRADE, "dcr", "btc"))
        exchanges.add(ExchangeData(EnumExchanges.PROFITFY, "dcr", "brl"))
        exchanges.add(ExchangeData(EnumExchanges.COINMARKETCAP, "dcr", "btc"))
        
        val order = Utils.readPreference(activity, "summaryOrderExchanges", "")
        
        if (order != "") {
            val listOrder = order.split(",")
            
            val listExchangesSorted = ArrayList<ExchangeData>()
            
            listOrder.map { exchangeLabel ->
                exchanges.map {
                    if (it.exchange.label.toLowerCase() == exchangeLabel) {
                        if (listExchangesSorted.none { it.exchange.label.toLowerCase() == exchangeLabel })
                            listExchangesSorted.add(it)
                    }
                }
            }
            
            exchanges.clear()
            
            exchanges.addAll(listExchangesSorted)
            
            listExchangesSorted.map {
                log(it.exchange.label)
            }
        }
        
        adapterExchanges = AdapterExchanges(activity, exchanges)
        
        loadData()
        
        rvExchanges.setHasFixedSize(true)
        
        // use a linear layout manager
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        
        rvExchanges.layoutManager = linearLayoutManager
        rvExchanges.adapter = adapterExchanges
        
        cbSummaryPinMarketCapPriceInNotifications.isChecked = activity.readPreference("pinMarketCapPriceInNotifications", false)
        
        if (cbSummaryPinMarketCapPriceInNotifications.isChecked) {
            val exchangeName = activity.readPreference("pinMarketCapPriceInNotificationsExchange", "bittrex")
            
            val adapter = ArrayAdapter.createFromResource(activity, R.array.exchanges, android.R.layout.simple_spinner_item)
            
            sSummaryPinMarketCapPriceInNotificationsCoin.setSelection(adapter.getPosition(exchangeName.capitalize()))
            
            sSummaryPinMarketCapPriceInNotificationsCoin.show()
        }
    }
    
    private fun loadData() {
        exchanges.map {
            object : AbstractExchange(exchange = it.exchange, coin = it.coin, market = it.market) {
                override fun onValueLoaded() {
                    try {
                        it.last = last
                        it.baseVolume = baseVolume
                        it.coinVolume = coinVolume
                        it.bid = bid
                        it.ask = ask
                        it.low = low
                        it.high = high
                        it.changes = changes
                        
                        adapterExchanges?.notifyDataSetChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
