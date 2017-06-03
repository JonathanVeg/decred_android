package altcoin.br.decred;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import altcoin.br.decred.fragments.AboutFragment;
import altcoin.br.decred.fragments.AlertsFragment;
import altcoin.br.decred.fragments.CalculatorFragment;
import altcoin.br.decred.fragments.ChartFragment;
import altcoin.br.decred.fragments.StatsFragment;
import altcoin.br.decred.fragments.SummaryFragment;
import altcoin.br.decred.services.PriceAlertService;
import altcoin.br.decred.utils.Utils;

public class MainActivity extends Activity {

    private int TAB_SUMMARY = 0;
    private int TAB_CHART = 1;
    private int TAB_CALC = 2;
    private int TAB_ALERT = 3;
    private int TAB_STATS = 4;
    private int TAB_ABOUT = 5;

    private int currentTab = 0;

    private Handler handler;

    private TextView tvLastUpdate;

    private ImageView bSummary;
    private ImageView bChart;
    private ImageView bCalculator;
    private ImageView bAbout;
    private ImageView bAlerts;
    private ImageView bStats;

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

        resetFooter();

        bSummary.performClick();

        startService(new Intent(this, PriceAlertService.class));
    }

    @Override
    public void onBackPressed() {
        if (currentTab != TAB_SUMMARY) {
            bSummary.performClick();

            return;
        }

        super.onBackPressed();
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

            handler = new Handler();

            handler.postDelayed(runnableCode, 10000);
        }
    };

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

        // footer

        bSummary = (ImageView) findViewById(R.id.bSummary);
        bChart = (ImageView) findViewById(R.id.bChart);
        bCalculator = (ImageView) findViewById(R.id.bCalculator);
        bAbout = (ImageView) findViewById(R.id.bAbout);
        bStats = (ImageView) findViewById(R.id.bStats);
        bAlerts = (ImageView) findViewById(R.id.bAlerts);
    }

    private void prepareListeners() {
        bSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTab(TAB_SUMMARY);
            }
        });

        bChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTab(TAB_CHART);
            }
        });

        bCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTab(TAB_CALC);
            }
        });

        bAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTab(TAB_ABOUT);
            }
        });

        bAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTab(TAB_ALERT);
            }
        });

        bStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTab(TAB_STATS);
            }
        });
    }

    private void resetFooter() {
        bSummary.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver));
        bChart.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver));
        bCalculator.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver));
        bAbout.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver));
        bStats.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver));
        bAlerts.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_silver));
    }

    private void changeTab(int tab) {
        FragmentManager fm = getFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

        if (tab == TAB_SUMMARY) {
            resetFooter();

            currentTab = TAB_SUMMARY;

            bSummary.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.silver));

            SummaryFragment fragment = new SummaryFragment();

            ft.replace(R.id.llFragments, fragment, "task").commit();

            Utils.logFabric("tabChanged", "tab", "summary");
        } else if (tab == TAB_CHART) {
            resetFooter();

            currentTab = TAB_CHART;

            bChart.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.silver));

            ChartFragment fragment = new ChartFragment();

            ft.replace(R.id.llFragments, fragment, "task").commit();

            Utils.logFabric("tabChanged", "tab", "chart");
        } else if (tab == TAB_CALC) {
            resetFooter();

            currentTab = TAB_CALC;

            bCalculator.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.silver));

            CalculatorFragment fragment = new CalculatorFragment();

            ft.replace(R.id.llFragments, fragment, "task").commit();

            Utils.logFabric("tabChanged", "tab", "calculator");
        } else if (tab == TAB_ABOUT) {
            resetFooter();

            currentTab = TAB_ABOUT;

            bAbout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.silver));

            AboutFragment fragment = new AboutFragment();

            ft.replace(R.id.llFragments, fragment, "task").commit();

            Utils.logFabric("tabChanged", "tab", "about");
        } else if (tab == TAB_ALERT) {
            resetFooter();

            currentTab = TAB_ALERT;

            bAlerts.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.silver));

            AlertsFragment fragment = new AlertsFragment();

            ft.replace(R.id.llFragments, fragment, "task").commit();

            Utils.logFabric("tabChanged", "tab", "alerts");
        } else if (tab == TAB_STATS) {
            resetFooter();

            currentTab = TAB_STATS;

            bStats.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.silver));

            StatsFragment fragment = new StatsFragment();

            ft.replace(R.id.llFragments, fragment, "task").commit();

            Utils.logFabric("tabChanged", "tab", "stats");
        }

    }
}
