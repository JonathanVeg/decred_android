package altcoin.br.decred.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import altcoin.br.decred.R;
import altcoin.br.decred.utils.Bitcoin;
import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class SummaryFragment extends Fragment {
    View view;

    boolean running;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        loadSummary();

        loadPoloniexData();

        loadBittrexData();

        loadBleutradeData();

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
                loadSummary();

                loadPoloniexData();

                loadBittrexData();

                loadBleutradeData();

                Utils.log("update ::: SummaryFragment");
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_summary, container, false);

        return view;
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

            if (!running) return;

            TextView tvSummaryBtcPrice = (TextView) view.findViewById(R.id.tvSummaryBtcPrice);
            TextView tvSummaryUsdPrice = (TextView) view.findViewById(R.id.tvSummaryUsdPrice);
            final TextView tvSummaryBrlPrice = (TextView) view.findViewById(R.id.tvSummaryBrlPrice);
            TextView tvSummaryUsd24hVolume = (TextView) view.findViewById(R.id.tvSummaryUsd24hVolume);
            TextView tvSummaryUsdMarketCap = (TextView) view.findViewById(R.id.tvSummaryUsdMarketCap);
            TextView tvSummary24hChanges = (TextView) view.findViewById(R.id.tvSummary24hChanges);

            tvSummaryBtcPrice.setText(btcPrice);
            tvSummaryUsdPrice.setText(usdPrice);
            tvSummaryUsd24hVolume.setText(usdVolume24h);
            tvSummaryUsdMarketCap.setText(usdMarketCap);
            tvSummary24hChanges.setText(String.format("%s%%", p24hChanges));

            if (Double.parseDouble(p24hChanges) >= 0)
                tvSummary24hChanges.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorChangesUp));
            else
                tvSummary24hChanges.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorChangesDown));

            Response.Listener<String> listener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject obj = new JSONObject(response).getJSONObject("ticker_24h").getJSONObject("total");

                        tvSummaryBrlPrice.setText(Utils.numberComplete(Double.parseDouble(btcPrice) * obj.getDouble("last"), 4));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            Bitcoin.convertBtcToBrl(listener);

            // tvLastUpdate.setText(Utils.now());
        }
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

            if (!running) return;

            TextView tvBittrexLast = (TextView) view.findViewById(R.id.tvBittrexLast);
            TextView tvBittrexBaseVolume = (TextView) view.findViewById(R.id.tvBittrexBaseVolume);
            TextView tvBittrexBid = (TextView) view.findViewById(R.id.tvBittrexBid);
            TextView tvBittrexAsk = (TextView) view.findViewById(R.id.tvBittrexAsk);
            TextView tvBittrexChanges = (TextView) view.findViewById(R.id.tvBittrexChanges);

            tvBittrexLast.setText(last);
            tvBittrexBaseVolume.setText(baseVolume);
            tvBittrexBid.setText(bid);
            tvBittrexAsk.setText(ask);
            tvBittrexChanges.setText(String.format("%s%%", changes));

            if (changes == null) changes = "0";

            if (Double.parseDouble(changes) >= 0)
                tvBittrexChanges.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorChangesUp));
            else
                tvBittrexChanges.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorChangesDown));
        }
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

            if (!running) return;

            TextView tvPoloniexLast = (TextView) view.findViewById(R.id.tvPoloniexLast);
            TextView tvPoloniexBaseVolume = (TextView) view.findViewById(R.id.tvPoloniexBaseVolume);
            TextView tvPoloniexBid = (TextView) view.findViewById(R.id.tvPoloniexBid);
            TextView tvPoloniexAsk = (TextView) view.findViewById(R.id.tvPoloniexAsk);
            TextView tvPoloniexChanges = (TextView) view.findViewById(R.id.tvPoloniexChanges);

            tvPoloniexLast.setText(last);
            tvPoloniexBaseVolume.setText(baseVolume);
            tvPoloniexBid.setText(bid);
            tvPoloniexAsk.setText(ask);
            tvPoloniexChanges.setText(String.format("%s%%", changes));

            if (Double.parseDouble(changes) >= 0)
                tvPoloniexChanges.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorChangesUp));
            else
                tvPoloniexChanges.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorChangesDown));
        }
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

            if (!running) return;

            TextView tvBleutradeLast = (TextView) view.findViewById(R.id.tvBleutradeLast);
            TextView tvBleutradeBaseVolume = (TextView) view.findViewById(R.id.tvBleutradeBaseVolume);
            TextView tvBleutradeBid = (TextView) view.findViewById(R.id.tvBleutradeBid);
            TextView tvBleutradeAsk = (TextView) view.findViewById(R.id.tvBleutradeAsk);
            TextView tvBleutradeChanges = (TextView) view.findViewById(R.id.tvBleutradeChanges);

            tvBleutradeLast.setText(last);
            tvBleutradeBaseVolume.setText(baseVolume);
            tvBleutradeBid.setText(bid);
            tvBleutradeAsk.setText(ask);
            tvBleutradeChanges.setText(String.format("%s%%", changes));

            if (changes != null && Double.parseDouble(changes) >= 0)
                tvBleutradeChanges.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorChangesUp));
            else
                tvBleutradeChanges.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorChangesDown));
        }
    }
}
