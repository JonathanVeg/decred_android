package altcoin.br.decred.utils.exchanges

enum class EnumExchanges(val label: String) {
    BITTREX("Bittrex"),
    BLEUTRADE("Bleutrade"),
    POLONIEX("Poloniex"),
    PROFITFY("Profitfy");
    
    override fun toString() = label
}
