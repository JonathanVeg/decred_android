package altcoin.br.decred.utils.exchanges

abstract class Exchange {
    var last = "0"
    var baseVolume = "0"
    var ask = "0"
    var bid = "0"
    var changes = "0"
    abstract fun onValueLoaded()
    abstract fun loadData()
}
