package altcoin.br.decred

import altcoin.br.decred.services.PriceAlertService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReceiverOnBootComplete : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startService(Intent(context, PriceAlertService::class.java))
        }
    }
}
