package altcoin.br.decred

import altcoin.br.decred.fragments.*
import altcoin.br.decred.services.NotificationCoinService
import altcoin.br.decred.services.PriceAlertService
import altcoin.br.decred.utils.Utils
import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.coin_market_cap.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.ll_footer.*
import org.greenrobot.eventbus.EventBus

class MainActivity : Activity() {
    private var currentTab = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        if (actionBar != null) {
            actionBar?.setDisplayShowHomeEnabled(true)
            actionBar?.title = "Decred"
        }
        
        instanceObjects()
        
        prepareListeners()
        
        bSummary?.performClick()
        
        startService(Intent(this, PriceAlertService::class.java))
        startService(Intent(this, NotificationCoinService::class.java))
        
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
    
    private fun instanceObjects() {
        Utils.textViewLink(tvOfficialSite, "https://decred.org/")
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
        
        resetFooter()
        
        currentTab = tab
        
        var fragment: Fragment? = null
        
        when (tab) {
            TAB_SUMMARY -> {
                bSummary?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                fragment = SummaryFragment()
                
                Utils.logFabric("tabChanged", "tab", "summary")
            }
            TAB_CHART -> {
                bChart?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                fragment = ChartFragment()
                
                Utils.logFabric("tabChanged", "tab", "chart")
            }
            TAB_CALC -> {
                bCalculator?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                fragment = CalculatorFragment()
                
                Utils.logFabric("tabChanged", "tab", "calculator")
            }
            TAB_ABOUT -> {
                bAbout?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                fragment = AboutFragment()
                
                Utils.logFabric("tabChanged", "tab", "about")
            }
            TAB_ALERT -> {
                bAlerts?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                fragment = AlertsFragment()
                
                Utils.logFabric("tabChanged", "tab", "alerts")
            }
            TAB_STATS -> {
                bStats?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))
                
                fragment = StatsFragment()
                
                Utils.logFabric("tabChanged", "tab", "stats")
            }
        }
        
        if (fragment != null)
            ft.replace(R.id.llFragments, fragment).commit()
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
