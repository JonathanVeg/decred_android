package altcoin.br.decred.utils.exchanges

enum class EnumExchanges(val label: String) {
    BITTREX("Bittrex"),
    BLEUTRADE("Bleutrade"),
    POLONIEX("Poloniex"),
    PROFITFY("Profitfy"),
    OOOBTC("ooobtc"),
    HUOBI("Huobi"),
    COINMARKETCAP("CoinMarketCap");
    
    override fun toString() = label
}
