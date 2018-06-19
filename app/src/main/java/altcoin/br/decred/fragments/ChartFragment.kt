package altcoin.br.decred.fragments

import altcoin.br.decred.R
import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import altcoin.br.decred.utils.numberComplete
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
import android.widget.Toast
import com.android.volley.Response
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.fragment_chart.*
import kotlinx.android.synthetic.main.market_chart.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ChartFragment : Fragment() {
    private var chartZoom = 0
    private var chartCandle = 0
    private var showValues = false
    
    private var adapterZoom: ArrayAdapter<String>? = null
    private var adapterCandle: ArrayAdapter<String>? = null
    
    private var running = false
    
    var toast: Toast? = null
    
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_chart, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        instanceObjects()
        
        prepareListeners()
    }
    
    override fun onStart() {
        super.onStart()
        
        loadMarketChart()
        
        running = true
    }
    
    override fun onStop() {
        super.onStop()
        
        running = false
    }
    
    private fun prepareListeners() {
        srChartRefresh?.setOnRefreshListener {
            loadMarketChart()
            
            loadChart()
        }
        
        coinChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, dataSetIndex: Int, h: Highlight) {
                try {
                    val text = "High: ${entries[e.xIndex].high.toString().numberComplete(8)}\n" +
                            "Low: ${entries[e.xIndex].low.toString().numberComplete(8)}\n" +
                            "Open: ${entries[e.xIndex].open.toString().numberComplete(8)}\n" +
                            "Close: ${entries[e.xIndex].close.toString().numberComplete(8)}\n" +
                            "Volume (BTC): ${volumes[e.xIndex].toString().numberComplete(8)}"
                    
                    toast?.cancel()
                    
                    toast = Toast.makeText(activity, text, Toast.LENGTH_LONG)
                    toast?.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            override fun onNothingSelected() {
            }
        })
        
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
                    "3m" -> 3 * 24 * 30
                    "6m" -> 6 * 24 * 30
                    "12m" -> 12 * 24 * 30
                    
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
        coinChart?.drawOrder = arrayOf(CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE)
        
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
        zoom.add("3m")
        zoom.add("6m")
        zoom.add("12m")
        
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
    
    private val volumeEntries = ArrayList<BarEntry>()
    private val volumes = ArrayList<Double>()
    private val entries = ArrayList<CandleEntry>()
    
    private inner class AtParseCandleJson internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal var data: CandleData? = null
        internal var volumeData: BarData? = null
        
        val labels = ArrayList<String>()
        @SuppressLint("DefaultLocale")
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                entries.clear()
                volumeEntries.clear()
                volumes.clear()
                
                val arr = JSONArray(response)
                
                var obj: JSONObject
                
                for (i in 0 until arr.length()) {
                    obj = arr.getJSONObject(i)
                    
                    entries.add(CandleEntry(i, obj.getDouble("high").toFloat(), obj.getDouble("low").toFloat(), obj.getDouble("open").toFloat(), obj.getDouble("close").toFloat()))
                    
                    volumeEntries.add(BarEntry(obj.getDouble("volume").toFloat(), i))
                    
                    volumes.add(obj.getDouble("volume"))
                    
                    labels.add(i.toString() + "")
                }
                
                val dataset = CandleDataSet(entries, "")
                dataset.increasingColor = -0xff0100
                dataset.decreasingColor = -0x10000
                dataset.axisDependency = YAxis.AxisDependency.LEFT
                dataset.decreasingPaintStyle = Paint.Style.FILL
                dataset.shadowColor = -0xffff01
                dataset.setDrawValues(showValues)
                
                data = CandleData(labels, dataset)
                
                val volumeDataSet = BarDataSet(volumeEntries, "")
                volumeDataSet.barShadowColor = -0x333334
                volumeDataSet.highLightColor = -0x333334
                volumeDataSet.axisDependency = YAxis.AxisDependency.RIGHT
                volumeDataSet.color = -0x333334
                volumeDataSet.setDrawValues(false)
                
                volumeData = BarData(labels, volumeDataSet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return null
        }
        
        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            
            if (!running) return
            
            srChartRefresh?.isRefreshing = false
            
            val cd = CombinedData(labels)
            cd.setData(data)
            cd.setData(volumeData)
            
            val yAxis = coinChart?.axisLeft
            
            yAxis?.setStartAtZero(false)
            
            coinChart?.data = cd
            
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
            
            labelsBid.reverse()
            
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
}
