package altcoin.br.decred.adapter.bitcoin;

import com.android.volley.Response;

abstract class FiatCoinPrice implements Response.Listener<String> {

    private double price;
    private double high;
    private double low;

    public synchronized void setPrice(double price) {
        this.price = price;
    }

    public synchronized void setHigh(double high) {
        this.high = high;
    }

    public synchronized void setLow(double low) {
        this.low = low;
    }

    public synchronized double getPrice() {
        return price;
    }

    public synchronized double getHigh() {
        return high;
    }

    public synchronized double getLow() {
        return low;
    }
}