package altcoin.br.decred.services

import altcoin.br.decred.MainActivity
import altcoin.br.decred.R
import altcoin.br.decred.data.DBTools
import altcoin.br.decred.model.Alert
import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.AsyncTask
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.text.Html
import android.text.SpannableString
import com.android.volley.Response
import org.json.JSONObject
import java.util.*

class PriceAlertService : Service() {
    private val timer = Timer()
    override fun onDestroy() {
        super.onDestroy()
        
        sendBroadcast(Intent("DecredKillAlertPriceService"))
    }
    
    override fun onBind(arg0: Intent): IBinder? {
        return null
    }
    
    override fun onCreate() {
        super.onCreate()
        
        val minutes = 5
        
        timer.scheduleAtFixedRate(MainTask(), 0, (minutes * 60 * 1000).toLong())
    }
    
    private fun createNotification(id: Int, contentText: String, line1: SpannableString, line2: SpannableString, line3: SpannableString) {
        val context = applicationContext
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(context, MainActivity::class.java)
        
        val stack = TaskStackBuilder.create(context)
        stack.addNextIntent(intent)
        
        val pendingIntent = stack.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT)
        
        val builder = NotificationCompat.Builder(context)
        builder.setContentTitle("Decred - Alert")
        
        builder.setContentText(contentText)
        
        // builder.setSmallIcon(R.drawable.ic_monetization_on_white_36dp);
        builder.setSmallIcon(R.drawable.logo_notification)
        
        builder.setPriority(Notification.PRIORITY_MAX)
        builder.setContentIntent(pendingIntent)
        
        val inboxStyle = NotificationCompat.InboxStyle()
                .addLine(line1)
                .addLine(line2)
                .addLine(line3)
                .setSummaryText("Decred")
        
        builder.setStyle(inboxStyle)
        
        val notification = builder.build()
        
        notification.ledARGB = -0x100
        notification.ledOnMS = 500
        notification.ledOffMS = 1000
        
        notification.vibrate = longArrayOf(150, 300, 150, 300)
        
        notification.flags = Notification.FLAG_AUTO_CANCEL or Notification.FLAG_SHOW_LIGHTS
        
        try {
            val song = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            val ringtone = RingtoneManager.getRingtone(this, song)
            
            ringtone.play()
        } catch (ignored: Exception) {
        }
        
        notificationManager.notify(id, notification)
    }
    
    private fun loadBittrexData(alert: Alert) {
        val url = "https://bittrex.com/api/v1.1/public/getmarketsummary?market=BTC-DCR"
        
        val listener = Response.Listener<String> { response -> AtParseBittrexData(response, alert).execute() }
        
        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }
    
    private fun loadPoloniexData(alert: Alert) {
        val url = "https://poloniex.com/public?command=returnTicker"
        
        val listener = Response.Listener<String> { response -> AtParsePoloniexData(response, alert).execute() }
        
        val internetRequests = InternetRequests()
        internetRequests.executePost(url, listener)
    }
    
    private fun prepareNotification(alerts: List<Alert>) {
        for (i in alerts.indices) {
            
            val alert = alerts[i]
            
            if (alert.isBittrex) loadBittrexData(alert)
            
            if (alert.isPoloniex) loadPoloniexData(alert)
        }
    }
    
    private fun hash(str: String): Int {
        val s = str.replace(" ".toRegex(), "")
        
        var h = 0
        
        for (i in 0 until s.length)
            h = 31 * h + s[i].toInt()
        
        return h
    }
    
    private inner class MainTask : TimerTask() {
        override fun run() {
            val alerts = ArrayList<Alert>()
            
            val db = DBTools(applicationContext)
            
            try {
                val count = db.search("select _id, awhen, value, active, bittrex, poloniex from alerts where active = 1")
                
                if (count > 0) {
                    var alert: Alert
                    
                    for (i in 0 until count) {
                        alert = Alert(applicationContext)
                        
                        alert.setId(db.getData(i, 0))
                        alert.setWhen(db.getData(i, 1))
                        alert.value = db.getData(i, 2)
                        alert.isActive = Utils.isTrue(db.getData(i, 3))
                        alert.isBittrex = Utils.isTrue(db.getData(i, 4))
                        alert.isPoloniex = Utils.isTrue(db.getData(i, 5))
                        
                        alerts.add(alert)
                    }
                    
                    prepareNotification(alerts)
                } else {
                    stopSelf()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                db.close()
            }
        }
    }
    
    private inner class AtParseBittrexData internal constructor(internal val response: String, internal val alert: Alert) : AsyncTask<Void?, Void?, Void?>() {
        internal var last: Double = 0.toDouble()
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                
                var obj = JSONObject(response)
                
                if (obj.getBoolean("success")) {
                    obj = obj.getJSONArray("result").getJSONObject(0)
                    
                    last = java.lang.Double.parseDouble(obj.getString("Last"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return null
        }
        
        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            
            val nameCoin = "DCR"
            
            var text = getString(R.string.alert_reached_for).replace("COIN".toRegex(), nameCoin).replace("EXCHANGE".toRegex(), "Bittrex")
            
            if (alert.`when` == Alert.GREATER && last > alert.valueDouble || alert.`when` == Alert.LOWER && last < alert.valueDouble) {
                
                val line1 = SpannableString(Html.fromHtml(text))
                
                val notificationId: Int
                
                if (alert.`when` == Alert.GREATER) {
                    notificationId = hash("bittrexgreater" + alert.value)
                    
                    text = getString(R.string.gets_greater_than).replace("COIN", nameCoin).replace("VALUE", Utils.numberComplete(alert.valueDouble, 8))
                } else {
                    notificationId = hash("bittrexlower" + alert.value)
                    
                    text = getString(R.string.gets_lower_than).replace("COIN", nameCoin).replace("VALUE", Utils.numberComplete(alert.valueDouble, 8))
                }
                
                val line2 = SpannableString(Html.fromHtml(text))
                
                text = getString(R.string.alert_last_value).replace("VALUE", Utils.numberComplete(last, 8))
                val line3 = SpannableString(Html.fromHtml(text))
                
                text = getString(R.string.alert_reached_for).replace("COIN", nameCoin).replace("EXCHANGE".toRegex(), "Bittrex")
                
                createNotification(notificationId, Html.fromHtml(text).toString(), line1, line2, line3)
                
                alert.isActive = false
                
                alert.save()
            }
        }
    }
    
    private inner class AtParsePoloniexData internal constructor(internal val response: String, internal val alert: Alert) : AsyncTask<Void?, Void?, Void?>() {
        internal var last: Double = 0.toDouble()
        internal fun getSpecificSummary(response: String): JSONObject? {
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
        
        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                val obj = getSpecificSummary(response)
                
                last = java.lang.Double.parseDouble(obj!!.getString("last"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return null
        }
        
        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            
            val nameCoin = "DCR"
            
            var text = getString(R.string.alert_reached_for).replace("COIN".toRegex(), nameCoin).replace("EXCHANGE".toRegex(), "Poloniex")
            
            if (alert.`when` == Alert.GREATER && last > alert.valueDouble || alert.`when` == Alert.LOWER && last < alert.valueDouble) {
                
                val line1 = SpannableString(Html.fromHtml(text))
                
                val notificationId: Int
                
                if (alert.`when` == Alert.GREATER) {
                    notificationId = hash("poloniexgreater" + alert.value)
                    
                    text = getString(R.string.gets_greater_than).replace("COIN", nameCoin).replace("VALUE", Utils.numberComplete(alert.valueDouble, 8))
                } else {
                    notificationId = hash("poloniexlower" + alert.value)
                    
                    text = getString(R.string.gets_lower_than).replace("COIN", nameCoin).replace("VALUE", Utils.numberComplete(alert.valueDouble, 8))
                }
                
                val line2 = SpannableString(Html.fromHtml(text))
                
                text = getString(R.string.alert_last_value).replace("VALUE", Utils.numberComplete(last, 8))
                val line3 = SpannableString(Html.fromHtml(text))
                
                text = getString(R.string.alert_reached_for).replace("COIN", nameCoin).replace("EXCHANGE".toRegex(), "Poloniex")
                
                createNotification(notificationId, Html.fromHtml(text).toString(), line1, line2, line3)
                
                alert.isActive = false
                
                alert.save()
            }
        }
    }
}
