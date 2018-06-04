package altcoin.br.decred.widget

import altcoin.br.decred.MainActivity
import altcoin.br.decred.R
import altcoin.br.decred.data.DBTools
import altcoin.br.decred.utils.Bitcoin
import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.widget.RemoteViews
import android.widget.Toast
import com.android.volley.Response
import org.json.JSONObject
import java.util.*

class PriceWidgetProvider : AppWidgetProvider() {
    
    private val hour: String
        get() {
            val c = Calendar.getInstance()
            
            var h = c.get(Calendar.HOUR).toString().padStart(2, '0')
            val m = c.get(Calendar.MINUTE).toString().padStart(2, '0')
            
            if (h == "00") {
                val a = c.get(Calendar.AM_PM)
                
                if (a == Calendar.PM)
                    h = "12"
            }
            
            return "$h:$m"
        }
    
    override fun onReceive(context: Context, intent: Intent) {
        // AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        
        if (WIDGET_BUTTON == intent.action) {
            try {
                Toast.makeText(context, "Updating widget", Toast.LENGTH_LONG).show()
                
                val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
                
                val thisWidget = ComponentName(context.applicationContext, PriceWidgetProvider::class.java)
                
                val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    onUpdate(context, appWidgetManager, appWidgetIds)
                }
                
                Utils.logFabric("widgetUpdateManually")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        super.onReceive(context, intent)
    }
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        
        Utils.log("PriceWidgetProvider ::: onUpdate")
        
        val db = DBTools(context)
        
        for (appWidgetId in appWidgetIds) {
            
            if (db.search("select exchange, fiat from coin_widgets where widget_id = WID".replace("WID".toRegex(), appWidgetId.toString() + "")) > 0) {
                
                when {
                    db.getData(0).equals("poloniex", ignoreCase = true) -> loadDataFromPoloniex(context, appWidgetManager, appWidgetId, db.getData(1))
                    db.getData(0).equals("bittrex", ignoreCase = true) -> loadDataFromBittrex(context, appWidgetManager, appWidgetId, db.getData(1))
                    else -> loadDataFromBleutrade(context, appWidgetManager, appWidgetId, db.getData(1))
                }
            } else
                loadDataFromPoloniex(context, appWidgetManager, appWidgetId, db.getData(1))
        }
        
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
    
    private fun getSpecificSummary(response: String): JSONObject? {
        try {
            val coin = "DCR"
            
            val jObject = JSONObject(response)
            
            val keys = jObject.keys()
            
            var jsonObj: JSONObject
            
            while (keys.hasNext()) {
                val key = keys.next() as String
                if (jObject.get(key) is JSONObject) {
                    jsonObj = jObject.get(key) as JSONObject
                    
                    if (key.startsWith("BTC_") && key.toLowerCase().contains(coin.toLowerCase())) {
                        
                        return jsonObj
                    }
                }
            }
            
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            
            return null
        }
    }
    
    private fun loadDataFromBittrex(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, fiat: String?) {
        val views = RemoteViews(context.packageName, R.layout.appwidget_coin)
        
        views.setTextViewText(R.id.tvWidNameCoin, "DCR - Trex - $hour")
        
        val url = "https://bittrex.com/api/v1.1/public/getmarketsummary?market=BTC-DCR"
        
        val listener = Response.Listener<String> { response ->
            try {
                
                var obj = JSONObject(response)
                
                if (obj.getBoolean("success")) {
                    obj = obj.getJSONArray("result").getJSONObject(0)
                    
                    val last = obj.getString("Last")
                    
                    views.setTextViewText(R.id.tvWidValInBtc, Utils.numberComplete(obj.getString("Last"), 8))
                    
                    val listener2 = Response.Listener<String> { response2 ->
                        try {
                            val obj2 = JSONObject(response2)
                            
                            views.setTextViewText(R.id.tvWidValInFiat, Utils.numberComplete(java.lang.Double.parseDouble(last) * obj2.getDouble("last"), 4))
                            
                            val openApp = Intent(context, MainActivity::class.java)
                            
                            val pendingIntent = PendingIntent.getActivity(context, 0, openApp, 0)
                            
                            views.setOnClickPendingIntent(R.id.tvWidNameCoin, pendingIntent)
                            
                            val intent = Intent(WIDGET_BUTTON)
                            val pendingIntentUpdate = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            views.setOnClickPendingIntent(R.id.ivWidLogo, pendingIntentUpdate)
                            
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    if (fiat != null && fiat.equals("BRL", ignoreCase = true)) {
                        views.setTextViewText(R.id.tvWidFiatName, "BRL: ")
                        Bitcoin.convertBtcToBrl(listener2)
                    } else {
                        views.setTextViewText(R.id.tvWidFiatName, "USD: ")
                        Bitcoin.convertBtcToUsd(listener2)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }
    
    private fun loadDataFromBleutrade(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, fiat: String?) {
        
        val views = RemoteViews(context.packageName, R.layout.appwidget_coin)
        
        views.setTextViewText(R.id.tvWidNameCoin, "DCR - Bleu - $hour")
        
        val url = "https://bleutrade.com/api/v2/public/getmarketsummary?market=DCR_BTC"
        
        val listener = Response.Listener<String> { response ->
            try {
                
                var obj = JSONObject(response)
                
                if (obj.getBoolean("success")) {
                    obj = obj.getJSONArray("result").getJSONObject(0)
                    
                    val last = obj.getString("Last")
                    
                    views.setTextViewText(R.id.tvWidValInBtc, Utils.numberComplete(obj.getString("Last"), 8))
                    
                    val listener2 = Response.Listener<String> { response2 ->
                        try {
                            val obj2 = JSONObject(response2)
                            
                            views.setTextViewText(R.id.tvWidValInFiat, Utils.numberComplete(java.lang.Double.parseDouble(last) * obj2.getDouble("last"), 4))
                            
                            val openApp = Intent(context, MainActivity::class.java)
                            
                            val pendingIntent = PendingIntent.getActivity(context, 0, openApp, 0)
                            
                            views.setOnClickPendingIntent(R.id.tvWidNameCoin, pendingIntent)
                            
                            val intent = Intent(WIDGET_BUTTON)
                            val pendingIntentUpdate = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            views.setOnClickPendingIntent(R.id.ivWidLogo, pendingIntentUpdate)
                            
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    if (fiat != null && fiat.equals("BRL", ignoreCase = true)) {
                        views.setTextViewText(R.id.tvWidFiatName, "BRL: ")
                        Bitcoin.convertBtcToBrl(listener2)
                    } else {
                        views.setTextViewText(R.id.tvWidFiatName, "USD: ")
                        Bitcoin.convertBtcToUsd(listener2)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }
    
    private fun loadDataFromPoloniex(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, fiat: String) {
        // RemoteViews(context.packageName, R.layout.appwidget_coin)
        
        val url = "https://poloniex.com/public?command=returnTicker"
        
        val listener = Response.Listener<String> { response ->
            try {
                
                AtParsePoloniexData(context, appWidgetManager, appWidgetId, fiat, response).execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }
    
    private inner class AtParsePoloniexData internal constructor(internal val context: Context, internal val manager: AppWidgetManager, internal val appWidgetId: Int, internal val fiat: String?, internal val response: String) : AsyncTask<Void, Void, Void>() {
        internal val views = RemoteViews(context.packageName, R.layout.appwidget_coin)
        
        init {
            views.setTextViewText(R.id.tvWidNameCoin, "DCR - Polo - $hour")
        }
        
        override fun doInBackground(vararg data: Void): Void? {
            try {
                val obj = getSpecificSummary(response)
                
                val last = obj!!.getString("last")
                
                views.setTextViewText(R.id.tvWidValInBtc, Utils.numberComplete(last, 8))
                
                val listener2 = Response.Listener<String> { response2 ->
                    try {
                        var obj2 = JSONObject(response2)
                        
                        if (fiat!!.equals("brl", ignoreCase = true))
                            obj2 = JSONObject(response2) //.getJSONObject("ticker_24h").getJSONObject("total");
                        
                        views.setTextViewText(R.id.tvWidValInFiat, Utils.numberComplete(java.lang.Double.parseDouble(last) * obj2.getDouble("last"), 4))
                        
                        val openApp = Intent(context, MainActivity::class.java)
                        
                        val pendingIntent = PendingIntent.getActivity(context, 0, openApp, 0)
                        
                        views.setOnClickPendingIntent(R.id.tvWidNameCoin, pendingIntent)
                        
                        val intent = Intent(WIDGET_BUTTON)
                        val pendingIntentUpdate = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        views.setOnClickPendingIntent(R.id.ivWidLogo, pendingIntentUpdate)
                        
                        Utils.log("PriceWidgetProvider ::: onUpdate ::: FINISHED")
                        
                        manager.updateAppWidget(appWidgetId, views)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                if (fiat != null && fiat.equals("BRL", ignoreCase = true)) {
                    views.setTextViewText(R.id.tvWidFiatName, "BRL: ")
                    Bitcoin.convertBtcToBrl(listener2)
                } else {
                    views.setTextViewText(R.id.tvWidFiatName, "USD: ")
                    Bitcoin.convertBtcToUsd(listener2)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return null
        }
    }
    
    companion object {
        private const val WIDGET_BUTTON = "android.appwidget.action.UPDATE_DRC_WIDGET"
    }
}
