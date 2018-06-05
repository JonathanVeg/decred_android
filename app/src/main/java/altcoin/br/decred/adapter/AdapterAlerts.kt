package altcoin.br.decred.adapter

import altcoin.br.decred.R
import altcoin.br.decred.data.DBTools
import altcoin.br.decred.model.Alert
import altcoin.br.decred.utils.Utils
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class AdapterAlerts(private val activity: Activity, private val alerts: MutableList<Alert>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private val layoutInflater =
            activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = layoutInflater.inflate(R.layout.row_alerts, parent, false)
        
        return MyViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as MyViewHolder
        
        try {
            val alert = alerts[holder.getAdapterPosition()]
            
            myHolder.tvAlertId.text = alert.id.toString()
            
            var aWhen = ""
            
            if (alert.isBittrex && !alert.isPoloniex)
                aWhen = "Trex - "
            else if (alert.isPoloniex && !alert.isBittrex)
                aWhen = "Polo - "
            else if (alert.isPoloniex && alert.isBittrex)
                aWhen = "Polo/Trex - "
            
            aWhen += alert.whenText
            
            myHolder.tvAlertWhen.text = aWhen
            myHolder.tvAlertValue.text = Utils.numberComplete(alert.value, 8)
            
            if (alert.isActive) {
                myHolder.ivAlertActive.visibility = View.VISIBLE
                myHolder.ivAlertInactive.visibility = View.GONE
            } else {
                myHolder.ivAlertInactive.visibility = View.VISIBLE
                myHolder.ivAlertActive.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    
    override fun getItemCount(): Int {
        return alerts.size
    }
    
    private inner class MyViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        internal val tvAlertId = v.findViewById(R.id.tvAlertId) as TextView
        internal val tvAlertWhen = v.findViewById(R.id.tvAlertWhen) as TextView
        internal val tvAlertValue = v.findViewById(R.id.tvAlertValue) as TextView
        internal val ivAlertDelete = v.findViewById(R.id.ivAlertDelete) as ImageView
        internal val ivAlertActive = v.findViewById(R.id.ivAlertActive) as ImageView
        internal val ivAlertInactive = v.findViewById(R.id.ivAlertInactive) as ImageView
        
        init {
            ivAlertDelete.setOnClickListener {
                AlertDialog.Builder(activity)
                        .setTitle("Confirmation")
                        .setMessage("Do you really want to remove this alert?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            val db = DBTools(activity)
                            
                            try {
                                db.exec("delete from alerts where _id = '" + alerts[adapterPosition].id + "'")
                                
                                alerts.removeAt(adapterPosition)
                                
                                notifyDataSetChanged()
                                
                                try {
                                    val obj = JSONObject()
                                    
                                    obj.put("tag", "correctListVisibility")
                                    
                                    EventBus.getDefault().post(obj)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                
                                Utils.logFabric("alertRemoved")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                db.close()
                            }
                        }
                        .setNegativeButton(android.R.string.no, null).show()
            }
            
            ivAlertActive.setOnClickListener {
                alerts[adapterPosition].isActive = false
                
                alerts[adapterPosition].save()
                
                ivAlertInactive.visibility = View.VISIBLE
                ivAlertActive.visibility = View.GONE
                
                Utils.logFabric("alertDeactivated")
            }
            
            ivAlertInactive.setOnClickListener {
                alerts[adapterPosition].isActive = true
                
                alerts[adapterPosition].save()
                
                ivAlertInactive.visibility = View.GONE
                ivAlertActive.visibility = View.VISIBLE
                
                Utils.logFabric("alertActivated")
            }
        }
    }
}