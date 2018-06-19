package altcoin.br.decred.adapter

import altcoin.br.decred.R
import altcoin.br.decred.model.Link
import altcoin.br.decred.utils.Utils
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class AdapterLinks(private val context: Activity, private val links: List<Link>) : BaseAdapter() {
    
    override fun getCount() =
            links.size
    
    override fun getItem(i: Int) =
            links[i]
    
    override fun getItemId(i: Int) =
            i.toLong()
    
    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View? {
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        
        val v = li.inflate(R.layout.row_links, null)
        
        val tvLinkLabel = v.findViewById<TextView>(R.id.tvLinkLabel)
        val tvLinkUrl = v.findViewById<TextView>(R.id.tvLinkUrl)
        
        var label = links[position].label
        
        if (!label.endsWith(":"))
            label += ":"
        
        tvLinkLabel.text = label.trim { it <= ' ' }
        tvLinkUrl.text = links[position].url.trim { it <= ' ' }
        
        Utils.textViewLink(tvLinkUrl, links[position].url)
        
        return v
    }
}
