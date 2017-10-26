package altcoin.br.decred.fragments

import altcoin.br.decred.R
import altcoin.br.decred.utils.Bitcoin
import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.annotation.SuppressLint
import android.app.Fragment
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Response
import kotlinx.android.synthetic.main.fragment_stats.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONObject

class StatsFragment : Fragment() {
    private var statsTimeStamp: Long = 0

    private var running: Boolean = false

    private var brlPrice: Double = 0.toDouble()
    private var usdPrice: Double = 0.toDouble()
    private var btcPrice: Double = 0.toDouble()

    override fun onStart() {
        super.onStart()

        Utils.log("onAttach")

        loadData()

        running = true

        try {
            EventBus.getDefault().register(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Subscribe
    fun eventBusReceiver(obj: JSONObject) {
        try {
            if (obj.has("tag") && obj.getString("tag").equals("update", ignoreCase = true) && running) {
                loadData()

                Utils.log("update ::: StatsFragment")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun loadData() {
        val url = "https://api.coinmarketcap.com/v1/ticker/decred/"

        val listener = Response.Listener<String> { response -> AtParseSummaryData(response).execute() }

        val internetRequests = InternetRequests()
        internetRequests.executeGet(url, listener)
    }

    private inner class AtParseSummaryData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val obj = JSONArray(response).getJSONObject(0)

                usdPrice = obj.getDouble("price_usd")
                btcPrice = obj.getDouble("price_btc")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            if (!running) return

            val listener = Response.Listener<String> { response ->
                try {
                    val obj = JSONObject(response) // .getJSONObject("last").getJSONObject("total")

                    brlPrice = btcPrice * obj.getDouble("last")

                    loadStatsData()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Bitcoin.convertBtcToBrl(listener)
        }
    }

    override fun onPause() {
        super.onPause()

        running = false
    }

    private fun loadStatsData() {
        statsTimeStamp = Utils.timestampLong()

        val url = "https://dcrstats.com/api/v1/get_stats?" + statsTimeStamp

        val listener = Response.Listener<String> { response ->
            AtParseStatsData(response).execute()

            val url2 = "https://dcrstats.com/api/v1/fees"

            val listener2 = Response.Listener<String> { response2 -> AtParseStatsBlockData(response2).execute() }

            val internetRequests = InternetRequests()

            internetRequests.executeGet(url2, listener2)
        }

        val internetRequests = InternetRequests()

        internetRequests.executeGet(url, listener)
    }

    private inner class AtParseStatsBlockData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal var statsLastBlockNumber = ""

        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val arr = JSONArray(response)

                statsLastBlockNumber = arr.getJSONObject(0).getString("height")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            if (!running) return

            tvStatsLastBlockNumber.text = statsLastBlockNumber
        }
    }

    private inner class AtParseStatsData internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal var statsTicketPrice = "" // done
        internal var statsNextTicketPrice = "" // done
        internal var statsMinTicketPrice = "" // done
        internal var statsMaxTicketPrice = "" // done

        internal var statsLastBlockDatetime = "" // done
        internal var statsVoteReward = "" // done

        internal var statsLastAvgBlockTime = "" // done
        internal var statsLastAvgHashrate = "" // done
        internal var statsStakeReward = "" // done
        internal var statsAdjustIn = "" // done
        internal var statsAvailableSupply = ""
        internal var statsPowReward = "" // done
        internal var statsTicketPollSize = "" // done
        internal var statsLockedDcr = "" // done
        internal var statsAvgTicketPrice = "" // done

        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val obj = JSONObject(response)

                statsTicketPrice = Utils.numberComplete(obj.getString("sbits"), 2)
                statsNextTicketPrice = Utils.numberComplete(obj.getString("est_sbits"), 2)
                statsMinTicketPrice = Utils.numberComplete(obj.getString("est_sbits_min"), 2)
                statsMaxTicketPrice = Utils.numberComplete(obj.getString("est_sbits_max"), 2)
                statsPowReward = Utils.numberComplete(obj.getString("pow_reward"), 2)
                statsAdjustIn = obj.getString("pos_adjustment")
                statsStakeReward = Utils.numberComplete(obj.getString("block_reward"), 2)
                statsVoteReward = Utils.numberComplete(obj.getString("vote_reward"), 2)
                statsLockedDcr = obj.getString("ticketpoolvalue")
                statsAvgTicketPrice = Utils.numberComplete(obj.getString("avg_ticket_price"), 2)

                val timestamp = statsTimeStamp - obj.getLong("last_block_datetime")

                var min = (timestamp / 60).toString() + ""
                var sec = (timestamp % 60).toString() + ""

                if (min.length == 1)
                    min = "0" + min

                if (sec.length == 1)
                    sec = "0" + sec

                statsLastBlockDatetime = min + ":" + sec
                statsLastAvgBlockTime = obj.getString("average_minutes") + ":" + obj.getString("average_seconds")

                statsTicketPollSize = obj.getString("poolsize")

                statsLastAvgHashrate = Utils.numberComplete(obj.getLong("networkhashps") / 1000000000000.0, 2)

                statsAvailableSupply = Utils.numberComplete(obj.getLong("coinsupply") / 1000000.0, 0).toString()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            if (!running) return

            tvStatsTicketPrice.text = "$statsTicketPrice (${Utils.numberComplete(java.lang.Double.parseDouble(statsTicketPrice) * usdPrice, 2)} USD - ${Utils.numberComplete(java.lang.Double.parseDouble(statsTicketPrice) * brlPrice, 2)} BRL)"
            tvStatsNextTicketPrice.text = "$statsNextTicketPrice (${Utils.numberComplete(java.lang.Double.parseDouble(statsNextTicketPrice) * usdPrice, 2)} USD - ${Utils.numberComplete(java.lang.Double.parseDouble(statsNextTicketPrice) * brlPrice, 2)} BRL)"
            tvStatsMinTicketPrice.text = "$statsMinTicketPrice (${Utils.numberComplete(java.lang.Double.parseDouble(statsMinTicketPrice) * usdPrice, 2)} USD - ${Utils.numberComplete(java.lang.Double.parseDouble(statsMinTicketPrice) * brlPrice, 2)} BRL)"
            tvStatsMaxTicketPrice.text = "$statsMaxTicketPrice (${Utils.numberComplete(java.lang.Double.parseDouble(statsMaxTicketPrice) * usdPrice, 2)} USD - ${Utils.numberComplete(java.lang.Double.parseDouble(statsMaxTicketPrice) * brlPrice, 2)} BRL)"
            tvStatsLastBlockDatetime.text = statsLastBlockDatetime
            tvStatsLastAvgBlockTime.text = String.format("%s min", statsLastAvgBlockTime)
            tvStatsLastAvgHashrate.text = String.format("%s TH/s", statsLastAvgHashrate)
            tvStatsStakeReward.text = statsStakeReward
            tvStatsAdjustIn.text = String.format("%s blocks", statsAdjustIn)
            tvStatsAvailableSupply.text = statsAvailableSupply
            tvStatsPowReward.text = statsPowReward
            tvStatsTicketPollSize.text = statsTicketPollSize
            tvStatsLockedDcr.text = statsLockedDcr
            tvStatsAvgTicketPrice.text = statsAvgTicketPrice
            tvStatsVoteReward.text = statsVoteReward

            tvStatsLastBlockDatetime.text = statsLastBlockDatetime
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_stats, container, false)
    }
}
