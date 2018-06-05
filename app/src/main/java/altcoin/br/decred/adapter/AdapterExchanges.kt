package altcoin.br.decred.adapter

import altcoin.br.decred.R
import altcoin.br.decred.model.ExchangeData
import altcoin.br.decred.utils.exchanges.AbstractExchange
import altcoin.br.decred.utils.hide
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView

class AdapterExchanges(private val context: Context, private val exchanges: ArrayList<ExchangeData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = layoutInflater.inflate(R.layout.row_exchanges, parent, false)
        
        return MyViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val h = holder as MyViewHolder
        
        try {
            val exchange = exchanges[h.adapterPosition].exchange
            
            h.tvExchangeTitle.text = exchange.toString()
            
            object : AbstractExchange(exchange) {
                override fun onValueLoaded() {
                    try {
                        h.tvExchangeLast.text = last
                        h.tvExchangeBaseVolume.text = baseVolume
                        h.tvExchangeBid.text = bid
                        h.tvExchangeAsk.text = ask
                        h.tvExchangeChanges.text = String.format("%s%%", changes)
                        
                        if (java.lang.Double.parseDouble(changes) >= 0)
                            h.tvExchangeChanges.setTextColor(ContextCompat.getColor(context, R.color.colorChangesUp))
                        else
                            h.tvExchangeChanges.setTextColor(ContextCompat.getColor(context, R.color.colorChangesDown))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

//            val db = DBTools(context)
//
//            try {
//                h.cbPinPriceInNotifications.isChecked = db.search("select count(*) from pinned_notifications where exchange = '${exchange.label.toLowerCase()}'") > 0
//            } finally {
//                db.close()
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun getItemId(position: Int) =
            position.toLong()
    
    override fun getItemCount() =
            exchanges.size
    
    private inner class MyViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        internal val tvExchangeTitle = v.findViewById(R.id.tvExchangeTitle) as TextView
        internal val tvExchangeLast = v.findViewById(R.id.tvExchangeLast) as TextView
        internal val tvExchangeBaseVolume = v.findViewById(R.id.tvExchangeBaseVolume) as TextView
        internal val tvExchangeBid = v.findViewById(R.id.tvExchangeBid) as TextView
        internal val tvExchangeAsk = v.findViewById(R.id.tvExchangeAsk) as TextView
        internal val tvExchangeChanges = v.findViewById(R.id.tvExchangeChanges) as TextView
        internal val cbPinPriceInNotifications = v.findViewById(R.id.cbPinPriceInNotifications) as CheckBox
        
        init {
            
            cbPinPriceInNotifications.hide()

//            cbPinPriceInNotifications.setOnCheckedChangeListener { _, b ->
//                val db = DBTools(context)
//
//                try {
//                    if (b)
//                        db.exec("insert into pinned_notifications (exchange) values('${exchanges[adapterPosition].toString().toLowerCase()}')")
//                    else
//                        db.exec("delete from pinned_notifications where exchange = '${exchanges[adapterPosition].toString().toLowerCase()}'")
//                } finally {
//                    db.close()
//                }
//            }
        }
    }
}