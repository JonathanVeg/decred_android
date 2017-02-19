package altcoin.br.decred;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import altcoin.br.decred.services.BalanceChangesService;
import altcoin.br.decred.services.PriceAlertService;
import altcoin.br.decred.utils.Bitcoin;
import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class MainActivity extends AppCompatActivity {

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

        loadSummary();

        loadBittrexData();

        loadPoloniexData();

        loadBleutradeData();

        loadChart();

        loadMarketChart();

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

            case R.id.share:

                try {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");

                    String shareBody = "Decred Android App. Get here: https://play.google.com/store/apps/details?id=altcoin.br.decred | Via @JonathanVeg2";
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Decred Android App");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                } catch (Exception e) {
                    e.printStackTrace();

                    Toast.makeText(this, "Error while sharing", Toast.LENGTH_LONG).show();
                }

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
    }

    private void prepareListeners() {
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

            if (Double.parseDouble(changes) >= 0)
                tvBleutradeChanges.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorChangesUp));
            else
                tvBleutradeChanges.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorChangesDown));
        }
    }
}
