package altcoin.br.decred.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class CopyableTextView(context: Context, attributes: AttributeSet) : TextView(context, attributes) {
    init {
        this.setOnLongClickListener {
            try {
                text.toString().copyToClipboard(context)
                
                alert(context, "Value $text copied to clipboard")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            true
        }
    }
}
