package altcoin.br.decred.model

import altcoin.br.decred.utils.exchanges.EnumExchanges

class ExchangeData(var exchange: EnumExchanges, var coin: String, var market: String){
	var last = "-1"
    var coinVolume = "-1"
    var baseVolume = "-1"
    var ask = "-1"
    var low = "-1"
    var bid = "-1"
    var high = "-1"
    var changes = "-1"
}
