package altcoin.br.decred.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import altcoin.br.decred.R;
import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class ChartFragment extends Fragment {
    private View view;

    private int chartZoom;
    private int chartCandle;
    private boolean showValues;

    private CandleStickChart coinChart;

    private LineChart marketChartBid;
    private LineChart marketChartAsk;

    private Spinner sZoom;
    private Spinner sCandle;

    private CheckBox cbShowValues;

    private ArrayAdapter<String> adapterZoom;
    private ArrayAdapter<String> adapterCandle;

    private boolean running;

    @Override
    public void onStart() {
        super.onStart();

        loadMarketChart();

        running = true;

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void eventBusReceiver(JSONObject obj) {
        try {
            if (obj.has("tag") && obj.getString("tag").equalsIgnoreCase("update") && running) {
                loadMarketChart();

                Utils.log("update ::: ChartFragment");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        running = false;
    }

    private void prepareListeners() {
        sZoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String item = adapterZoom.getItem(position);

                if (item == null) return;

                Utils.writePreference(getActivity(), "chartZoom", item);

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
                String item = adapterCandle.getItem(position);

                if (item == null)
                    return;

                Utils.log("sCandle changed ::: " + item);

                Utils.writePreference(getActivity(), "chartCandle", item);

                item = item.split("-")[0];

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

    private void instanceObjects() {
        coinChart = (CandleStickChart) view.findViewById(R.id.coinChart);

        sZoom = (Spinner) view.findViewById(R.id.sZoom);
        sCandle = (Spinner) view.findViewById(R.id.sCandle);

        cbShowValues = (CheckBox) view.findViewById(R.id.cbShowValues);

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

        adapterZoom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, zoom);
        adapterZoom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sZoom.setAdapter(adapterZoom);

        adapterCandle = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, candle);
        adapterCandle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCandle.setAdapter(adapterCandle);

        sZoom.setSelection(adapterZoom.getPosition(Utils.readPreference(getActivity(), "chartZoom", "3h")));
        sCandle.setSelection(adapterCandle.getPosition(Utils.readPreference(getActivity(), "chartCandle", "15-min")));

        // market chart

        marketChartBid = (LineChart) view.findViewById(R.id.marketChartBid);
        marketChartAsk = (LineChart) view.findViewById(R.id.marketChartAsk);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chart, container, false);

        instanceObjects();

        prepareListeners();

        return view;
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
        final String response;

        CandleData data;
        final ArrayList<CandleEntry> entries = new ArrayList<>();

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

            if (!running) return;

            YAxis yAxis = coinChart.getAxisLeft();

            yAxis.setStartAtZero(false);

            coinChart.setData(data);

            coinChart.getAxisRight().setDrawLabels(false);

            coinChart.setDescription("");

            coinChart.notifyDataSetChanged();

            coinChart.invalidate();
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
        final String response;

        final ArrayList<Entry> entriesBid;
        final ArrayList<Entry> entriesAsk;

        final ArrayList<String> labelsBid;
        final ArrayList<String> labelsAsk;

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

            if (!running) return;

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
}
