package altcoin.br.decred.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import altcoin.br.decred.R;
import altcoin.br.decred.adapter.AdapterLinks;
import altcoin.br.decred.model.Link;
import altcoin.br.decred.utils.Utils;

public class AboutFragment extends Fragment {
    private View view;

    private List<Link> links;
    private AdapterLinks adapterLinks;

    private TextView tvAboutDeveloper;
    private TextView tvAboutCode;
    private TextView tvDcrStatsLink;

    private LinearLayout llAboutDonate;
    private TextView tvAboutDonateWallet;
    private TextView tvAboutDonate;

    @Override
    public void onStart() {
        super.onStart();

        prepareFirebasePart();

        Utils.textViewLink(tvAboutDeveloper, "https://twitter.com/jonathanveg2");
        Utils.textViewLink(tvAboutCode, "https://github.com/JonathanVeg/decred_android");
        Utils.textViewLink(tvDcrStatsLink, "https://dcrstats.com/");
    }

    private void prepareListeners() {
        tvAboutDonateWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String wallet = tvAboutDonateWallet.getText().toString();

                    Utils.copyToClipboard(getActivity(), wallet);

                    Toast.makeText(getActivity(), "Wallet WALLET copied to clipboard".replaceAll("WALLET", wallet), Toast.LENGTH_LONG).show();

                    Utils.logFabric("donationWalletCopied");
                } catch (Exception e) {
                    e.printStackTrace();

                    Toast.makeText(getActivity(), "Error while copying wallet", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void instanceObjects() {
        ListView lvLinks = (ListView) view.findViewById(R.id.lvLinks);

        links = new ArrayList<>();

        adapterLinks = new AdapterLinks(getActivity(), links);

        lvLinks.setAdapter(adapterLinks);

        tvAboutDeveloper = (TextView) view.findViewById(R.id.tvAboutDeveloper);
        tvAboutCode = (TextView) view.findViewById(R.id.tvAboutCode);
        tvDcrStatsLink = (TextView) view.findViewById(R.id.tvDcrStatsLink);

        tvAboutDonateWallet = (TextView) view.findViewById(R.id.tvAboutDonateWallet);
        tvAboutDonate = (TextView) view.findViewById(R.id.tvAboutDonate);
        llAboutDonate = (LinearLayout) view.findViewById(R.id.llDonate);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_about, container, false);

        instanceObjects();

        prepareListeners();

        return view;
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
                    tvAboutDonateWallet.setText("DsUJTC7MZDWfnWyYnmm9P6ijsA44oRQVsSn");
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
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
