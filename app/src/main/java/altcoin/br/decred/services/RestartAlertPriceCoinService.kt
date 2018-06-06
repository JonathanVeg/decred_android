package altcoin.br.decred.services

import altcoin.br.decred.data.DBTools
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RestartAlertPriceCoinService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("RestartService", "RestartAlertPriceCoinService")
        
        val db = DBTools(context)
        
        try {
            if (db.search("select _id, awhen, value, active, bittrex, poloniex from alerts where active = 1") > 0)
                context.startService(Intent(context.applicationContext, NotificationCoinService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }
}
