package altcoin.br.decred.utils

import java.math.BigDecimal

fun String.numberComplete(decimalPlaces: Int): String {
	try {
		var bd = BigDecimal(this)

		bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_DOWN)

		return bd.toPlainString()
	} catch (e: Exception) {
		return ""
	}
}