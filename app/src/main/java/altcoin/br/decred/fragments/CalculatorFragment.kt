package altcoin.br.decred.fragments

import altcoin.br.decred.R
import altcoin.br.decred.utils.Bitcoin
import altcoin.br.decred.utils.InternetRequests
import altcoin.br.decred.utils.Utils
import altcoin.br.decred.utils.alert
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Response
import kotlinx.android.synthetic.main.fragment_calculator.*
import org.json.JSONArray
import org.json.JSONObject

class CalculatorFragment : Fragment() {
    private fun prepareListeners() {
        bConvertBtcTo?.setOnClickListener {
            if (verifyEditTextNull(etValueToConvertBtc)) {
                Utils.logFabric("calculator", "operation", "btcTo")
                
                hideKeyboard()
                
                Utils.writePreference(activity, "etValueToConvertBtc", etValueToConvertBtc?.text.toString())
                
                val listener = Response.Listener<String> { response ->
                    try {
                        val obj = JSONArray(response).getJSONObject(0)
                        
                        val quantity = Utils.eval(etValueToConvertBtc)
                        
                        tvCalcBtcInDcr?.text = Utils.numberComplete(String.format("%s", quantity / java.lang.Double.parseDouble(obj.getString("price_btc"))), 8)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        
                        alert(activity, "Error while converting")
                    }
                }
                
                execApiCall(listener)
            }
        }
        
        bConvertUsdTo?.setOnClickListener {
            if (verifyEditTextNull(etValueToConvertUsd)) {
                Utils.logFabric("calculator", "operation", "usdTo")
                
                hideKeyboard()
                
                Utils.writePreference(activity, "etValueToConvertUsd", etValueToConvertUsd?.text.toString())
                
                val listener = Response.Listener<String> { response ->
                    try {
                        val obj = JSONArray(response).getJSONObject(0)
                        
                        val quantity = Utils.eval(etValueToConvertUsd)
                        
                        tvCalcUsdInDcr?.text = Utils.numberComplete(String.format("%s", quantity / java.lang.Double.parseDouble(obj.getString("price_usd"))), 8)
                    } catch (e: Exception) {
                        e.printStackTrace()
    
                        alert(activity, "Error while converting")
                    }
                }
                
                execApiCall(listener)
            }
        }
        
        bConvertBrlTo?.setOnClickListener {
            if (verifyEditTextNull(etValueToConvertBrl)) {
                Utils.logFabric("calculator", "operation", "brlTo")
                
                hideKeyboard()
                
                Utils.writePreference(activity, "etValueToConvertBrl", etValueToConvertBrl?.text.toString())
                
                val listener2 = Response.Listener<String> { response ->
                    try {
                        val obj = JSONObject(response) //.getJSONObject("ticker_24h").getJSONObject("total")
                        
                        val quantity = Utils.eval(etValueToConvertBrl) / obj.getDouble("last")
                        
                        val listener = Response.Listener<String> { response2 ->
                            try {
                                val obj2 = JSONArray(response2).getJSONObject(0)
                                
                                tvCalcBrlInDcr?.text = Utils.numberComplete(String.format("%s", quantity / java.lang.Double.parseDouble(obj2.getString("price_btc"))), 4)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                
                                Toast.makeText(activity, "Error while converting", Toast.LENGTH_LONG).show()
                            }
                        }
                        
                        execApiCall(listener)
                    } catch (e: Exception) {
                        e.printStackTrace()
    
                        alert(activity, "Error while converting")
                    }
                }
                
                Bitcoin.convertBtcToBrl(listener2)
            }
        }
        
        bConvertDcrTo?.setOnClickListener {
            if (verifyEditTextNull(etValueToConvertDcr)) {
                Utils.logFabric("calculator", "operation", "dcrTo")
                
                hideKeyboard()
                
                Utils.writePreference(activity, "etValueToConvertDcr", etValueToConvertDcr?.text.toString())
                
                val listener = Response.Listener<String> { response ->
                    try {
                        val obj = JSONArray(response).getJSONObject(0)
                        
                        val quantity = Utils.eval(etValueToConvertDcr)
                        
                        tvCalcDcrInBtc?.text = Utils.numberComplete(String.format("%s", quantity * java.lang.Double.parseDouble(obj.getString("price_btc"))), 8)
                        
                        tvCalcDcrInUsd?.text = Utils.numberComplete(String.format("%s", quantity * java.lang.Double.parseDouble(obj.getString("price_usd"))), 4)
                        
                        val listener = Response.Listener<String> { response2 ->
                            try {
                                val obj2 = JSONObject(response2) //.getJSONObject("ticker_24h").getJSONObject("total")
                                
                                tvCalcDcrInBrl?.text = Utils.numberComplete(java.lang.Double.parseDouble(obj.getString("price_btc")) * obj2.getDouble("last") * quantity, 4)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        
                        Bitcoin.convertBtcToBrl(listener)
                    } catch (e: Exception) {
                        e.printStackTrace()
    
                        alert(activity, "Error while converting")
                    }
                }
                
                execApiCall(listener)
            }
        }
    }
    
    private fun instanceObjects() {
        etValueToConvertBrl?.setText(Utils.readPreference(activity, "etValueToConvertBrl", "0"))
        etValueToConvertBtc?.setText(Utils.readPreference(activity, "etValueToConvertBtc", "0"))
        etValueToConvertUsd?.setText(Utils.readPreference(activity, "etValueToConvertUsd", "0"))
        etValueToConvertDcr?.setText(Utils.readPreference(activity, "etValueToConvertDcr", "0"))
    }
    
    private fun verifyEditTextNull(et: EditText?): Boolean {
        if (et?.text.toString() == "") {
            Toast.makeText(activity, "You need to fill the box", Toast.LENGTH_SHORT).show()
            
            return false
        }
        
        return true
    }
    
    private fun execApiCall(listener: Response.Listener<String>) {
        val url = "https://api.coinmarketcap.com/v1/ticker/decred/"
        
        val internetRequests = InternetRequests()
        
        internetRequests.executeGet(url, listener)
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
        return inflater?.inflate(R.layout.fragment_calculator, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        instanceObjects()
        
        prepareListeners()
    }
}
