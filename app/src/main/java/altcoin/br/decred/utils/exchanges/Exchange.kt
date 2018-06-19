package altcoin.br.decred.utils.exchanges

abstract class Exchange {
    var last = "-1"
    var coinVolume = "-1"
    var baseVolume = "-1"
    var ask = "-1"
    var bid = "-1"
    var high = "-1"
    var low = "-1"
    var changes = "-1"
    abstract fun onValueLoaded()
    abstract fun loadData()
}
