package altcoin.br.decred.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONObject;

import java.util.Calendar;

import altcoin.br.decred.MainActivity;
import altcoin.br.decred.R;
import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class TicketWidgetProvider extends AppWidgetProvider {

    private static final String WIDGET_BUTTON = "android.appwidget.action.UPDATE_DRC_TICKET_WIDGET";

    @Override
    public void onReceive(Context context, Intent intent) {
        // AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if (WIDGET_BUTTON.equals(intent.getAction())) {
            try {
                Toast.makeText(context, "Updating widget", Toast.LENGTH_LONG).show();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());

                ComponentName thisWidget = new ComponentName(context.getApplicationContext(), TicketWidgetProvider.class);

                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }

                Utils.logFabric("widgetUpdateManually");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Utils.log("TicketWidgetProvider ::: onUpdate");

        for (final int appWidgetId : appWidgetIds) {
            loadData(context, appWidgetManager, appWidgetId);
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

    private void loadData(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        String url = "https://dcrstats.com/api/v1/get_stats?" + Utils.timestampLong();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    new AtParseTicketData(context, appWidgetManager, appWidgetId, response).execute();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        InternetRequests internetRequests = new InternetRequests();
        internetRequests.executeGet(url, listener);
    }

    private class AtParseTicketData extends AsyncTask<Void, Void, Void> {
        final String response;
        final Context context;
        final int appWidgetId;
        final AppWidgetManager manager;
        final RemoteViews views;

        AtParseTicketData(final Context context, AppWidgetManager appWidgetManager, final int appWidgetId, String data) {
            this.response = data;
            this.context = context;
            this.appWidgetId = appWidgetId;

            manager = appWidgetManager;

            views = new RemoteViews(context.getPackageName(), R.layout.appwidget_ticket);

            views.setTextViewText(R.id.tvWidNameCoin, "DCR - Polo - " + getHour());
        }

        @Override
        protected Void doInBackground(Void... data) {
            try {
                JSONObject obj = new JSONObject(response);

                views.setTextViewText(R.id.tvTicWidNameCoin, "DCR Tickets - " + getHour());
                views.setTextViewText(R.id.tvTicWidPrice, Utils.numberComplete(obj.getString("sbits"), 2) + " DCRs");
                views.setTextViewText(R.id.tvTicWidNextPrice, Utils.numberComplete(obj.getString("est_sbits"), 2) + " DCRs");
                views.setTextViewText(R.id.tvTicWidPriceAjustBlocks, obj.getString("pos_adjustment"));
                views.setTextViewText(R.id.tvTicWidPriceAdjustTime, String.valueOf(obj.getDouble("pos_adjustment") * obj.getDouble("average_minutes")) + " min");

                Intent intent = new Intent(WIDGET_BUTTON);
                PendingIntent pendingIntentUpdate = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.ivTicWidLogo, pendingIntentUpdate);

                Intent openApp = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openApp, 0);
                views.setOnClickPendingIntent(R.id.tvTicWidNameCoin, pendingIntent);

                manager.updateAppWidget(appWidgetId, views);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
