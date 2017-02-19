package altcoin.br.decred.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import altcoin.br.decred.AlertActivity;
import altcoin.br.decred.R;
import altcoin.br.decred.data.DBTools;
import altcoin.br.decred.model.Alert;
import altcoin.br.decred.utils.Utils;

public class AdapterAlerts extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater layoutInflater;
    private List<Alert> alerts;

    private Activity activity;

    public AdapterAlerts(Activity activity, List<Alert> alerts) {
        this.activity = activity;

        this.alerts = alerts;

        this.layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.row_alerts, parent, false);

        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        myViewHolder myHolder = ((myViewHolder) holder);

        try {
            Alert alert = alerts.get(holder.getAdapterPosition());

            myHolder.tvAlertId.setText(String.valueOf(alert.getId()));
            myHolder.tvAlertWhen.setText(String.valueOf(alert.getWhenText()));
            myHolder.tvAlertValue.setText(Utils.numberComplete(alert.getValue(), 8));

            if (alert.isActive()) {
                myHolder.ivAlertActive.setVisibility(View.VISIBLE);
                myHolder.ivAlertInactive.setVisibility(View.GONE);
            } else {
                myHolder.ivAlertInactive.setVisibility(View.VISIBLE);
                myHolder.ivAlertActive.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    private class myViewHolder extends RecyclerView.ViewHolder {

        TextView tvAlertId;
        TextView tvAlertCoin;
        TextView tvAlertWhen;
        TextView tvAlertValue;
        ImageView ivAlertDelete;
        ImageView ivAlertActive;
        ImageView ivAlertInactive;

        myViewHolder(View v) {
            super(v);

            tvAlertId = (TextView) v.findViewById(R.id.tvAlertId);
            tvAlertWhen = (TextView) v.findViewById(R.id.tvAlertWhen);
            tvAlertValue = (TextView) v.findViewById(R.id.tvAlertValue);
            ivAlertDelete = (ImageView) v.findViewById(R.id.ivAlertDelete);
            ivAlertActive = (ImageView) v.findViewById(R.id.ivAlertActive);
            ivAlertInactive = (ImageView) v.findViewById(R.id.ivAlertInactive);

            prepareListeners();
        }

        void prepareListeners() {
            ivAlertDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(activity)
                            .setTitle("Confirmation")
                            .setMessage("Do you really want to remove this alert?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    DBTools db = new DBTools(activity);

                                    try {
                                        db.exec("delete from alerts where _id = '" + alerts.get(getAdapterPosition()).getId() + "'");

                                        alerts.remove(getAdapterPosition());

                                        notifyDataSetChanged();

                                        ((AlertActivity) activity).correctListVisibility();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        db.close();
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();

                }
            });

            ivAlertActive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alerts.get(getAdapterPosition()).setActive(false);

                    alerts.get(getAdapterPosition()).save();

                    ivAlertInactive.setVisibility(View.VISIBLE);
                    ivAlertActive.setVisibility(View.GONE);
                }
            });

            ivAlertInactive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alerts.get(getAdapterPosition()).setActive(true);

                    alerts.get(getAdapterPosition()).save();

                    ivAlertInactive.setVisibility(View.GONE);
                    ivAlertActive.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}