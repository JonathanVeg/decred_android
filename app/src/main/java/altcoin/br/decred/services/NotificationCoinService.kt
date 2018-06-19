package altcoin.br.decred.services

import altcoin.br.decred.MainActivity
import altcoin.br.decred.R
import altcoin.br.decred.utils.*
import altcoin.br.decred.utils.exchanges.AbstractExchange
import altcoin.br.decred.utils.exchanges.EnumExchanges
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.android.volley.Response
import org.json.JSONObject
import java.util.*

class NotificationCoinService : Service() {
    private val timer =
            Timer()
    
    private var exchangeName = "bittrex"
    
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
                if (readPreference("pinMarketCapPriceInNotifications", false)) {
                    exchangeName = readPreference("pinMarketCapPriceInNotificationsExchange", "bittrex")
                    
                    loadDataAndCreatePinnedNotification()
                } else {
                    val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    
                    mNotificationManager.cancel(hash("marketcap"))
                    
                    stopSelf()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadDataAndCreatePinnedNotification() {
        val exchange =
                when (exchangeName) {
                    "bittrex" -> EnumExchanges.BITTREX
                    "poloniex" -> EnumExchanges.POLONIEX
                    "bleutrade" -> EnumExchanges.BLEUTRADE
                    "profitfy" -> EnumExchanges.PROFITFY
                    "ooobtc" -> EnumExchanges.OOOBTC
                    
                    else -> EnumExchanges.HUOBI
                }
        
        object : AbstractExchange(exchange, "dcr", "btc") {
            override fun onValueLoaded() {
                val self = this
                
                val listener = Response.Listener<String> { response ->
                    try {
                        val obj = JSONObject(response)
                        
                        val brlPrice = Utils.numberComplete(java.lang.Double.parseDouble(self.last) * obj.getDouble("last"), 4)
                        
                        val listener2 = Response.Listener<String> { response2 ->
                            try {
                                val obj2 = JSONObject(response2)
                                
                                val usdPrice = Utils.numberComplete(java.lang.Double.parseDouble(self.last) * obj2.getDouble("last"), 4)
                                
                                preparePinnedNotification(self.last, self.changes, usdPrice, brlPrice)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        
                        Bitcoin.convertBtcToUsd(listener2)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                Bitcoin.convertBtcToBrl(listener)
            }
        }
    }
    
    private fun preparePinnedNotification(bitcoinPrice: String, changes: String, usdPrice: String, brlPrice: String) {
        val context = this
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(context, MainActivity::class.java)
        
        val stack = TaskStackBuilder.create(context)
        stack.addNextIntent(intent)
        
        val pendingIntent = stack.getPendingIntent(hash("marketcap"), PendingIntent.FLAG_UPDATE_CURRENT)
        
        val builder = NotificationCompat.Builder(context)
        builder.setContentTitle("Decred Price | ${exchangeName.capitalize()}")
        
        builder.setContentText(makeSpannableBold("BTC", "${bitcoinPrice.numberComplete(8)} | ${changes.numberComplete(1)}%"))
        
        // builder.setSmallIcon(R.drawable.ic_monetization_on_white_36dp);
        builder.setSmallIcon(R.drawable.logo_notification)
        
        builder.setPriority(Notification.PRIORITY_MIN)
        builder.setContentIntent(pendingIntent)
        
        val inboxStyle = NotificationCompat.InboxStyle()
                .addLine(makeSpannableBold("BTC:", "${bitcoinPrice.numberComplete(8)} | ${changes.numberComplete(1)}%"))
                .addLine(makeSpannableBold("USD:", usdPrice.numberComplete(4)))
                .addLine(makeSpannableBold("BRL:", brlPrice.numberComplete(4)))
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