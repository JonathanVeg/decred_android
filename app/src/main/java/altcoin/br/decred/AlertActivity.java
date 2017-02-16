package altcoin.br.decred;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import altcoin.br.decred.adapter.AdapterAlerts;
import altcoin.br.decred.data.DBTools;
import altcoin.br.decred.model.Alert;
import altcoin.br.decred.utils.Utils;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class AlertActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        instanceObjects();

        prepareListeners();

        new atLoadAlerts(this).execute();
    }

    private void instanceObjects() {
        rvAlerts = (RecyclerView) findViewById(R.id.rvAlerts);
        sOptions = (Spinner) findViewById(R.id.sOptions);
        etValue = (EditText) findViewById(R.id.etValue);
        bSaveAlert = (Button) findViewById(R.id.bSaveAlert);
        rlNoAlerts = (RelativeLayout) findViewById(R.id.rlNoAlerts);
        llCurrentAlerts = (LinearLayout) findViewById(R.id.llCurrentAlerts);
        cbAlertPoloniex = (CheckBox) findViewById(R.id.cbAlertPoloniex);
        cbAlertBittrex = (CheckBox) findViewById(R.id.cbAlertBittrex);

        alerts = new ArrayList<>();

        adapterAlerts = new AdapterAlerts(this, alerts);

        rvAlerts.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvAlerts.setLayoutManager(linearLayoutManager);
        rvAlerts.setAdapter(adapterAlerts);
    }

    private void prepareListeners() {
        bSaveAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    hideKeyboard();

                    Alert alert = new Alert(AlertActivity.this);

                    alert.setWhen(sOptions.getSelectedItemPosition() == 0 ? Alert.GREATER : Alert.LOWER);

                    alert.setValue(etValue.getText().toString());

                    alert.setBittrex(cbAlertBittrex.isChecked());

                    alert.setPoloniex(cbAlertPoloniex.isChecked());

                    alert.setActive(true);

                    if (alert.save()) {
                        Utils.alert(AlertActivity.this, "Alert saved");

                        new atLoadAlerts(AlertActivity.this).execute();
                    } else
                        Utils.alert(AlertActivity.this, "Error while saving alert");
                } catch (Exception e) {
                    e.printStackTrace();

                    Utils.alert(AlertActivity.this, "Error while saving alert");
                }
            }
        });
    }

    class atLoadAlerts extends AsyncTask<Void, Void, Void> {
        Context context;

        List<Alert> list;

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
                    alert = new Alert(AlertActivity.this);

                    alert.setId(db.getData(i, 0));
                    alert.setWhen(db.getData(i, 1));
                    alert.setValue(db.getData(i, 2));
                    alert.setCreatedAt(db.getData(i, 3));
                    alert.setActive(Utils.isTrue(db.getData(i, 4)));

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

            alerts.clear();

            alerts.addAll(list);

            adapterAlerts.notifyDataSetChanged();

            correctListVisibility();
        }
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

    public void correctListVisibility() {
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
}
