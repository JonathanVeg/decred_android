package altcoin.br.decred.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
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

fun String.numberComplete(decimalPlaces: Int): String {
	try {
		var bd = BigDecimal(this)

		bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_DOWN)

		return bd.toPlainString()
	} catch (e: Exception) {
		return ""
	}
}

fun Double.numberComplete(decimalPlaces: Int): String {
	try {
		var bd = BigDecimal(this)

		bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_DOWN)

		return bd.toPlainString()
	} catch (e: Exception) {
		e.printStackTrace()

		return "0".numberComplete(decimalPlaces)
	}
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

fun isPackageInstalled(context: Context, packageName: String): Boolean {
	try {
		val pm = context.packageManager

		pm.getPackageInfo(packageName, 0)

		return true
	} catch (e: Exception) {
		return false
	}
}

fun alert(context: Context, text: String) {
	try {
		Toast.makeText(context, text, Toast.LENGTH_LONG).show()
	} catch (ignored: Exception) {
	}
}

fun fromHtml(source: String): Spanned {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
	} else {
		Html.fromHtml(source)
	}
}