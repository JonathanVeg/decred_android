package altcoin.br.decred.utils.exchanges

abstract class AbstractExchange(val exchange: EnumExchanges, val coin: String, val market: String) {
    abstract fun onValueLoaded()
    var last = "-1"
    var coinVolume = "-1"
    var baseVolume = "-1"
    var ask = "-1"
    var low = "-1"
    var bid = "-1"
    var high = "-1"
    var changes = "-1"
    
    init {
        when (exchange) {
            EnumExchanges.BITTREX -> object : Bittrex(coin, market) {
                override fun onValueLoaded() {
                    super.onValueLoaded()
                    
                    setData(this)
                }
            }.loadData()
            
            EnumExchanges.POLONIEX -> object : Poloniex(coin, market) {
                override fun onValueLoaded() {
                    super.onValueLoaded()
                    
                    setData(this)
                }
            }.loadData()
            
            EnumExchanges.BLEUTRADE -> object : Bleutrade(coin, market) {
                override fun onValueLoaded() {
                    super.onValueLoaded()
                    
                    setData(this)
                }
            }.loadData()
            
            EnumExchanges.PROFITFY -> object : Profitfy(coin, market) {
                override fun onValueLoaded() {
                    super.onValueLoaded()
                    
                    setData(this)
                }
            }.loadData()
            
            EnumExchanges.OOOBTC -> object : Ooobtc(coin, market) {
                override fun onValueLoaded() {
                    super.onValueLoaded()
                    
                    setData(this)
                }
            }.loadData()
            
            EnumExchanges.HUOBI -> object : Huobi(coin, market) {
                override fun onValueLoaded() {
                    super.onValueLoaded()
                    
                    setData(this)
                }
            }.loadData()
            
            else -> {
            }
        }
    }
    
    private fun setData(it: Exchange) {
        last = it.last
        baseVolume = it.baseVolume
        coinVolume = it.coinVolume
        ask = it.ask
        bid = it.bid
        high = it.high
        low = it.low
        changes = it.changes
        
        onValueLoaded()
    }
}
