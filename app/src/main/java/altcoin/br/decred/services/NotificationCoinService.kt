package altcoin.br.decred.services

import altcoin.br.decred.MainActivity
import altcoin.br.decred.R
import altcoin.br.decred.utils.*
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.android.volley.Response
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class NotificationCoinService : Service() {
    private val timer =
            Timer()
    
    override fun onBind(arg0: Intent): IBinder? =
            null
    
    override fun onDestroy() {
        super.onDestroy()
        
        sendBroadcast(Intent("DecredKillNotificationCoinService"))
    }
    
    override fun onCreate() {
        super.onCreate()
        
        timer.scheduleAtFixedRate(MainTask(), 0, (5 * 60 * 1000).toLong())
    }
    
    private inner class MainTask : TimerTask() {
        override fun run() {
            try {
                if (readPreference("pinMarketCapPriceInNotifications", false))
                    loadDataAndCreateMarketCapNotification()
                else {
                    val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    
                    mNotificationManager.cancel(hash("marketcap"))
                    
                    stopSelf()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadDataAndCreateMarketCapNotification() {
        val url = "https://api.coinmarketcap.com/v1/ticker/decred/"
        
        val listener = Response.Listener<String> { response -> AtParseAndPrepareMarketCapNotification(response).execute() }
        
        val internetRequests = InternetRequests()
        
        internetRequests.executeGet(url, listener)
    }
    
    private inner class AtParseAndPrepareMarketCapNotification internal constructor(internal val response: String) : AsyncTask<Void?, Void?, Void?>() {
        internal var usdPrice: String = ""
        internal var btcPrice: String = ""
        internal var p24hChanges: String = ""
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val obj = JSONArray(response).getJSONObject(0)
                
                usdPrice = Utils.numberComplete(obj.getString("price_usd"), 4)
                btcPrice = Utils.numberComplete(obj.getString("price_btc"), 8)
                p24hChanges = Utils.numberComplete(obj.getString("percent_change_24h"), 2)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return null
        }
        
        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            val listener = Response.Listener<String> { response ->
                try {
                    val obj = JSONObject(response)
                    
                    val brlPrice = Utils.numberComplete(java.lang.Double.parseDouble(btcPrice) * obj.getDouble("last"), 4)
                    
                    prepareMarketCapNotification(btcPrice, p24hChanges, usdPrice, brlPrice)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            Bitcoin.convertBtcToBrl(listener)
        }
    }
    
    private fun prepareMarketCapNotification(bitcoinPrice: String, changes: String, usdPrice: String, brlPrice: String) {
        val context = this
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(context, MainActivity::class.java)
        
        val stack = TaskStackBuilder.create(context)
        stack.addNextIntent(intent)
        
        val pendingIntent = stack.getPendingIntent(hash("marketcap"), PendingIntent.FLAG_UPDATE_CURRENT)
        
        val builder = NotificationCompat.Builder(context)
        builder.setContentTitle("Decred Price")
        
        builder.setContentText("BTC: ${bitcoinPrice.numberComplete(8)} | ${changes.numberComplete(1)}%")
        
        // builder.setSmallIcon(R.drawable.ic_monetization_on_white_36dp);
        builder.setSmallIcon(R.drawable.logo_notification)
        
        builder.setPriority(Notification.PRIORITY_MIN)
        builder.setContentIntent(pendingIntent)
        
        val inboxStyle = NotificationCompat.InboxStyle()
                .addLine("BTC: ${bitcoinPrice.numberComplete(8)} | ${changes.numberComplete(1)}%")
                .addLine("USD: ${usdPrice.numberComplete(4)}")
                .addLine("BRL: ${brlPrice.numberComplete(4)}")
                .setSummaryText("Decred")
        
        builder.setStyle(inboxStyle)
        
        // Update intent
        val iUpdateNotifications = Intent()
        iUpdateNotifications.action = "DECRED_UPDATE_NOTIFICATIONS"
        val piUpdateNotifications = PendingIntent.getBroadcast(context, 12345, iUpdateNotifications, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.addAction(R.drawable.ic_action_refresh, "Update", piUpdateNotifications)
        
        val notification = builder.build()
        
        notification.flags = Notification.FLAG_NO_CLEAR
        
        notificationManager.notify(hash("marketcap"), notification)
    }
}