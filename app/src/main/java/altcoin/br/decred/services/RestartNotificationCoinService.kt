package altcoin.br.decred.services

import altcoin.br.decred.utils.log
import altcoin.br.decred.utils.readPreference
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RestartNotificationCoinService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("RestartService", "RestartNotificationCoinService")
        
        try {
            if (context.readPreference("pinMarketCapPriceInNotifications", false)) {
                context.startService(Intent(context.applicationContext, NotificationCoinService::class.java))
                
                log("RestartNotificationCoinService TEM ALGO")
            } else
                log("RestartNotificationCoinService NÃO TEM ALGO")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
