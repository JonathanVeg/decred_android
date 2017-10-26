package altcoin.br.decred

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import altcoin.br.decred.services.PriceAlertService

class RestartPriceAlertService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("RestartService", "Restarting PriceAlertService")

        context.startService(Intent(context.applicationContext, PriceAlertService::class.java))
    }
}
