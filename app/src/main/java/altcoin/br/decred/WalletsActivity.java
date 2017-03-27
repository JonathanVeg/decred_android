package altcoin.br.decred;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class WalletsActivity extends AppCompatActivity {

    WebView wvWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallets);

        instanceObjects();

        prepareListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // wvWallet.loadUrl("https://wallet.decred.org/");
        wvWallet.loadUrl("https://google.com/");
    }

    private void instanceObjects() {
        wvWallet = (WebView) findViewById(R.id.wvWallet);
    }

    private void prepareListeners() {

    }
}
