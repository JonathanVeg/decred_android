package altcoin.br.decred.widget

import altcoin.br.decred.MainActivity
import altcoin.br.decred.R
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

class TicketWidgetProvider : AppWidgetProvider() {
    
    private val hour: String
        get() {
            val c = Calendar.getInstance()
            
            var h = "" + c.get(Calendar.HOUR)
            var m = "" + c.get(Calendar.MINUTE)
            
            if (h.length == 1) h = "0$h"
            
            if (m.length == 1) m = "0$m"
            
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
                
                val thisWidget = ComponentName(context.applicationContext, TicketWidgetProvider::class.java)
                
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
        
        Utils.log("TicketWidgetProvider ::: onUpdate")
        
        for (appWidgetId in appWidgetIds) {
            loadData(context, appWidgetManager, appWidgetId)
        }
        
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
    
    private fun loadData(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val url = "https://dcrstats.com/api/v1/get_stats?" + Utils.timestampLong()
        
        val listener = Response.Listener<String> { response ->
            try {
                
                AtParseTicketData(context, appWidgetManager, appWidgetId, response).execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        val internetRequests = InternetRequests()
        internetRequests.executeGet(url, listener)
    }
    
    private inner class AtParseTicketData internal constructor(internal val context: Context, internal val manager: AppWidgetManager, internal val appWidgetId: Int, internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal val views = RemoteViews(context.packageName, R.layout.appwidget_ticket)
        
        init {
            views.setTextViewText(R.id.tvWidNameCoin, "DCR - Polo - $hour")
        }
        
        override fun doInBackground(vararg data: Void?): Void? {
            try {
                val obj = JSONObject(response)
                
                views.setTextViewText(R.id.tvTicWidNameCoin, "DCR Tickets - $hour")
                views.setTextViewText(R.id.tvTicWidPrice, Utils.numberComplete(obj.getString("sbits"), 2) + " DCRs")
                views.setTextViewText(R.id.tvTicWidNextPrice, Utils.numberComplete(obj.getString("est_sbits"), 2) + " DCRs")
                views.setTextViewText(R.id.tvTicWidPriceAjustBlocks, obj.getString("pos_adjustment"))
                views.setTextViewText(R.id.tvTicWidPriceAdjustTime, (obj.getDouble("pos_adjustment") * obj.getDouble("average_minutes")).toString() + " min")
                
                val intent = Intent(WIDGET_BUTTON)
                val pendingIntentUpdate = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                views.setOnClickPendingIntent(R.id.ivTicWidLogo, pendingIntentUpdate)
                
                val openApp = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, openApp, 0)
                views.setOnClickPendingIntent(R.id.tvTicWidNameCoin, pendingIntent)
                
                manager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return null
        }
    }
    
    companion object {
        private const val WIDGET_BUTTON = "android.appwidget.action.UPDATE_DRC_TICKET_WIDGET"
    }
}
