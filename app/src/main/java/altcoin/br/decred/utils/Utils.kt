package altcoin.br.decred.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import java.math.BigDecimal

fun Activity.hideKeyboard() {
    try {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    } catch (ignored: Exception) {
    }
}

fun String.numberComplete(decimalPlaces: Int) =
        try {
            var bd = BigDecimal(this)
            
            bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_DOWN)
            
            bd.toPlainString()
        } catch (e: Exception) {
            "0.0"
        }

fun Double.numberComplete(decimalPlaces: Int) =
        try {
            var bd = BigDecimal(this)
            
            bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_DOWN)
            
            bd.toPlainString()
        } catch (e: Exception) {
            e.printStackTrace()
            
            "0".numberComplete(decimalPlaces)
        }

fun EditText.toDouble(): Double = try {
    java.lang.Double.parseDouble(this.text.toString())
} catch (e: Exception) {
    0.0
}

fun log(any: Any, tag: String = "KotlinLog") = Log.e(tag, any.toString())
fun View.hide() {
    try {
        this.visibility = View.GONE
    } catch (ignored: Exception) {
    }
}

fun View.show() {
    try {
        this.visibility = View.VISIBLE
    } catch (ignored: Exception) {
    }
}

fun View.isVisible(): Boolean =
        visibility == View.VISIBLE

fun View.visible(b: Boolean) {
    try {
        if (b)
            this.visibility = View.VISIBLE
        else
            this.visibility = View.GONE
    } catch (ignored: Exception) {
    }
}

fun View.toggleVisibility() {
    try {
        if (this.isVisible())
            this.hide()
        else
            this.show()
    } catch (ignored: Exception) {
    }
}

fun isPackageInstalled(context: Context, packageName: String) =
        try {
            val pm = context.packageManager
            
            pm.getPackageInfo(packageName, 0)
            
            true
        } catch (e: Exception) {
            false
        }

fun alert(context: Context, text: String) {
    try {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    } catch (_: Exception) {
    }
}

