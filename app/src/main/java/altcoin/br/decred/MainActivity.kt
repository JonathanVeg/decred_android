package altcoin.br.decred

import altcoin.br.decred.fragments.*
import altcoin.br.decred.services.PriceAlertService
import altcoin.br.decred.utils.Utils
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.Log
import kotlinx.android.synthetic.main.bittrex.*
import kotlinx.android.synthetic.main.bleutrade.*
import kotlinx.android.synthetic.main.coin_market_cap.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.ll_footer.*
import kotlinx.android.synthetic.main.poloniex.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class MainActivity : Activity() {
    private var currentTab = 0
    
    private var handler: Handler? = null
    
    private val runnableCode = object : Runnable {
        override fun run() {
            tvLastUpdate?.text = Utils.now()
            
            handler = Handler()
            
            handler?.postDelayed(this, 10000)
            
            try {
                EventBus.getDefault().post(JSONObject("{}").put("tag", "update"))
            } catch (_: Exception) {
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        if (actionBar != null) {
            actionBar?.setDisplayShowHomeEnabled(true)
            actionBar?.title = "Decred"
        }
        
        instanceObjects()
        
        prepareListeners()
        
        resetFooter()
        
        bSummary?.performClick()
        
        startService(Intent(this, PriceAlertService::class.java))
        
        try {
            EventBus.getDefault().register(this)
        } catch (_: Exception) {
        }
    }
    
    override fun onBackPressed() {
        if (currentTab != TAB_SUMMARY) {
            bSummary?.performClick()
            
            return
        }
        
        super.onBackPressed()
    }
    
    override fun onStart() {
        super.onStart()
        
        // creating the handler for updating the altcoin.br.decred.data constantily
        try {
            
            handler = Handler()
            
            handler?.postDelayed(runnableCode, 10000)
        } catch (e: Exception) {
            Log.e("Handler", "Error while creating handler")
            
            e.printStackTrace()
        }
    }
    
    override fun onPause() {
        super.onPause()
        
        try {
            handler?.removeCallbacks(runnableCode)
        } catch (e: Exception) {
            Log.e("Handler", "Error while pausing handler")
            
            e.printStackTrace()
        }
    }
    
    private fun instanceObjects() {
        Utils.textViewLink(tvOficialSite, "https://decred.info/")
        Utils.textViewLink(tvPoloniexTitle, "https://coinmarketcap.com/exchanges/poloniex/")
        Utils.textViewLink(tvBittrexTitle, "https://coinmarketcap.com/exchanges/bittrex/")
        Utils.textViewLink(tvBleutradeTitle, "https://coinmarketcap.com/exchanges/bleutrade/")
        Utils.textViewLink(tvCoinMarketCapTitle, "https://coinmarketcap.com/currencies/decred/#markets")
    }
    
    private fun prepareListeners() {
        bSummary?.setOnClickListener { changeTab(TAB_SUMMARY) }
        
        bChart?.setOnClickListener { changeTab(TAB_CHART) }
        
        bCalculator?.setOnClickListener { changeTab(TAB_CALC) }
        
        bAbout?.setOnClickListener { changeTab(TAB_ABOUT) }
        
        bAlerts?.setOnClickListener { changeTab(TAB_ALERT) }
        
        bStats?.setOnClickListener { changeTab(TAB_STATS) }
    }
    
    private fun resetFooter() {
        bSummary?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver))
        bChart?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver))
        bCalculator?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver))
        bAbout?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver))
        bStats?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver))
        bAlerts?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver))
    }
    
    private fun changeTab(tab: Int) {
        val fm = fragmentManager
        
        val ft = fm.beginTransaction()
        
        when (tab) {
            TAB_SUMMARY -> {
                resetFooter()
                
                currentTab = TAB_SUMMARY
                
                bSummary?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                val fragment = SummaryFragment()
                
                ft.replace(R.id.llFragments, fragment, "task").commit()
                
                Utils.logFabric("tabChanged", "tab", "summary")
            }
            TAB_CHART -> {
                resetFooter()
                
                currentTab = TAB_CHART
                
                bChart?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                val fragment = ChartFragment()
                
                ft.replace(R.id.llFragments, fragment, "task").commit()
                
                Utils.logFabric("tabChanged", "tab", "chart")
            }
            TAB_CALC -> {
                resetFooter()
                
                currentTab = TAB_CALC
                
                bCalculator?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                val fragment = CalculatorFragment()
                
                ft.replace(R.id.llFragments, fragment, "task").commit()
                
                Utils.logFabric("tabChanged", "tab", "calculator")
            }
            TAB_ABOUT -> {
                resetFooter()
                
                currentTab = TAB_ABOUT
                
                bAbout?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                val fragment = AboutFragment()
                
                ft.replace(R.id.llFragments, fragment, "task").commit()
                
                Utils.logFabric("tabChanged", "tab", "about")
            }
            TAB_ALERT -> {
                resetFooter()
                
                currentTab = TAB_ALERT
                
                bAlerts?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                val fragment = AlertsFragment()
                
                ft.replace(R.id.llFragments, fragment, "task").commit()
                
                Utils.logFabric("tabChanged", "tab", "alerts")
            }
            TAB_STATS -> {
                resetFooter()
                
                currentTab = TAB_STATS
                
                bStats?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                val fragment = StatsFragment()
                
                ft.replace(R.id.llFragments, fragment, "task").commit()
                
                Utils.logFabric("tabChanged", "tab", "stats")
            }
        }
    }
    
    companion object {
        private const val TAB_SUMMARY = 0
        private const val TAB_CHART = 1
        private const val TAB_CALC = 2
        private const val TAB_ALERT = 3
        private const val TAB_STATS = 4
        private const val TAB_ABOUT = 5
    }
}
