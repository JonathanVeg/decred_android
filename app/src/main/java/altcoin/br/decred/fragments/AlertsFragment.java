package altcoin.br.decred.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import altcoin.br.decred.R;
import altcoin.br.decred.adapter.AdapterAlerts;
import altcoin.br.decred.data.DBTools;
import altcoin.br.decred.model.Alert;
import altcoin.br.decred.services.PriceAlertService;
import altcoin.br.decred.utils.Utils;

public class AlertsFragment extends Fragment {
    private View view;

    private CheckBox cbAlertPoloniex;
    private CheckBox cbAlertBittrex;
    private Spinner sOptions;
    private EditText etValue;
    private Button bSaveAlert;
    private AdapterAlerts adapterAlerts;
    private List<Alert> alerts;
    private RelativeLayout rlNoAlerts;
    private LinearLayout llCurrentAlerts;
    private RecyclerView rvAlerts;

    private boolean running;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        new atLoadAlerts(getActivity()).execute();

        running = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        running = false;
    }

    // listener do eventBus
    @Subscribe
    @SuppressWarnings("unused")
    public void eventBusReceiver(JSONObject obj) {
        try {
            if (obj.has("tag")) {
                if (obj.getString("tag").equalsIgnoreCase("correctListVisibility")) {
                    correctListVisibility();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareListeners() {
        bSaveAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    hideKeyboard();

                    Alert alert = new Alert(getActivity());

                    alert.setWhen(sOptions.getSelectedItemPosition() == 0 ? Alert.GREATER : Alert.LOWER);

                    alert.setValue(etValue.getText().toString());

                    alert.setBittrex(cbAlertBittrex.isChecked());

                    alert.setPoloniex(cbAlertPoloniex.isChecked());

                    alert.setActive(true);

                    if (alert.save()) {
                        Utils.alert(getActivity(), "Alert saved");

                        new atLoadAlerts(getActivity()).execute();

                        getActivity().stopService(new Intent(getActivity(), PriceAlertService.class));

                        getActivity().startService(new Intent(getActivity(), PriceAlertService.class));

                        String where = "";

                        if (alert.isPoloniex()) where += "P";

                        if (alert.isBittrex()) where += "B";

                        Utils.logFabric("alertSaved", "where", where);
                    } else
                        Utils.alert(getActivity(), "Error while saving alert");
                } catch (Exception e) {
                    e.printStackTrace();

                    Utils.alert(getActivity(), "Error while saving alert");
                }
            }
        });
    }

    private void instanceObjects() {
        sOptions = (Spinner) view.findViewById(R.id.sOptions);
        etValue = (EditText) view.findViewById(R.id.etValue);
        bSaveAlert = (Button) view.findViewById(R.id.bSaveAlert);
        rlNoAlerts = (RelativeLayout) view.findViewById(R.id.rlNoAlerts);
        llCurrentAlerts = (LinearLayout) view.findViewById(R.id.llCurrentAlerts);
        cbAlertPoloniex = (CheckBox) view.findViewById(R.id.cbAlertPoloniex);
        cbAlertBittrex = (CheckBox) view.findViewById(R.id.cbAlertBittrex);

        alerts = new ArrayList<>();

        adapterAlerts = new AdapterAlerts(getActivity(), alerts);

        rvAlerts = (RecyclerView) view.findViewById(R.id.rvAlerts);
        rvAlerts.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvAlerts.setLayoutManager(linearLayoutManager);
        rvAlerts.setAdapter(adapterAlerts);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragments_alerts, container, false);

        instanceObjects();

        prepareListeners();

        return view;
    }

    class atLoadAlerts extends io.fabric.sdk.android.services.concurrency.AsyncTask<Void, Void, Void> {
        final Context context;

        final List<Alert> list;

        atLoadAlerts(Context context) {
            this.context = context;

            list = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DBTools db = new DBTools(context);

            try {
                int count = db.search("select * from alerts order by created_at desc");

                Alert alert;

                for (int i = 0; i < count; i++) {
                    alert = new Alert(getActivity());

                    alert.setId(db.getData(i, 0));
                    alert.setWhen(db.getData(i, 1));
                    alert.setValue(db.getData(i, 2));
                    alert.setCreatedAt(db.getData(i, 3));
                    alert.setActive(Utils.isTrue(db.getData(i, 4)));
                    alert.setPoloniex(Utils.isTrue(db.getData(i, 5)));
                    alert.setBittrex(Utils.isTrue(db.getData(i, 6)));

                    list.add(alert);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!running) return;

            alerts.clear();

            alerts.addAll(list);

            adapterAlerts.notifyDataSetChanged();

            correctListVisibility();
        }
    }

    private void correctListVisibility() {
        if (alerts.size() > 0) {
            rlNoAlerts.setVisibility(View.GONE);
            llCurrentAlerts.setVisibility(View.VISIBLE);
            rvAlerts.setVisibility(View.VISIBLE);
        } else {
            llCurrentAlerts.setVisibility(View.GONE);
            rvAlerts.setVisibility(View.GONE);
            rlNoAlerts.setVisibility(View.VISIBLE);
        }
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
