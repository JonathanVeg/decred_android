package altcoin.br.decred.fragments

import altcoin.br.decred.R
import altcoin.br.decred.adapter.AdapterAlerts
import altcoin.br.decred.data.DBTools
import altcoin.br.decred.model.Alert
import altcoin.br.decred.services.PriceAlertService
import altcoin.br.decred.utils.Utils
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.fragments_alerts.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject

class AlertsFragment : Fragment() {
    private var adapterAlerts: AdapterAlerts? = null
    private var alerts: ArrayList<Alert> = ArrayList()
    
    private var running: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            EventBus.getDefault().register(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onStart() {
        super.onStart()
        
        AtLoadAlerts(activity).execute()
        
        running = true
    }
    
    override fun onStop() {
        super.onStop()
        
        running = false
    }
    
    // listener do eventBus
    @Subscribe
    fun eventBusReceiver(obj: JSONObject) {
        try {
            if (obj.has("tag")) {
                if (obj.getString("tag").equals("correctListVisibility", ignoreCase = true)) {
                    correctListVisibility()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun prepareListeners() {
        bSaveAlert?.setOnClickListener {
            try {
                hideKeyboard()
                
                val alert = Alert(activity)
                
                alert.`when` = if (sOptions?.selectedItemPosition == 0) Alert.GREATER else Alert.LOWER
                
                alert.value = etValue?.text.toString()
                
                alert.isBittrex = cbAlertBittrex?.isChecked ?: false
                
                alert.isPoloniex = cbAlertPoloniex?.isChecked ?: false
                
                alert.isActive = true
                
                if (alert.save()) {
                    Utils.alert(activity, "Alert saved")
                    
                    AtLoadAlerts(activity).execute()
                    
                    activity.stopService(Intent(activity, PriceAlertService::class.java))
                    
                    activity.startService(Intent(activity, PriceAlertService::class.java))
                    
                    var where = ""
                    
                    if (alert.isPoloniex) where += "P"
                    
                    if (alert.isBittrex) where += "B"
                    
                    Utils.logFabric("alertSaved", "where", where)
                } else
                    Utils.alert(activity, "Error while saving alert")
            } catch (e: Exception) {
                e.printStackTrace()
                
                Utils.alert(activity, "Error while saving alert")
            }
        }
    }
    
    private fun instanceObjects() {
        alerts = ArrayList()
        
        adapterAlerts = AdapterAlerts(activity, alerts)
        
        rvAlerts.setHasFixedSize(true)
        
        // use a linear layout manager
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        
        rvAlerts.layoutManager = linearLayoutManager
        rvAlerts.adapter = adapterAlerts
    }
    
    internal inner class AtLoadAlerts(val context: Context) : io.fabric.sdk.android.services.concurrency.AsyncTask<Void?, Void?, Void?>() {
        val list: MutableList<Alert> = ArrayList()
        override fun doInBackground(vararg voids: Void?): Void? {
            val db = DBTools(context)
            
            try {
                val count = db.search("select * from alerts order by created_at desc")
                
                var alert: Alert
                
                for (i in 0 until count) {
                    alert = Alert(activity)
                    
                    alert.setId(db.getData(i, 0))
                    alert.setWhen(db.getData(i, 1))
                    alert.value = db.getData(i, 2)
                    alert.setCreatedAt(db.getData(i, 3))
                    alert.isActive = Utils.isTrue(db.getData(i, 4))
                    alert.isPoloniex = Utils.isTrue(db.getData(i, 5))
                    alert.isBittrex = Utils.isTrue(db.getData(i, 6))
                    
                    list.add(alert)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                db.close()
            }
            
            return null
        }
        
        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            
            if (!running) return
            
            alerts.clear()
            
            alerts.addAll(list)
            
            adapterAlerts?.notifyDataSetChanged()
            
            correctListVisibility()
        }
    }
    
    private fun correctListVisibility() {
        if (alerts.size > 0) {
            rlNoAlerts.visibility = View.GONE
            llCurrentAlerts.visibility = View.VISIBLE
            rvAlerts.visibility = View.VISIBLE
        } else {
            llCurrentAlerts.visibility = View.GONE
            rvAlerts.visibility = View.GONE
            rlNoAlerts.visibility = View.VISIBLE
        }
    }
    
    private fun hideKeyboard() {
        try {
            val view = activity.currentFocus
            if (view != null) {
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragments_alerts, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        instanceObjects()
        
        prepareListeners()
    }
}
