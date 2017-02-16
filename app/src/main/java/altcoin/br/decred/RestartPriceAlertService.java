package altcoin.br.decred;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import altcoin.br.decred.services.PriceAlertService;

public class RestartPriceAlertService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("RestartService", "Restarting PriceAlertService");

        context.startService(new Intent(context.getApplicationContext(), PriceAlertService.class));
    }
}
