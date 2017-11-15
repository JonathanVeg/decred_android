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

			try {
				EventBus.getDefault().post(JSONObject("{}").put("tag", "update"))
			} catch (_: Exception) {
			}

			handler = Handler()

			handler?.postDelayed(this, 10000)
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
		} catch (e: Exception) {
			e.printStackTrace()
		}

	}

	override fun onBackPressed() {
		if (currentTab != Companion.TAB_SUMMARY) {
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
		bSummary?.setOnClickListener { changeTab(Companion.TAB_SUMMARY) }

		bChart?.setOnClickListener { changeTab(Companion.TAB_CHART) }

		bCalculator?.setOnClickListener { changeTab(Companion.TAB_CALC) }

		bAbout?.setOnClickListener { changeTab(Companion.TAB_ABOUT) }

		bAlerts?.setOnClickListener { changeTab(Companion.TAB_ALERT) }

		bStats?.setOnClickListener { changeTab(Companion.TAB_STATS) }
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
			Companion.TAB_SUMMARY -> {
				resetFooter()

				currentTab = Companion.TAB_SUMMARY

				bSummary?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))

				val fragment = SummaryFragment()

				ft.replace(R.id.llFragments, fragment, "task").commit()

				Utils.logFabric("tabChanged", "tab", "summary")
			}
			Companion.TAB_CHART -> {
				resetFooter()

				currentTab = Companion.TAB_CHART

				bChart?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))

				val fragment = ChartFragment()

				ft.replace(R.id.llFragments, fragment, "task").commit()

				Utils.logFabric("tabChanged", "tab", "chart")
			}
			Companion.TAB_CALC -> {
				resetFooter()

				currentTab = Companion.TAB_CALC

				bCalculator?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))

				val fragment = CalculatorFragment()

				ft.replace(R.id.llFragments, fragment, "task").commit()

				Utils.logFabric("tabChanged", "tab", "calculator")
			}
			Companion.TAB_ABOUT -> {
				resetFooter()

				currentTab = Companion.TAB_ABOUT

				bAbout?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))

				val fragment = AboutFragment()

				ft.replace(R.id.llFragments, fragment, "task").commit()

				Utils.logFabric("tabChanged", "tab", "about")
			}
			Companion.TAB_ALERT -> {
				resetFooter()

				currentTab = Companion.TAB_ALERT

				bAlerts?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))

				val fragment = AlertsFragment()

				ft.replace(R.id.llFragments, fragment, "task").commit()

				Utils.logFabric("tabChanged", "tab", "alerts")
			}
			Companion.TAB_STATS -> {
				resetFooter()

				currentTab = Companion.TAB_STATS

				bStats?.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.silver))

				val fragment = StatsFragment()

				ft.replace(R.id.llFragments, fragment, "task").commit()

				Utils.logFabric("tabChanged", "tab", "stats")
			}
		}
	}

	companion object {
		private val TAB_SUMMARY = 0
		private val TAB_CHART = 1
		private val TAB_CALC = 2
		private val TAB_ALERT = 3
		private val TAB_STATS = 4
		private val TAB_ABOUT = 5
	}
}
