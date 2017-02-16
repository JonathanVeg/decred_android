package altcoin.br.decred.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import com.android.volley.Response;

import java.util.Timer;
import java.util.TimerTask;

import altcoin.br.decred.MainActivity;
import altcoin.br.decred.R;
import altcoin.br.decred.data.DBTools;
import altcoin.br.decred.model.Wallet;
import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class BalanceChangesService extends Service {
    private Timer timer = new Timer();

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();

        int minutes = 15;

        timer.scheduleAtFixedRate(new mainTask(), 0, minutes * 60 * 1000);
    }

    private class mainTask extends TimerTask {
        public void run() {
            Utils.log("BalanceChangesService ::: mainTask");

            if (!Utils.readPreference(getApplicationContext(), "alert_balance_changes", false)) {
                stopSelf();

                return;
            }

            DBTools db = new DBTools(getApplicationContext());

            try {
                int count = db.search("select address, balance from wallets");

                Utils.log("COUNT ::: " + count);

                if (count == 0) {
                    stopSelf();

                    return;
                }

                for (int i = 0; i < count; i++) {
                    final String wallet = db.getData(i, 0);

                    final String lastBalance = db.getData(i, 1);

                    Response.Listener<String> listener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (!Utils.numberComplete(response, 8).equals(Utils.numberComplete(lastBalance, 8))) {
                                Utils.log("" + response);

                                Utils.log("" + lastBalance);

                                // save for avoiding another alerts

                                Wallet w = new Wallet(wallet);

                                w.setBalance(response + "");

                                boolean saved = w.save(getApplicationContext());

                                Utils.log(" ::: " + saved);

                                createNotification(wallet, "" + lastBalance, "" + response);
                            }
                        }
                    };


                    String url = "https://www.blockexperts.com/api?coin=dcr&action=getbalance&address=ADDRESS".replaceAll("ADDRESS", wallet);

                    InternetRequests ir = new InternetRequests();

                    ir.executeGet(url, listener);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.close();
            }
        }
    }

    private void createNotification(String wallet, String lastBalance, String currentBalance) {
        Context context = getApplicationContext();
        int id = hash(wallet);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);

        TaskStackBuilder stack = TaskStackBuilder.create(context);
        stack.addParentStack(MainActivity.class);
        stack.addNextIntent(intent);

        PendingIntent pendingIntent = stack.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        SpannableString sb1, sb2, sb3, sb4;

        String text = "DECRED (DCR)";
        sb1 = new SpannableString(text);
        sb1.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":") + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        text = "Value changed wallet: " + wallet;

        sb4 = new SpannableString(text);
        sb4.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":") + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(TextUtils.concat(sb1));
        builder.setContentText(TextUtils.concat(sb4));

        builder.setSmallIcon(R.drawable.dcr_icon);
        // builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_monetization_on_white_24dp));

        builder.setPriority(Notification.PRIORITY_DEFAULT);

        builder.setContentIntent(pendingIntent);

        text = "Last balance: LASTVALUE".replaceAll("LASTVALUE", Utils.numberComplete(lastBalance, 8));
        sb1 = new SpannableString(text);
        sb1.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":") + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        text = "Last balance: LASTVALUE".replaceAll("LASTVALUE", Utils.numberComplete(lastBalance, 8));

        sb2 = new SpannableString(text);
        sb2.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":") + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        text = "Current balance: CURRENTVALUE".replaceAll("CURRENTVALUE", Utils.numberComplete(currentBalance, 8));
        sb3 = new SpannableString(text);
        sb3.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":") + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        NotificationCompat.Style inboxStyle = new NotificationCompat.InboxStyle()
                .addLine(sb4)
                .addLine(sb2)
                .addLine(sb3)
                .setSummaryText("Decred");

        builder.setStyle(inboxStyle);

        Notification notification = builder.build();

        notification.ledARGB = Color.rgb(30, 232, 226);
        notification.ledOnMS = 500;
        notification.ledOffMS = 1000;

        notification.vibrate = new long[]{150, 300, 150, 300};

        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;

        notificationManager.notify(id, notification);
    }

    private int hash(String name) {
        String s = name.replaceAll(" ", "");

        int h = 0;

        for (int i = 0; i < s.length(); i++)
            h = 31 * h + s.charAt(i);

        return h;
    }
}

