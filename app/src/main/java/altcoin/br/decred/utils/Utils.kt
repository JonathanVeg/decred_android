package altcoin.br.decred.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import java.math.BigDecimal

fun String.copyToClipboard(context: Context) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        val clip = ClipData.newPlainText(".", this)
        
        clipboard.primaryClip = clip
    } catch (e: Exception) {
        e.printStackTrace()
        
        alert(context, "Error while copying")
    }
}

fun makeSpannableBold(title: String, value: String): SpannableString {
    val ss = SpannableString("${title.trim()} ${value.trim()}")
    
    val boldSpan = StyleSpan(Typeface.BOLD)
    
    ss.setSpan(boldSpan, 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    
    return ss
}

fun hash(name: String): Int {
    val s = name.replace(" ".toRegex(), "")
    
    var h = 0
    
    for (i in 0 until s.length)
        h = 31 * h + s[i].toInt()
    
    return h
}

fun Context.writePreference(key: String, value: Boolean) {
    try {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        
        preferences.edit().putBoolean(key, value).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.writePreference(key: String, value: String) {
    try {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        
        preferences.edit().putString(key, value).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.readPreference(key: String, defaultValue: Boolean): Boolean {
    try {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        
        return preferences.getBoolean(key, defaultValue)
    } catch (e: Exception) {
        e.printStackTrace()
        
        return defaultValue
    }
}

fun Context.readPreference(key: String, defaultValue: String): String {
    try {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        
        return preferences.getString(key, defaultValue)
    } catch (e: Exception) {
        e.printStackTrace()
        
        return defaultValue
    }
}

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

fun View.setVisibility(visible: Boolean, hideIsGone: Boolean = true) {
    try {
        if (visible)
            this.show()
        else
            this.hide(hideIsGone)
    } catch (_: Exception) {
    }
}

fun View?.hide(hideIsGone: Boolean = true) {
    try {
        if (hideIsGone)
            this?.visibility = View.GONE
        else
            this?.visibility = View.INVISIBLE
    } catch (ignored: Exception) {
    }
}

fun View?.show() {
    try {
        this?.visibility = View.VISIBLE
    } catch (ignored: Exception) {
    }
}

fun View?.isVisible(): Boolean =
        this?.visibility == View.VISIBLE

fun View?.visible(b: Boolean) {
    try {
        if (b)
            this?.visibility = View.VISIBLE
        else
            this?.visibility = View.GONE
    } catch (ignored: Exception) {
    }
}

fun View?.toggleVisibility() {
    try {
        if (this?.isVisible() == true)
            this.hide()
        else
            this?.show()
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

var toast: Toast? = null
fun alert(context: Context, text: Any) {
    try {
        if (toast != null) toast?.cancel()
        
        toast = Toast.makeText(context, text.toString(), Toast.LENGTH_LONG)
        
        toast?.show()
    } catch (_: Exception) {
    }
}