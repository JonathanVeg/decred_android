package altcoin.br.decred.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

import altcoin.br.decred.MainActivity;
import altcoin.br.decred.R;
import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class CoinWidgetProvider extends AppWidgetProvider {

    private static String WIDGET_BUTTON = "android.appwidget.action.UPDATE_DRC_WIDGET";


    @Override
    public void onReceive(Context context, Intent intent) {
        // AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if (WIDGET_BUTTON.equals(intent.getAction())) {
            try {
                Toast.makeText(context, "Updating widget", Toast.LENGTH_LONG).show();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());

                ComponentName thisWidget = new ComponentName(context.getApplicationContext(), CoinWidgetProvider.class);

                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }

                Utils.answersLog("manuallyWidgetUpdate", "manuallyWidgetUpdate", "0002");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final AppWidgetManager manager = appWidgetManager;

        for (final int appWidgetId : appWidgetIds) {
            Utils.log(" ::: Widget Update");

            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_coin);

            views.setTextViewText(R.id.tvWidNameCoin, "DCR - " + getHour());

            String url = "https://api.coinmarketcap.com/v1/ticker/decred/";

            Response.Listener<String> listener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject obj = new JSONArray(response).getJSONObject(0);

                        views.setTextViewText(R.id.tvWidValInBtc, Utils.numberComplete(obj.getString("price_btc"), 8));
                        views.setTextViewText(R.id.tvWidValInUsd, Utils.numberComplete(obj.getString("price_usd"), 4));

                        Utils.answersLog("widgetUpdate", "widgetUpdate", "0001");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent openApp = new Intent(context, MainActivity.class);

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openApp, 0);

                    views.setOnClickPendingIntent(R.id.tvWidNameCoin, pendingIntent);

                    Intent intent = new Intent(WIDGET_BUTTON);
                    PendingIntent pendingIntentUpdate = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    views.setOnClickPendingIntent(R.id.ivWidLogo, pendingIntentUpdate);


                    manager.updateAppWidget(appWidgetId, views);
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {

                        views.setTextViewText(R.id.tvWidValInBtc, "...");
                        views.setTextViewText(R.id.tvWidValInUsd, "...");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    manager.updateAppWidget(appWidgetId, views);
                }
            };

            InternetRequests internetRequests = new InternetRequests();
            internetRequests.executeGet(url, listener, errorListener);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private String getHour() {
        Calendar c = Calendar.getInstance();

        String h = "" + c.get(Calendar.HOUR);
        String m = "" + c.get(Calendar.MINUTE);

        if (h.length() == 1) h = "0" + h;

        if (m.length() == 1) m = "0" + m;

        if (h.equals("00")) {
            int a = c.get(Calendar.AM_PM);

            if (a == Calendar.PM)
                h = "12";
        }

        return h + ":" + m;
    }

}
