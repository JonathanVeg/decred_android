package altcoin.br.decred.fragments

import altcoin.br.decred.R
import altcoin.br.decred.adapter.AdapterLinks
import altcoin.br.decred.model.Link
import altcoin.br.decred.utils.Utils
import altcoin.br.decred.utils.visible
import android.annotation.SuppressLint
import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.fragment_stats.*

@SuppressLint("SetTextI18n")
class AboutFragment : Fragment() {
    private var links: ArrayList<Link> = ArrayList()
    private var adapterLinks: AdapterLinks? = null
    override fun onStart() {
        super.onStart()

        prepareFirebasePart()

        Utils.textViewLink(tvAboutDeveloper, "https://twitter.com/jonathanveg2")
        Utils.textViewLink(tvAboutCode, "https://github.com/JonathanVeg/decred_android")
        Utils.textViewLink(tvDcrStatsLink, "https://dcrstats.com/")
    }

    private fun prepareListeners() {
        tvAboutDonateWallet?.setOnClickListener {
            try {
                val wallet = "DsUJTC7MZDWfnWyYnmm9P6ijsA44oRQVsSn"

                Utils.copyToClipboard(activity, wallet)

                Toast.makeText(activity, "DCR Wallet ($wallet) copied to clipboard", Toast.LENGTH_LONG).show()

                Utils.logFabric("donationWalletCopied")
            } catch (e: Exception) {
                e.printStackTrace()

                Toast.makeText(activity, "Error while copying wallet", Toast.LENGTH_LONG).show()
            }
        }

        tvAboutDonateWalletBTC?.setOnClickListener {
            try {
                val wallet = "1GDa2bhgKaCwQrka2xY1P9cexKNb88HYFE"

                Utils.copyToClipboard(activity, wallet)

                Toast.makeText(activity, "BTC Wallet ($wallet) copied to clipboard", Toast.LENGTH_LONG).show()

                Utils.logFabric("donationWalletCopied")
            } catch (e: Exception) {
                e.printStackTrace()

                Toast.makeText(activity, "Error while copying wallet", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun instanceObjects() {
        adapterLinks = AdapterLinks(activity, links)

        lvLinks.adapter = adapterLinks
    }

    private fun prepareFirebasePart() {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val database = FirebaseDatabase.getInstance()
            val showWallet = database.getReference("donation").child("show_wallet")

            // Read from the database
            showWallet.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val value = dataSnapshot.getValue<Boolean>(Boolean::class.java)

                    llDonate?.visible(value == true)

                    showWallet.keepSynced(true)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            // links
            val drLinks = database.getReference("links")

            // Read from the database
            drLinks.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    try {
                        val value = dataSnapshot.getValue<String>(String::class.java)

                        val localLinks = ArrayList<Link>()

                        val arrLinks = value.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

                        var i = 0
                        while (i < arrLinks.size) {
                            localLinks.add(Link(arrLinks[i], arrLinks[i + 1]))
                            i += 2
                        }

                        links.clear()

                        links.addAll(localLinks)

                        adapterLinks?.notifyDataSetChanged()

                        drLinks.keepSynced(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_about, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        instanceObjects()

        prepareListeners()
    }
}
