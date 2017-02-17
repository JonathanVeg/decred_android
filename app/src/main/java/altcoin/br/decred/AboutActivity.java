package altcoin.br.decred;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import altcoin.br.decred.adapter.AdapterLinks;
import altcoin.br.decred.model.Link;
import altcoin.br.decred.utils.Utils;

public class AboutActivity extends AppCompatActivity {
    private static String TAG = "Decred AboutActivity";

    private ListView lvLinks;
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
        setContentView(R.layout.activity_about);

        instanceObjects();

        prepareLinks();

        prepareFirebasePart();

        prepareListeners();
    }

    private void instanceObjects() {
        lvLinks = (ListView) findViewById(R.id.lvLinks);
        links = new ArrayList<>();

        adapterLinks = new AdapterLinks(this, links);

        lvLinks.setAdapter(adapterLinks);

        tvAboutDeveloper = (TextView) findViewById(R.id.tvAboutDeveloper);
        tvAboutCode = (TextView) findViewById(R.id.tvAboutCode);

        tvAboutDonateWallet = (TextView) findViewById(R.id.tvAboutDonateWallet);
        tvAboutDonate = (TextView) findViewById(R.id.tvAboutDonate);
        llAboutDonate = (LinearLayout) findViewById(R.id.llDonate);
    }

    private void prepareLinks() {
        Utils.textViewLink(tvAboutDeveloper, "https://twitter.com/jonathanveg2");
        Utils.textViewLink(tvAboutCode, "https://github.com/JonathanVeg/decred_android");
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

    private void prepareListeners() {
        tvAboutDonateWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String wallet = tvAboutDonateWallet.getText().toString();

                    Utils.copyToClipboard(AboutActivity.this, wallet);

                    Toast.makeText(AboutActivity.this, "Wallet WALLET copied to clipboard".replaceAll("WALLET", wallet), Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    e.printStackTrace();

                    Toast.makeText(AboutActivity.this, "Error while copying wallet", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
