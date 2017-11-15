package altcoin.br.decred.adapter.bitcoin;

import com.android.volley.Response;

abstract class FiatCoinPrice implements Response.Listener<String> {

    private double price;
    private double high;
    private double low;

    synchronized double getPrice() {
        return price;
    }

    synchronized void setPrice(double price) {
        this.price = price;
    }

    synchronized double getHigh() {
        return high;
    }

    synchronized void setHigh(double high) {
        this.high = high;
    }

    synchronized double getLow() {
        return low;
    }

    synchronized void setLow(double low) {
        this.low = low;
    }
}