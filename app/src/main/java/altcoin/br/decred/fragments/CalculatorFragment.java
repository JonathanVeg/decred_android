package altcoin.br.decred.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import altcoin.br.decred.R;
import altcoin.br.decred.utils.Bitcoin;
import altcoin.br.decred.utils.InternetRequests;
import altcoin.br.decred.utils.Utils;

public class CalculatorFragment extends Fragment {
    View view;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void prepareListeners() {
        bConvertBtcTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyEditTextNull(etValueToConvertBtc)) {
                    Utils.logFabric("calculator", "operation", "btcTo");

                    hideKeyboard();

                    Utils.writePreference(getActivity(), "etValueToConvertBtc", etValueToConvertBtc.getText().toString());

                    Response.Listener<String> listener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {

                                JSONObject obj = new JSONArray(response).getJSONObject(0);

                                double quantity = Double.parseDouble(etValueToConvertBtc.getText().toString());

                                tvCalcBtcInDcr.setText(Utils.numberComplete(String.format("%s", quantity / Double.parseDouble(obj.getString("price_btc"))), 8));

                            } catch (Exception e) {
                                e.printStackTrace();

                                Toast.makeText(getActivity(), "Error while converting", Toast.LENGTH_LONG).show();
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
                    Utils.logFabric("calculator", "operation", "usdTo");

                    hideKeyboard();

                    Utils.writePreference(getActivity(), "etValueToConvertUsd", etValueToConvertUsd.getText().toString());

                    Response.Listener<String> listener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {

                                JSONObject obj = new JSONArray(response).getJSONObject(0);

                                double quantity = Double.parseDouble(etValueToConvertUsd.getText().toString());

                                tvCalcUsdInDcr.setText(Utils.numberComplete(String.format("%s", quantity / Double.parseDouble(obj.getString("price_usd"))), 8));

                            } catch (Exception e) {
                                e.printStackTrace();

                                Toast.makeText(getActivity(), "Error while converting", Toast.LENGTH_LONG).show();
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
                    Utils.logFabric("calculator", "operation", "brlTo");

                    hideKeyboard();

                    Utils.writePreference(getActivity(), "etValueToConvertBrl", etValueToConvertBrl.getText().toString());

                    Response.Listener<String> listener2 = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject obj = new JSONObject(response).getJSONObject("ticker_24h").getJSONObject("total");

                                final double quantity = Double.parseDouble(etValueToConvertBrl.getText().toString()) / obj.getDouble("last");

                                Response.Listener<String> listener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {

                                            JSONObject obj = new JSONArray(response).getJSONObject(0);

                                            tvCalcBrlInDcr.setText(Utils.numberComplete(String.format("%s", quantity / Double.parseDouble(obj.getString("price_btc"))), 4));

                                        } catch (Exception e) {
                                            e.printStackTrace();

                                            Toast.makeText(getActivity(), "Error while converting", Toast.LENGTH_LONG).show();
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
                    Utils.logFabric("calculator", "operation", "dcrTo");

                    hideKeyboard();

                    Utils.writePreference(getActivity(), "etValueToConvertDcr", etValueToConvertDcr.getText().toString());

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
                                            JSONObject obj2 = new JSONObject(response).getJSONObject("ticker_24h").getJSONObject("total");

                                            tvCalcDcrInBrl.setText(Utils.numberComplete(Double.parseDouble(obj.getString("price_btc")) * obj2.getDouble("last") * quantity, 4));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };

                                Bitcoin.convertBtcToBrl(listener);

                            } catch (Exception e) {
                                e.printStackTrace();

                                Toast.makeText(getActivity(), "Error while converting", Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    execApiCall(listener);
                }
            }
        });

    }

    private void instanceObjects() {
        bConvertBrlTo = (Button) view.findViewById(R.id.bConvertBrlTo);
        bConvertBtcTo = (Button) view.findViewById(R.id.bConvertBtcTo);
        bConvertUsdTo = (Button) view.findViewById(R.id.bConvertUsdTo);
        bConvertDcrTo = (Button) view.findViewById(R.id.bConvertDcrTo);

        etValueToConvertBrl = (EditText) view.findViewById(R.id.etValueToConvertBrl);
        etValueToConvertBtc = (EditText) view.findViewById(R.id.etValueToConvertBtc);
        etValueToConvertUsd = (EditText) view.findViewById(R.id.etValueToConvertUsd);
        etValueToConvertDcr = (EditText) view.findViewById(R.id.etValueToConvertDcr);

        tvCalcBrlInDcr = (TextView) view.findViewById(R.id.tvCalcBrlInDcr);
        tvCalcBtcInDcr = (TextView) view.findViewById(R.id.tvCalcBtcInDcr);
        tvCalcUsdInDcr = (TextView) view.findViewById(R.id.tvCalcUsdInDcr);
        tvCalcDcrInBrl = (TextView) view.findViewById(R.id.tvCalcDcrInBrl);
        tvCalcDcrInBtc = (TextView) view.findViewById(R.id.tvCalcDcrInBtc);
        tvCalcDcrInUsd = (TextView) view.findViewById(R.id.tvCalcDcrInUsd);

        // load in the lasts values used

        etValueToConvertBrl.setText(Utils.readPreference(getActivity(), "etValueToConvertBrl", "0"));
        etValueToConvertBtc.setText(Utils.readPreference(getActivity(), "etValueToConvertBtc", "0"));
        etValueToConvertUsd.setText(Utils.readPreference(getActivity(), "etValueToConvertUsd", "0"));
        etValueToConvertDcr.setText(Utils.readPreference(getActivity(), "etValueToConvertDcr", "0"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calculator, container, false);

        instanceObjects();

        prepareListeners();

        return view;
    }

    private boolean verifyEditTextNull(EditText et) {
        if (et.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "You need to fill the box", Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    private void execApiCall(Response.Listener<String> listener) {
        String url = "https://api.coinmarketcap.com/v1/ticker/decred/";

        InternetRequests internetRequests = new InternetRequests();

        internetRequests.executeGet(url, listener);
    }

    private void hideKeyboard() {
        try {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
