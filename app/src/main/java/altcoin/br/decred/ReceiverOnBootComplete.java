package altcoin.br.decred;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import altcoin.br.decred.services.BalanceChangesService;
import altcoin.br.decred.services.PriceAlertService;

public class ReceiverOnBootComplete extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent arg1) {
        if (arg1.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, PriceAlertService.class));
            context.startService(new Intent(context, BalanceChangesService.class));
        }
    }
}
