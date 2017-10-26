package altcoin.br.decred.fragments

import altcoin.br.decred.R
import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.annotation.SuppressLint
import android.app.Fragment
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.android.volley.Response
import com.github.mikephil.charting.data.*
import kotlinx.android.synthetic.main.fragment_chart.*
import kotlinx.android.synthetic.main.market_chart.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ChartFragment : Fragment() {
    private var chartZoom: Int = 0
    private var chartCandle: Int = 0
    private var showValues: Boolean = false

    private var adapterZoom: ArrayAdapter<String>? = null
    private var adapterCandle: ArrayAdapter<String>? = null

    private var running: Boolean = false

    override fun onStart() {
        super.onStart()

        loadMarketChart()

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
                loadMarketChart()

                Utils.log("update ::: ChartFragment")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onPause() {
        super.onPause()

        running = false
    }

    private fun prepareListeners() {
        sZoom?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                val item = adapterZoom?.getItem(position) ?: return

                Utils.writePreference(activity, "chartZoom", item)

                chartZoom = when (item) {
                    "3h" -> 3
                    "6h" -> 6
                    "24h" -> 24
                    "2d" -> 48
                    "1w" -> 24 * 7
                    "2w" -> 24 * 7 * 2
                    "1m" -> 24 * 30

                    else -> 3
                }

                loadChart()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }

        sCandle?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                var item: String? = adapterCandle?.getItem(position) ?: return

                Utils.log("sCandle changed ::: " + item!!)

                Utils.writePreference(activity, "chartCandle", item)

                item = item.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

                chartCandle = when (item) {
                    "5" -> 5 * 60
                    "15" -> 15 * 60
                    "30" -> 30 * 60
                    "120" -> 120 * 60
                    "240" -> 240 * 60

                    else -> 5 * 60
                }

                loadChart()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }

        cbShowValues?.setOnCheckedChangeListener { _, b ->
            showValues = b

            loadChart()
        }
    }

    private fun instanceObjects() {
        chartZoom = 3
        chartCandle = 30 * 60

        showValues = false

        val zoom = ArrayList<String>()
        val candle = ArrayList<String>()

        zoom.add("3h")
        zoom.add("6h")
        zoom.add("24h")
        zoom.add("2d")
        zoom.add("1w")
        zoom.add("2w")
        zoom.add("1m")

        candle.add("5-min")
        candle.add("15-min")
        candle.add("30-min")
        candle.add("120-min")
        candle.add("240-min")

        adapterZoom = ArrayAdapter(activity, android.R.layout.simple_spinner_item, zoom)
        adapterZoom?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sZoom?.adapter = adapterZoom

        adapterCandle = ArrayAdapter(activity, android.R.layout.simple_spinner_item, candle)
        adapterCandle?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sCandle?.adapter = adapterCandle

        adapterZoom?.getPosition(Utils.readPreference(activity, "chartZoom", "3h"))?.let { sZoom?.setSelection(it) }
        adapterCandle?.getPosition(Utils.readPreference(activity, "chartCandle", "15-min"))?.let { sCandle?.setSelection(it) }
    }

    private fun loadChart() {
        val url = "https://poloniex.com/public?" +
                "command=returnChartData" +
                "&currencyPair=BTC_DCR" +
                "&start=" + (Utils.timestampLong() - 60 * chartZoom * 60) +
                "&period=" + chartCandle

        val listener = Response.Listener<String> { response -> AtParseCandleJson(response).execute() }

        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }

    private inner class AtParseCandleJson internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal lateinit var data: CandleData
        internal val entries = ArrayList<CandleEntry>()

        @SuppressLint("DefaultLocale")
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val arr = JSONArray(response)

                var obj: JSONObject

                val labels = ArrayList<String>()

                for (i in 0 until arr.length()) {
                    obj = arr.getJSONObject(i)

                    entries.add(CandleEntry(i, obj.getDouble("high").toFloat(),
                            obj.getDouble("low").toFloat(), obj.getDouble("open").toFloat(), obj.getDouble("close").toFloat()))

                    labels.add(i.toString() + "")
                }

                val dataset = CandleDataSet(entries, "")
                dataset.increasingColor = -0xff0100
                dataset.decreasingColor = -0x10000
                dataset.decreasingPaintStyle = Paint.Style.FILL
                dataset.shadowColor = -0xffff01
                dataset.setDrawValues(showValues)

                data = CandleData(labels, dataset)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            if (!running) return

            val yAxis = coinChart?.axisLeft

            yAxis?.setStartAtZero(false)

            coinChart?.data = data

            coinChart?.axisRight?.setDrawLabels(false)

            coinChart?.setDescription("")

            coinChart?.notifyDataSetChanged()

            coinChart?.invalidate()
        }
    }

    private fun loadMarketChart() {
        val url = "https://poloniex.com/public?command=returnOrderBook&currencyPair=BTC_DCR&depth=750"

        val listener = Response.Listener<String> { response -> AtParseMarketChart(response).execute() }

        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }

    private inner class AtParseMarketChart internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal val entriesBid: ArrayList<Entry> = ArrayList()
        internal val entriesAsk: ArrayList<Entry> = ArrayList()

        internal val labelsBid: ArrayList<String> = ArrayList()
        internal val labelsAsk: ArrayList<String> = ArrayList()

        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val jObject = JSONObject(response)

                val keys = jObject.keys()

                var internal: JSONArray

                while (keys.hasNext()) {
                    val key = keys.next() as String

                    if (jObject.get(key) is JSONArray) {
                        if (key == "bids") {
                            internal = jObject.getJSONArray(key)

                            var totalAsk = 0.0

                            for (i in 0 until internal.length()) {
                                val item = internal.getJSONArray(i)

                                totalAsk += item.getDouble(0) * item.getDouble(1)

                                entriesBid.add(Entry(totalAsk.toFloat(), i))
                                labelsBid.add(item.getString(0))
                            }
                        }

                        if (key == "asks") {
                            internal = jObject.getJSONArray(key)

                            var totalBid = 0.0

                            for (i in 0 until internal.length()) {
                                val item = internal.getJSONArray(i)

                                totalBid += item.getDouble(0) * item.getDouble(1)

                                entriesAsk.add(Entry(totalBid.toFloat(), i))
                                labelsAsk.add(item.getString(0))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            if (!running) return

            // bid
            // invert the data
            for (i in entriesBid.indices)
                entriesBid[i].xIndex = entriesBid.size - 1 - i

            Collections.reverse(labelsBid)

            val datasetBid = LineDataSet(entriesBid, "Bids")

            datasetBid.color = -0xff0100

            datasetBid.setDrawValues(false)

            datasetBid.fillColor = -0xff0100

            datasetBid.setDrawCircles(false)

            datasetBid.setDrawFilled(true)

            val lineDataBid = LineData(labelsBid, datasetBid)

            marketChartBid?.data = lineDataBid

            marketChartBid?.axisRight?.setDrawLabels(false)

            marketChartBid?.setDescription("")

            marketChartBid?.notifyDataSetChanged()

            marketChartBid?.invalidate()

            // ask
            val datasetAsk = LineDataSet(entriesAsk, "Asks")

            datasetAsk.color = -0x10000

            datasetAsk.setDrawValues(false)

            datasetAsk.fillColor = -0x10000

            datasetAsk.setDrawCircles(false)

            datasetAsk.setDrawFilled(true)

            val lineDataAsk = LineData(labelsAsk, datasetAsk)

            marketChartAsk?.data = lineDataAsk

            marketChartAsk?.axisRight?.setDrawLabels(false)

            marketChartAsk?.setDescription("")

            marketChartAsk?.notifyDataSetChanged()

            marketChartAsk?.invalidate()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_chart, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        instanceObjects()

        prepareListeners()
    }
}
