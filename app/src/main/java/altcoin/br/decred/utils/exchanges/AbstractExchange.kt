package altcoin.br.decred.utils.exchanges

abstract class AbstractExchange(val exchange: EnumExchanges) {
    abstract fun onValueLoaded()
    var last = "0"
    var baseVolume = "0"
    var ask = "0"
    var bid = "0"
    var changes = "0"
    
    init {
        when (exchange) {
            EnumExchanges.BITTREX -> object : Bittrex() {
                override fun onValueLoaded() {
                    super.onValueLoaded()
                    
                    setData(this)
                }
            }.loadData()
            
            EnumExchanges.POLONIEX -> object : Poloniex() {
                override fun onValueLoaded() {
                    super.onValueLoaded()
                    
                    setData(this)
                }
            }.loadData()
            
            EnumExchanges.BLEUTRADE -> object : Bleutrade() {
                override fun onValueLoaded() {
                    super.onValueLoaded()
                    
                    setData(this)
                }
            }.loadData()
            
            EnumExchanges.PROFITFY -> object : Profitfy() {
                override fun onValueLoaded() {
                    super.onValueLoaded()
                    
                    setData(this)
                }
            }.loadData()
        }
    }
    
    private fun setData(it: Exchange) {
        last = it.last
        baseVolume = it.baseVolume
        ask = it.ask
        bid = it.bid
        changes = it.changes
        
        onValueLoaded()
    }
}
