package altcoin.br.decred.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import altcoin.br.decred.R;
import altcoin.br.decred.adapter.bitcoin.FiatPriceFiat;
import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class StatsFragment extends Fragment {
    View view;

    private long statsTimeStamp = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        loadStatsData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_stats, container, false);

        return view;
    }

    private void loadStatsData() {
        statsTimeStamp = Utils.timestampLong();

        String url = "https://dcrstats.com/api/v1/get_stats?" + statsTimeStamp;

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                new atParseStatsData(response).execute();

                String url2 = "https://dcrstats.com/api/v1/fees";

                Response.Listener<String> listener2 = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        new atParseStatsBlockData(getActivity(), response).execute();
                    }
                };

                InternetRequests internetRequests = new InternetRequests();

                internetRequests.executeGet(url2, listener2);
            }
        };

        InternetRequests internetRequests = new InternetRequests();

        internetRequests.executeGet(url, listener);
    }

    private class atParseStatsBlockData extends AsyncTask<Void, Void, Void> {
        Context context;
        String response;

        String statsLastBlockNumber = "";

        atParseStatsBlockData(Context c, String r) {
            context = c;

            response = r;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONArray arr = new JSONArray(response);

                statsLastBlockNumber = arr.getJSONObject(0).getString("height");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            TextView tvStatsLastBlockNumber = (TextView) getActivity().findViewById(R.id.tvStatsLastBlockNumber);

            tvStatsLastBlockNumber.setText(statsLastBlockNumber);
        }
    }

    private class atParseStatsData extends AsyncTask<Void, Void, Void> {
        String response;

        String statsTicketPrice = ""; // done
        String statsNextTicketPrice = ""; // done
        String statsMinTicketPrice = ""; // done
        String statsMaxTicketPrice = ""; // done

        double statsTicketPriceUsd = 0; 
        double statsNextTicketPriceUsd = 0; 
        double statsMinTicketPriceUsd = 0; 
        double statsMaxTicketPriceUsd = 0; 

        String statsLastBlockDatetime = ""; // done
        String statsVoteReward = ""; // done

        String statsLastAvgBlockTime = ""; // done
        String statsLastAvgHashrate = ""; // done
        String statsStakeReward = ""; // done
        String statsAdjustIn = ""; // done
        String statsAvailableSupply = "";
        String statsPowReward = ""; // done
        String statsTicketPollSize = ""; // done
        String statsLockedDcr = ""; // done
        String statsAvgTicketPrice = ""; // done

        atParseStatsData(String r) {
            response = r;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject obj = new JSONObject(response);

                statsTicketPrice = Utils.numberComplete(obj.getString("sbits"), 2);
                statsNextTicketPrice = Utils.numberComplete(obj.getString("est_sbits"), 2);
                statsMinTicketPrice = Utils.numberComplete(obj.getString("est_sbits_min"), 2);
                statsMaxTicketPrice = Utils.numberComplete(obj.getString("est_sbits_max"), 2);
                statsPowReward = Utils.numberComplete(obj.getString("pow_reward"), 2);
                statsAdjustIn = obj.getString("pos_adjustment");
                statsStakeReward = Utils.numberComplete(obj.getString("block_reward"), 2);
                statsVoteReward = Utils.numberComplete(obj.getString("vote_reward"), 2);
                statsLockedDcr = obj.getString("ticketpoolvalue");
                statsAvgTicketPrice = Utils.numberComplete(obj.getString("avg_ticket_price"), 2);

                new FiatPriceFiat(getActivity(), "USD"){
                    @Override
                    public void onValueLoaded() {
                        super.onValueLoaded();

                        
                    }
                };

                Long timestamp = statsTimeStamp - obj.getLong("last_block_datetime");

                String min = (timestamp / 60) + "";
                String sec = (timestamp % 60) + "";

                if (min.length() == 1)
                    min = "0" + min;

                if (sec.length() == 1)
                    sec = "0" + sec;

                statsLastBlockDatetime = min + ":" + sec;
                statsLastAvgBlockTime = obj.getString("average_minutes") + ":" + obj.getString("average_seconds");

                statsTicketPollSize = obj.getString("poolsize");

                statsLastAvgHashrate = Utils.numberComplete(obj.getLong("networkhashps") / (1000000000000.0), 2);

                statsAvailableSupply = String.valueOf(Utils.numberComplete(obj.getLong("coinsupply") / 1000000.0, 0));



            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            TextView tvStatsTicketPrice = (TextView) getActivity().findViewById(R.id.tvStatsTicketPrice);
            TextView tvStatsTicketPriceUsd = (TextView) getActivity().findViewById(R.id.tvStatsTicketPriceUsd);

            TextView tvStatsNextTicketPrice = (TextView) getActivity().findViewById(R.id.tvStatsNextTicketPrice);
            TextView tvStatsNextTicketPriceUsd = (TextView) getActivity().findViewById(R.id.tvStatsNextTicketPriceUsd);

            TextView tvStatsMinTicketPrice = (TextView) getActivity().findViewById(R.id.tvStatsMinTicketPrice);
            TextView tvStatsMinTicketPriceUsd = (TextView) getActivity().findViewById(R.id.tvStatsMinTicketPriceUsd);

            TextView tvStatsMaxTicketPrice = (TextView) getActivity().findViewById(R.id.tvStatsMaxTicketPrice);
            TextView tvStatsMaxTicketPriceUsd = (TextView) getActivity().findViewById(R.id.tvStatsMaxTicketPriceUsd);

            TextView tvStatsLastBlockDatetime = (TextView) getActivity().findViewById(R.id.tvStatsLastBlockDatetime);

            TextView tvStatsLastAvgBlockTime = (TextView) getActivity().findViewById(R.id.tvStatsLastAvgBlockTime);
            TextView tvStatsLastAvgHashrate = (TextView) getActivity().findViewById(R.id.tvStatsLastAvgHashrate);
            TextView tvStatsStakeReward = (TextView) getActivity().findViewById(R.id.tvStatsStakeReward);
            TextView tvStatsAdjustIn = (TextView) getActivity().findViewById(R.id.tvStatsAdjustIn);
            TextView tvStatsAvailableSupply = (TextView) getActivity().findViewById(R.id.tvStatsAvailableSupply);
            TextView tvStatsPowReward = (TextView) getActivity().findViewById(R.id.tvStatsPowReward);
            TextView tvStatsTicketPollSize = (TextView) getActivity().findViewById(R.id.tvStatsTicketPollSize);
            TextView tvStatsLockedDcr = (TextView) getActivity().findViewById(R.id.tvStatsLockedDcr);
            TextView tvStatsAvgTicketPrice = (TextView) getActivity().findViewById(R.id.tvStatsAvgTicketPrice);
            TextView tvStatsVoteReward = (TextView) getActivity().findViewById(R.id.tvStatsVoteReward);

            tvStatsTicketPrice.setText(statsTicketPrice);
            tvStatsNextTicketPrice.setText(statsNextTicketPrice);
            tvStatsMinTicketPrice.setText(statsMinTicketPrice);
            tvStatsMaxTicketPrice.setText(statsMaxTicketPrice);
            tvStatsLastBlockDatetime.setText(statsLastBlockDatetime);
            tvStatsLastAvgBlockTime.setText(statsLastAvgBlockTime + " min");
            tvStatsLastAvgHashrate.setText(statsLastAvgHashrate + " TH/s");
            tvStatsStakeReward.setText(statsStakeReward);
            tvStatsAdjustIn.setText(statsAdjustIn + " blocks");
            tvStatsAvailableSupply.setText(statsAvailableSupply);
            tvStatsPowReward.setText(statsPowReward);
            tvStatsTicketPollSize.setText(statsTicketPollSize);
            tvStatsLockedDcr.setText(statsLockedDcr);
            tvStatsAvgTicketPrice.setText(statsAvgTicketPrice);
            tvStatsVoteReward.setText(statsVoteReward);

            tvStatsLastBlockDatetime.setText(statsLastBlockDatetime);
        }
    }
}
