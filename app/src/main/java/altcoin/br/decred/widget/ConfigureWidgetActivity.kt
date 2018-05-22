package altcoin.br.decred.widget

import altcoin.br.decred.R
import altcoin.br.decred.data.DBTools
import altcoin.br.decred.utils.Utils
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.RemoteViews
import kotlinx.android.synthetic.main.activity_configure_widget.*
import java.util.*

class ConfigureWidgetActivity : AppCompatActivity() {
    
    private var mAppWidgetId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_widget)
        
        val listExchanges = ArrayList<String>()
        listExchanges.add("Poloniex")
        listExchanges.add("Bittrex")
        listExchanges.add("Bleutrade")
        
        val adapterCoins = ArrayAdapter(this, android.R.layout.simple_spinner_item, listExchanges)
        
        adapterCoins.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        sWidExchanges?.adapter = adapterCoins
        
        val listFiat = ArrayList<String>()
        listFiat.add("USD")
        listFiat.add("BRL")
        
        val adapterFiat = ArrayAdapter(this, android.R.layout.simple_spinner_item, listFiat)
        
        adapterFiat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        sWidFiat?.adapter = adapterFiat
        
        prepareListeners()
    }
    
    override fun onStart() {
        super.onStart()
        
        val intent = intent
        val extras = intent.extras
        
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }
    }
    
    private fun prepareListeners() {
        bWidSave?.setOnClickListener {
            val context = this@ConfigureWidgetActivity
            
            val db = DBTools(context)
            
            try {
                val saved = db.exec("insert into coin_widgets (widget_id, exchange, fiat) values (WID, 'EXCHANGE', 'FIAT')".replace("EXCHANGE".toRegex(), sWidExchanges?.selectedItem.toString()).replace("FIAT".toRegex(), sWidFiat?.selectedItem.toString()).replace("WID".toRegex(), mAppWidgetId.toString() + ""))
                
                if (saved) {
                    db.close()
                    
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    
                    val views = RemoteViews(context.packageName,
                            R.layout.appwidget_coin)
                    appWidgetManager.updateAppWidget(mAppWidgetId, views)
                    
                    Utils.writePreference(context, "temp_widget_coin", sWidExchanges?.selectedItem.toString())
                    
                    PriceWidgetProvider().onUpdate(context,
                            AppWidgetManager.getInstance(context),
                            intArrayOf(mAppWidgetId)
                    )
                    
                    val resultValue = Intent()
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                } else {
                    setResult(Activity.RESULT_CANCELED)
                    
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                
                setResult(Activity.RESULT_CANCELED)
                
                finish()
            } finally {
                db.close()
            }
        }
    }
}