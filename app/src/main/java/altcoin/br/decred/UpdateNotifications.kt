package altcoin.br.decred

import altcoin.br.decred.services.NotificationCoinService
import altcoin.br.decred.utils.log
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UpdateNotifications : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "DECRED_UPDATE_NOTIFICATIONS") {
            try {
                // restarta o service
                context.stopService(Intent(context.applicationContext, NotificationCoinService::class.java))
                
                context.startService(Intent(context.applicationContext, NotificationCoinService::class.java))
                
                log("DECRED_UPDATE_NOTIFICATIONS")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
