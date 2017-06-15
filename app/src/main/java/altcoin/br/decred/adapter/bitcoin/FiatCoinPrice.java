package altcoin.br.decred.adapter.bitcoin;

import com.android.volley.Response;

abstract class FiatCoinPrice implements Response.Listener<String> {

    private double price;
    private double high;
    private double low;

    synchronized void setPrice(double price) {
        this.price = price;
    }

    synchronized void setHigh(double high) {
        this.high = high;
    }

    synchronized void setLow(double low) {
        this.low = low;
    }

    synchronized double getPrice() {
        return price;
    }

    synchronized double getHigh() {
        return high;
    }

    synchronized double getLow() {
        return low;
    }
}