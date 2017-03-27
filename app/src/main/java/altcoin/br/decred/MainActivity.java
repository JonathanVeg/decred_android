package altcoin.br.decred;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import altcoin.br.decred.adapter.AdapterLinks;
import altcoin.br.decred.model.Link;
import altcoin.br.decred.services.BalanceChangesService;
import altcoin.br.decred.services.PriceAlertService;
import altcoin.br.decred.utils.Bitcoin;
import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class MainActivity extends Activity {

    private Handler handler;

    private TextView tvLastUpdate;

    // parte dos graficos

    private Spinner sZoom;
    private Spinner sCandle;

    private CheckBox cbShowValues;

    private int chartZoom;
    private int chartCandle;
    private boolean showValues;

    private CandleStickChart coinChart;

    private LineChart marketChartBid;
    private LineChart marketChartAsk;

    private ArrayAdapter<String> adapterZoom;
    private ArrayAdapter<String> adapterCandle;

    // footer

    ImageView bSummary;
    ImageView bChart;
    ImageView bCalculator;
    ImageView bAbout;

    LinearLayout llSummary;
    LinearLayout llChart;
    LinearLayout llCalculator;
    LinearLayout llAbout;

    // calculator
    private Button bConvertBrlTo;
    private Button bConvertBtcTo;
    private Button bConvertUsdTo;
    private Button bConvertDcrTo;

    private EditText etValueToConvertBrl;
    private EditText etValueToConvertBtc;
    private EditText etValueToConvertUsd;
    private EditText etValueToConvertDcr;

    private TextView tvCalcBrlInDcr;
    private TextView tvCalcBtcInDcr;
    private TextView tvCalcUsdInDcr;
    private TextView tvCalcDcrInBrl;
    private TextView tvCalcDcrInBtc;
    private TextView tvCalcDcrInUsd;

    // about
    private static String TAG = "Decred AboutActivity";

    private List<Link> links;
    private AdapterLinks adapterLinks;

    private TextView tvAboutDeveloper;
    private TextView tvAboutCode;

    private LinearLayout llAboutDonate;
    private TextView tvAboutDonateWallet;
    private TextView tvAboutDonate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getActionBar() != null) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setTitle("Decred");
        }

        instanceObjects();

        prepareListeners();

        prepareLinks();

        prepareFirebasePart();

        loadSummary();

        loadPoloniexData();

        loadBittrexData();

        loadBleutradeData();

        loadChart();

        loadMarketChart();

        resetFooter();

        bSummary.performClick();

        startService(new Intent(this, BalanceChangesService.class));
        startService(new Intent(this, PriceAlertService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // creating the handler for updating the altcoin.br.decred.data constantily
        try {

            handler = new Handler();

            handler.postDelayed(runnableCode, 10000);

        } catch (Exception e) {
            Log.e("Handler", "Error while creating handler");

            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // creating the handler for updating the altcoin.br.decred.data constantily
        try {

            handler.removeCallbacks(runnableCode);

        } catch (Exception e) {
            Log.e("Handler", "Error while pausing handler");

            e.printStackTrace();
        }
    }

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            tvLastUpdate.setText(" ... ");

            loadSummary();

            loadBittrexData();

            loadPoloniexData();

            loadChart();

            loadMarketChart();

            // after executing it creates another instance
            // i think there is a way to make it better.
            handler = new Handler();

            handler.postDelayed(runnableCode, 10000);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.calculator:
                startActivity(new Intent(this, CalculatorActivity.class));

                return true;

            case R.id.alerts:
                startActivity(new Intent(this, AlertActivity.class));

                return true;

            case R.id.wallets:
                startActivity(new Intent(this, WalletsActivity.class));

                return true;

            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadMarketChart() {
        String url = "https://poloniex.com/public?command=returnOrderBook&currencyPair=BTC_DCR&depth=750";

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                new atParseMarketChart(response).execute();
            }
        };

        InternetRequests internetRequests = new InternetRequests();
        internetRequests.executePost(url, listener);
    }

    private class atParseMarketChart extends AsyncTask<Void, Void, Void> {
        String response;

        ArrayList<Entry> entriesBid;
        ArrayList<Entry> entriesAsk;

        ArrayList<String> labelsBid;
        ArrayList<String> labelsAsk;

        atParseMarketChart(String response) {
            this.response = response;

            entriesBid = new ArrayList<>();
            entriesAsk = new ArrayList<>();

            labelsBid = new ArrayList<>();
            labelsAsk = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject jObject = new JSONObject(response);

                Iterator<?> keys = jObject.keys();

                JSONArray internal;

                while (keys.hasNext()) {
                    String key = (String) keys.next();

                    if (jObject.get(key) instanceof JSONArray) {

                        if (key.equals("bids")) {
                            internal = jObject.getJSONArray(key);

                            double totalAsk = 0;

                            for (int i = 0; i < internal.length(); i++) {
                                JSONArray item = internal.getJSONArray(i);

                                totalAsk += item.getDouble(0) * item.getDouble(1);

                                entriesBid.add(new Entry((float) totalAsk, i));
                                labelsBid.add(item.getString(0));
                            }
                        }

                        if (key.equals("asks")) {
                            internal = jObject.getJSONArray(key);

                            double totalBid = 0;

                            for (int i = 0; i < internal.length(); i++) {
                                JSONArray item = internal.getJSONArray(i);

                                totalBid += item.getDouble(0) * item.getDouble(1);

                                entriesAsk.add(new Entry((float) totalBid, i));
                                labelsAsk.add(item.getString(0));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // bid

            // invert the data
            for (int i = 0; i < entriesBid.size(); i++)
                entriesBid.get(i).setXIndex(entriesBid.size() - 1 - i);

            Collections.reverse(labelsBid);

            LineDataSet datasetBid = new LineDataSet(entriesBid, "Bids");

            datasetBid.setColor(0xFF00FF00);

            datasetBid.setDrawValues(false);

            datasetBid.setFillColor(0xFF00FF00);

            datasetBid.setDrawCircles(false);

            datasetBid.setDrawFilled(true);

            LineData lineDataBid = new LineData(labelsBid, datasetBid);

            marketChartBid.setData(lineDataBid);

            marketChartBid.getAxisRight().setDrawLabels(false);

            marketChartBid.setDescription("");

            marketChartBid.notifyDataSetChanged();

            marketChartBid.invalidate();

            // ask

            LineDataSet datasetAsk = new LineDataSet(entriesAsk, "Asks");

            datasetAsk.setColor(0xFFFF0000);

            datasetAsk.setDrawValues(false);

            datasetAsk.setFillColor(0xFFFF0000);

            datasetAsk.setDrawCircles(false);

            datasetAsk.setDrawFilled(true);

            LineData lineDataAsk = new LineData(labelsAsk, datasetAsk);

            marketChartAsk.setData(lineDataAsk);

            marketChartAsk.getAxisRight().setDrawLabels(false);

            marketChartAsk.setDescription("");

            marketChartAsk.notifyDataSetChanged();

            marketChartAsk.invalidate();
        }
    }

    private void loadChart() {
        String url = "https://poloniex.com/public?" +
                "command=returnChartData" +
                "&currencyPair=BTC_DCR" +
                "&start=" + (Utils.timestampLong() - 60 * chartZoom * 60) +
                "&period=" + chartCandle;

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                new atParseCandleJson(response).execute();
            }
        };

        InternetRequests internetRequests = new InternetRequests();
        internetRequests.executePost(url, listener);
    }

    private class atParseCandleJson extends AsyncTask<Void, Void, Void> {
        String response;

        CandleData data;
        ArrayList<CandleEntry> entries = new ArrayList<>();

        atParseCandleJson(String response) {
            this.response = response;
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONArray arr = new JSONArray(response);

                JSONObject obj;

                List<String> labels = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    obj = arr.getJSONObject(i);

                    entries.add(new CandleEntry(i, (float) obj.getDouble("high"),
                            (float) obj.getDouble("low"), (float) obj.getDouble("open"), (float) obj.getDouble("close")));

                    labels.add(i + "");
                }

                CandleDataSet dataset = new CandleDataSet(entries, "");
                dataset.setIncreasingColor(0xFF00FF00);
                dataset.setDecreasingColor(0xFFFF0000);
                dataset.setDecreasingPaintStyle(Paint.Style.FILL);
                dataset.setShadowColor(0xFF0000FF);
                dataset.setDrawValues(showValues);

                data = new CandleData(labels, dataset);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            YAxis yAxis = coinChart.getAxisLeft();

            yAxis.setStartAtZero(false);

            coinChart.setData(data);

            coinChart.getAxisRight().setDrawLabels(false);

            coinChart.setDescription("");

            coinChart.notifyDataSetChanged();

            coinChart.invalidate();
        }
    }

    private void instanceObjects() {
        tvLastUpdate = (TextView) findViewById(R.id.tvLastUpdate);

        TextView tvOficialSite = (TextView) findViewById(R.id.tvOficialSite);
        TextView tvPoloniexTitle = (TextView) findViewById(R.id.tvPoloniexTitle);
        TextView tvBittrexTitle = (TextView) findViewById(R.id.tvBittrexTitle);
        TextView tvBleutradeTitle = (TextView) findViewById(R.id.tvBleutradeTitle);
        TextView tvCoinMarketCapTitle = (TextView) findViewById(R.id.tvCoinMarketCapTitle);

        Utils.textViewLink(tvOficialSite, "https://decred.info/");
        Utils.textViewLink(tvPoloniexTitle, "https://coinmarketcap.com/exchanges/poloniex/");
        Utils.textViewLink(tvBittrexTitle, "https://coinmarketcap.com/exchanges/bittrex/");
        Utils.textViewLink(tvBleutradeTitle, "https://coinmarketcap.com/exchanges/bleutrade/");
        Utils.textViewLink(tvCoinMarketCapTitle, "https://coinmarketcap.com/currencies/decred/#markets");

        // parte dos graficos

        coinChart = (CandleStickChart) findViewById(R.id.coinChart);

        sZoom = (Spinner) findViewById(R.id.sZoom);
        sCandle = (Spinner) findViewById(R.id.sCandle);

        cbShowValues = (CheckBox) findViewById(R.id.cbShowValues);

        chartZoom = 3;
        chartCandle = 30 * 60;

        showValues = false;

        List<String> zoom = new ArrayList<>();
        List<String> candle = new ArrayList<>();

        zoom.add("3h");
        zoom.add("6h");
        zoom.add("24h");
        zoom.add("2d");
        zoom.add("1w");
        zoom.add("2w");
        zoom.add("1m");

        candle.add("5-min");
        candle.add("15-min");
        candle.add("30-min");
        candle.add("120-min");
        candle.add("240-min");

        adapterZoom = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, zoom);
        adapterZoom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sZoom.setAdapter(adapterZoom);

        adapterCandle = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, candle);
        adapterCandle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCandle.setAdapter(adapterCandle);

        sCandle.setSelection(2);
        sZoom.setSelection(1);

        // market chart

        marketChartBid = (LineChart) findViewById(R.id.marketChartBid);
        marketChartAsk = (LineChart) findViewById(R.id.marketChartAsk);

        // footer

        bSummary = (ImageView) findViewById(R.id.bSummary);
        bChart = (ImageView) findViewById(R.id.bChart);
        bCalculator = (ImageView) findViewById(R.id.bCalculator);
        bAbout = (ImageView) findViewById(R.id.bAbout);

        llSummary = (LinearLayout) findViewById(R.id.llSummary);
        llChart = (LinearLayout) findViewById(R.id.llChart);
        llCalculator = (LinearLayout) findViewById(R.id.llCalculator);
        llAbout = (LinearLayout) findViewById(R.id.llAbout);

        instanceObjectsCalculator();

        instanceObjectsAbout();
    }

    private void instanceObjectsAbout() {
        ListView lvLinks = (ListView) findViewById(R.id.lvLinks);

        links = new ArrayList<>();

        adapterLinks = new AdapterLinks(this, links);

        lvLinks.setAdapter(adapterLinks);

        tvAboutDeveloper = (TextView) findViewById(R.id.tvAboutDeveloper);
        tvAboutCode = (TextView) findViewById(R.id.tvAboutCode);

        tvAboutDonateWallet = (TextView) findViewById(R.id.tvAboutDonateWallet);
        tvAboutDonate = (TextView) findViewById(R.id.tvAboutDonate);
        llAboutDonate = (LinearLayout) findViewById(R.id.llDonate);
    }

    private void instanceObjectsCalculator() {
        bConvertBrlTo = (Button) findViewById(R.id.bConvertBrlTo);
        bConvertBtcTo = (Button) findViewById(R.id.bConvertBtcTo);
        bConvertUsdTo = (Button) findViewById(R.id.bConvertUsdTo);
        bConvertDcrTo = (Button) findViewById(R.id.bConvertDcrTo);

        etValueToConvertBrl = (EditText) findViewById(R.id.etValueToConvertBrl);
        etValueToConvertBtc = (EditText) findViewById(R.id.etValueToConvertBtc);
        etValueToConvertUsd = (EditText) findViewById(R.id.etValueToConvertUsd);
        etValueToConvertDcr = (EditText) findViewById(R.id.etValueToConvertDcr);

        tvCalcBrlInDcr = (TextView) findViewById(R.id.tvCalcBrlInDcr);
        tvCalcBtcInDcr = (TextView) findViewById(R.id.tvCalcBtcInDcr);
        tvCalcUsdInDcr = (TextView) findViewById(R.id.tvCalcUsdInDcr);
        tvCalcDcrInBrl = (TextView) findViewById(R.id.tvCalcDcrInBrl);
        tvCalcDcrInBtc = (TextView) findViewById(R.id.tvCalcDcrInBtc);
        tvCalcDcrInUsd = (TextView) findViewById(R.id.tvCalcDcrInUsd);

        // load in the lasts values used

        etValueToConvertBrl.setText(Utils.readPreference(this, "etValueToConvertBrl", "0"));
        etValueToConvertBtc.setText(Utils.readPreference(this, "etValueToConvertBtc", "0"));
        etValueToConvertUsd.setText(Utils.readPreference(this, "etValueToConvertUsd", "0"));
        etValueToConvertDcr.setText(Utils.readPreference(this, "etValueToConvertDcr", "0"));
    }

    private void prepareListeners() {
        prepareListenersCalculator();

        prepareListenersAbout();

        prepareMenuListeners();

        // parte dos graficos

        sZoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String item = adapterZoom.getItem(position);

                if (item == null) return;

                switch (item) {
                    case "3h":
                        chartZoom = 3;
                        break;
                    case "6h":
                        chartZoom = 6;
                        break;
                    case "24h":
                        chartZoom = 24;
                        break;
                    case "2d":
                        chartZoom = 48;
                        break;
                    case "1w":
                        chartZoom = 24 * 7;
                        break;
                    case "2w":
                        chartZoom = 24 * 7 * 2;
                        break;
                    case "1m":
                        chartZoom = 24 * 30;
                        break;

                    default:
                        chartZoom = 3;
                }

                loadChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        sCandle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (adapterCandle.getItem(position) == null) return;

                String item = adapterCandle.getItem(position).split("-")[0];
                switch (item) {
                    case "5":
                        chartCandle = 5 * 60;
                        break;
                    case "15":
                        chartCandle = 15 * 60;
                        break;
                    case "30":
                        chartCandle = 30 * 60;
                        break;
                    case "120":
                        chartCandle = 120 * 60;
                        break;
                    case "240":
                        chartCandle = 240 * 60;
                        break;

                    default:
                        chartCandle = 5 * 60;
                }

                loadChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        cbShowValues.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                showValues = b;

                loadChart();
            }
        });
    }

    private void prepareFirebasePart() {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference showWallet = database.getReference("donation").child("show_wallet");

            // Read from the database
            showWallet.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Boolean value = dataSnapshot.getValue(Boolean.class);

                    if (value)
                        llAboutDonate.setVisibility(View.VISIBLE);
                    else
                        llAboutDonate.setVisibility(View.GONE);

                    showWallet.keepSynced(true);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            final DatabaseReference wallet = database.getReference("donation").child("wallet");

            // Read from the database
            wallet.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class);

                    tvAboutDonateWallet.setText(value);

                    wallet.keepSynced(true);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            final DatabaseReference title = database.getReference("donation").child("title");

            // Read from the database
            title.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class);

                    tvAboutDonate.setText(value);

                    wallet.keepSynced(true);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            // links
            final DatabaseReference drLinks = database.getReference("links");

            // Read from the database
            drLinks.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    try {
                        String value = dataSnapshot.getValue(String.class);

                        List<Link> localLinks = new ArrayList<>();

                        String[] arrLinks = value.split(",");

                        for (int i = 0; i < arrLinks.length; i += 2) {
                            localLinks.add(new Link(arrLinks[i], arrLinks[i + 1]));
                        }

                        links.clear();

                        links.addAll(localLinks);

                        adapterLinks.notifyDataSetChanged();

                        drLinks.keepSynced(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void prepareListenersAbout() {
        tvAboutDonateWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String wallet = tvAboutDonateWallet.getText().toString();

                    Utils.copyToClipboard(MainActivity.this, wallet);

                    Toast.makeText(MainActivity.this, "Wallet WALLET copied to clipboard".replaceAll("WALLET", wallet), Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    e.printStackTrace();

                    Toast.makeText(MainActivity.this, "Error while copying wallet", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void prepareListenersCalculator() {
        bConvertBtcTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyEditTextNull(etValueToConvertBtc)) {
                    hideKeyboard();

                    Utils.writePreference(MainActivity.this, "etValueToConvertBtc", etValueToConvertBtc.getText().toString());

                    Response.Listener<String> listener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {

                                JSONObject obj = new JSONArray(response).getJSONObject(0);

                                double quantity = Double.parseDouble(etValueToConvertBtc.getText().toString());

                                tvCalcBtcInDcr.setText(Utils.numberComplete(String.format("%s", quantity / Double.parseDouble(obj.getString("price_btc"))), 8));

                            } catch (Exception e) {
                                e.printStackTrace();

                                Toast.makeText(MainActivity.this, "Error while converting", Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    execApiCall(listener);
                }
            }
        });

        bConvertUsdTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyEditTextNull(etValueToConvertUsd)) {
                    hideKeyboard();

                    Utils.writePreference(MainActivity.this, "etValueToConvertUsd", etValueToConvertUsd.getText().toString());

                    Response.Listener<String> listener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {

                                JSONObject obj = new JSONArray(response).getJSONObject(0);

                                double quantity = Double.parseDouble(etValueToConvertUsd.getText().toString());

                                tvCalcUsdInDcr.setText(Utils.numberComplete(String.format("%s", quantity / Double.parseDouble(obj.getString("price_usd"))), 8));

                            } catch (Exception e) {
                                e.printStackTrace();

                                Toast.makeText(MainActivity.this, "Error while converting", Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    execApiCall(listener);
                }
            }
        });

        bConvertBrlTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyEditTextNull(etValueToConvertBrl)) {
                    hideKeyboard();

                    Utils.writePreference(MainActivity.this, "etValueToConvertBrl", etValueToConvertBrl.getText().toString());

                    Response.Listener<String> listener2 = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject obj = new JSONObject(response);

                                final double quantity = Double.parseDouble(etValueToConvertBrl.getText().toString()) / obj.getDouble("last");

                                Response.Listener<String> listener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {

                                            JSONObject obj = new JSONArray(response).getJSONObject(0);

                                            tvCalcBrlInDcr.setText(Utils.numberComplete(String.format("%s", quantity / Double.parseDouble(obj.getString("price_btc"))), 4));

                                        } catch (Exception e) {
                                            e.printStackTrace();

                                            Toast.makeText(MainActivity.this, "Error while converting", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                };

                                execApiCall(listener);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    Bitcoin.convertBtcToBrl(listener2);
                }
            }
        });

        bConvertDcrTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyEditTextNull(etValueToConvertDcr)) {
                    hideKeyboard();

                    Utils.writePreference(MainActivity.this, "etValueToConvertDcr", etValueToConvertDcr.getText().toString());

                    Response.Listener<String> listener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {

                                final JSONObject obj = new JSONArray(response).getJSONObject(0);

                                final double quantity = Double.parseDouble(etValueToConvertDcr.getText().toString());

                                tvCalcDcrInBtc.setText(Utils.numberComplete(String.format("%s", quantity * Double.parseDouble(obj.getString("price_btc"))), 8));

                                tvCalcDcrInUsd.setText(Utils.numberComplete(String.format("%s", quantity * Double.parseDouble(obj.getString("price_usd"))), 4));

                                Response.Listener<String> listener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject obj2 = new JSONObject(response);

                                            tvCalcDcrInBrl.setText(Utils.numberComplete(Double.parseDouble(obj.getString("price_btc")) * obj2.getDouble("last") * quantity, 4));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };

                                Bitcoin.convertBtcToBrl(listener);

                            } catch (Exception e) {
                                e.printStackTrace();

                                Toast.makeText(MainActivity.this, "Error while converting", Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    execApiCall(listener);
                }
            }
        });

    }

    private void loadBittrexData() {
        String url = "https://bittrex.com/api/v1.1/public/getmarketsummary?market=BTC-DCR";

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                new atParseBittrexData(response).execute();
            }
        };

        InternetRequests internetRequests = new InternetRequests();
        internetRequests.executePost(url, listener);
    }

    private void loadPoloniexData() {
        String url = "https://poloniex.com/public?command=returnTicker";

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                new atParsePoloniexData(response).execute();
            }
        };

        InternetRequests internetRequests = new InternetRequests();
        internetRequests.executePost(url, listener);
    }

    private void loadBleutradeData() {
        String url = "https://bleutrade.com/api/v2/public/getmarketsummary?market=DCR_BTC";

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                new atParseBleutradeData(response).execute();
            }
        };

        InternetRequests internetRequests = new InternetRequests();
        internetRequests.executePost(url, listener);
    }

    private void loadSummary() {
        String url = "https://api.coinmarketcap.com/v1/ticker/decred/";

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                new atParseSummaryData(response).execute();
            }
        };

        InternetRequests internetRequests = new InternetRequests();
        internetRequests.executeGet(url, listener);
    }

    private class atParseBittrexData extends AsyncTask<Void, Void, Void> {
        String response;

        String last;
        String baseVolume;
        String ask;
        String bid;
        String changes;

        atParseBittrexData(String response) {
            this.response = response;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                JSONObject obj = new JSONObject(response);

                if (obj.getBoolean("success")) {
                    obj = obj.getJSONArray("result").getJSONObject(0);

                    last = Utils.numberComplete(obj.getString("Last"), 8);
                    baseVolume = Utils.numberComplete(obj.getString("BaseVolume"), 8);
                    ask = Utils.numberComplete(obj.getString("Ask"), 8);
                    bid = Utils.numberComplete(obj.getString("Bid"), 8);

                    // the api does not give the % changes, but we can calculate it using the prevDay and last values
                    Double prev = obj.getDouble("PrevDay");

                    double c = (prev - Double.parseDouble(last)) / prev * (-100);

                    changes = Utils.numberComplete("" + c, 2);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            TextView tvBittrexLast = (TextView) findViewById(R.id.tvBittrexLast);
            TextView tvBittrexBaseVolume = (TextView) findViewById(R.id.tvBittrexBaseVolume);
            TextView tvBittrexBid = (TextView) findViewById(R.id.tvBittrexBid);
            TextView tvBittrexAsk = (TextView) findViewById(R.id.tvBittrexAsk);
            TextView tvBittrexChanges = (TextView) findViewById(R.id.tvBittrexChanges);

            tvBittrexLast.setText(last);
            tvBittrexBaseVolume.setText(baseVolume);
            tvBittrexBid.setText(bid);
            tvBittrexAsk.setText(ask);
            tvBittrexChanges.setText(String.format("%s%%", changes));

            if (changes == null) changes = "0";

            if (Double.parseDouble(changes) >= 0)
                tvBittrexChanges.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorChangesUp));
            else
                tvBittrexChanges.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorChangesDown));
        }
    }

    private class atParsePoloniexData extends AsyncTask<Void, Void, Void> {
        String response;

        String last;
        String baseVolume;
        String ask;
        String bid;
        String changes;

        atParsePoloniexData(String response) {
            this.response = response;
        }

        JSONObject getSpecificSummary(String response) {
            try {
                String coin = "DCR";

                JSONObject jObject = new JSONObject(response);

                Iterator<?> keys = jObject.keys();

                JSONObject jsonObj;

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (jObject.get(key) instanceof JSONObject) {
                        jsonObj = (JSONObject) jObject.get(key);

                        if (key.startsWith("BTC_") && key.toLowerCase().contains(coin.toLowerCase())) {

                            return jsonObj;

                        }
                    }
                }

                return null;
            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                JSONObject obj = getSpecificSummary(response);

                last = Utils.numberComplete(obj.getString("last"), 8);
                baseVolume = Utils.numberComplete(obj.getString("baseVolume"), 8);
                ask = Utils.numberComplete(obj.getString("lowestAsk"), 8);
                bid = Utils.numberComplete(obj.getString("highestBid"), 8);
                changes = Utils.numberComplete(obj.getDouble("percentChange") * 100, 2);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            TextView tvPoloniexLast = (TextView) findViewById(R.id.tvPoloniexLast);
            TextView tvPoloniexBaseVolume = (TextView) findViewById(R.id.tvPoloniexBaseVolume);
            TextView tvPoloniexBid = (TextView) findViewById(R.id.tvPoloniexBid);
            TextView tvPoloniexAsk = (TextView) findViewById(R.id.tvPoloniexAsk);
            TextView tvPoloniexChanges = (TextView) findViewById(R.id.tvPoloniexChanges);

            tvPoloniexLast.setText(last);
            tvPoloniexBaseVolume.setText(baseVolume);
            tvPoloniexBid.setText(bid);
            tvPoloniexAsk.setText(ask);
            tvPoloniexChanges.setText(String.format("%s%%", changes));

            if (Double.parseDouble(changes) >= 0)
                tvPoloniexChanges.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorChangesUp));
            else
                tvPoloniexChanges.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorChangesDown));
        }
    }

    private class atParseSummaryData extends AsyncTask<Void, Void, Void> {
        String response;

        String usdPrice;
        String btcPrice;
        String usdVolume24h;
        String p24hChanges;
        String usdMarketCap;

        atParseSummaryData(String response) {
            this.response = response;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                JSONObject obj = new JSONArray(response).getJSONObject(0);

                usdPrice = Utils.numberComplete(obj.getString("price_usd"), 4);
                btcPrice = Utils.numberComplete(obj.getString("price_btc"), 8);
                p24hChanges = Utils.numberComplete(obj.getString("percent_change_24h"), 2);
                usdVolume24h = Utils.numberComplete(obj.getString("24h_volume_usd"), 4);
                usdMarketCap = Utils.numberComplete(obj.getString("market_cap_usd"), 4);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            TextView tvSummaryBtcPrice = (TextView) findViewById(R.id.tvSummaryBtcPrice);
            TextView tvSummaryUsdPrice = (TextView) findViewById(R.id.tvSummaryUsdPrice);
            final TextView tvSummaryBrlPrice = (TextView) findViewById(R.id.tvSummaryBrlPrice);
            TextView tvSummaryUsd24hVolume = (TextView) findViewById(R.id.tvSummaryUsd24hVolume);
            TextView tvSummaryUsdMarketCap = (TextView) findViewById(R.id.tvSummaryUsdMarketCap);
            TextView tvSummary24hChanges = (TextView) findViewById(R.id.tvSummary24hChanges);

            tvSummaryBtcPrice.setText(btcPrice);
            tvSummaryUsdPrice.setText(usdPrice);
            tvSummaryUsd24hVolume.setText(usdVolume24h);
            tvSummaryUsdMarketCap.setText(usdMarketCap);
            tvSummary24hChanges.setText(String.format("%s%%", p24hChanges));

            if (Double.parseDouble(p24hChanges) >= 0)
                tvSummary24hChanges.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorChangesUp));
            else
                tvSummary24hChanges.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorChangesDown));

            Response.Listener<String> listener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject obj = new JSONObject(response);

                        tvSummaryBrlPrice.setText(Utils.numberComplete(Double.parseDouble(btcPrice) * obj.getDouble("last"), 4));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            Bitcoin.convertBtcToBrl(listener);

            tvLastUpdate.setText(Utils.now());
        }
    }

    private class atParseBleutradeData extends AsyncTask<Void, Void, Void> {
        String response;

        String last;
        String baseVolume;
        String ask;
        String bid;
        String changes;

        atParseBleutradeData(String response) {
            this.response = response;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                JSONObject obj = new JSONObject(response).getJSONArray("result").getJSONObject(0);

                last = Utils.numberComplete(obj.getString("Last"), 8);
                baseVolume = Utils.numberComplete(obj.getString("BaseVolume"), 8);
                ask = Utils.numberComplete(obj.getString("Ask"), 8);
                bid = Utils.numberComplete(obj.getString("Bid"), 8);

                Double prev = obj.getDouble("PrevDay");

                double c = (prev - Double.parseDouble(last)) / prev * (-100);

                changes = Utils.numberComplete("" + c, 2);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            TextView tvBleutradeLast = (TextView) findViewById(R.id.tvBleutradeLast);
            TextView tvBleutradeBaseVolume = (TextView) findViewById(R.id.tvBleutradeBaseVolume);
            TextView tvBleutradeBid = (TextView) findViewById(R.id.tvBleutradeBid);
            TextView tvBleutradeAsk = (TextView) findViewById(R.id.tvBleutradeAsk);
            TextView tvBleutradeChanges = (TextView) findViewById(R.id.tvBleutradeChanges);

            tvBleutradeLast.setText(last);
            tvBleutradeBaseVolume.setText(baseVolume);
            tvBleutradeBid.setText(bid);
            tvBleutradeAsk.setText(ask);
            tvBleutradeChanges.setText(String.format("%s%%", changes));

            if (changes != null && Double.parseDouble(changes) >= 0)
                tvBleutradeChanges.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorChangesUp));
            else
                tvBleutradeChanges.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorChangesDown));
        }
    }

    private void resetFooter() {
        bSummary.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver));
        bChart.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver));
        bCalculator.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver));
        bAbout.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver));

        llSummary.setVisibility(View.GONE);
        llChart.setVisibility(View.GONE);
        llCalculator.setVisibility(View.GONE);
        llAbout.setVisibility(View.GONE);
    }

    private void prepareMenuListeners() {
        bSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFooter();

                bSummary.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.silver));

                llSummary.setVisibility(View.VISIBLE);
            }
        });

        bChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFooter();

                bChart.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.silver));

                llChart.setVisibility(View.VISIBLE);
            }
        });

        bCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFooter();

                bCalculator.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.silver));

                llCalculator.setVisibility(View.VISIBLE);
            }
        });

        bAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFooter();

                bAbout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.silver));

                llAbout.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean verifyEditTextNull(EditText et) {
        if (et.getText().toString().equals("")) {
            Toast.makeText(this, "You need to fill the box", Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    private void hideKeyboard() {
        try {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void execApiCall(Response.Listener<String> listener) {

        String url = "https://api.coinmarketcap.com/v1/ticker/decred/";

        InternetRequests internetRequests = new InternetRequests();

        internetRequests.executeGet(url, listener);

    }

    private void prepareLinks() {
        Utils.textViewLink(tvAboutDeveloper, "https://twitter.com/jonathanveg2");
        Utils.textViewLink(tvAboutCode, "https://github.com/JonathanVeg/decred_android");
    }
}
