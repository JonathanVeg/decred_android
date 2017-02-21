package altcoin.br.decred.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import altcoin.br.decred.R;
import altcoin.br.decred.data.DBTools;
import altcoin.br.decred.utils.Utils;

public class ConfigureWidgetActivity extends AppCompatActivity {

    private int mAppWidgetId;
    private Spinner sExchanges;
    private Spinner sFiat;
    private Button bWidSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_widget);

        sExchanges = (Spinner) findViewById(R.id.sWidExchanges);
        sFiat = (Spinner) findViewById(R.id.sWidFiat);
        bWidSave = (Button) findViewById(R.id.bWidSave);

        List<String> listExchanges = new ArrayList<>();
        listExchanges.add("Poloniex");
        listExchanges.add("Bittrex");
        listExchanges.add("Bleutrade");

        ArrayAdapter<String> adapterCoins = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listExchanges);

        adapterCoins.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sExchanges.setAdapter(adapterCoins);

        List<String> listFiat = new ArrayList<>();
        listFiat.add("USD");
        listFiat.add("BRL");

        ArrayAdapter<String> adapterFiat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listFiat);

        adapterFiat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sFiat.setAdapter(adapterFiat);

        prepareListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
    }

    private void prepareListeners() {
        bWidSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Context context = ConfigureWidgetActivity.this;

                DBTools db = new DBTools(context);

                try {
                    boolean saved = db.exec("insert into coin_widgets (widget_id, exchange, fiat) values (WID, 'EXCHANGE', 'FIAT')".replaceAll("EXCHANGE", sExchanges.getSelectedItem().toString()).replaceAll("FIAT", sFiat.getSelectedItem().toString()).replaceAll("WID", mAppWidgetId + ""));

                    if (saved) {
                        db.close();

                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                        RemoteViews views = new RemoteViews(context.getPackageName(),
                                R.layout.appwidget_coin);
                        appWidgetManager.updateAppWidget(mAppWidgetId, views);

                        Utils.writePreference(context, "temp_widget_coin", sExchanges.getSelectedItem().toString());

                        new CoinWidgetProvider()
                                .onUpdate(context,
                                        AppWidgetManager.getInstance(context),
                                        new int[]{mAppWidgetId}
                                );

                        Intent resultValue = new Intent();
                        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                        setResult(RESULT_OK, resultValue);
                        finish();
                    } else {
                        setResult(RESULT_CANCELED);

                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    setResult(RESULT_CANCELED);

                    finish();
                } finally {
                    db.close();
                }
            }
        });
    }
}