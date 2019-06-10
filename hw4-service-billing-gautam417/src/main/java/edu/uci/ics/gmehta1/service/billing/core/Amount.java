package edu.uci.ics.gmehta1.service.billing.core;

public class Amount {
    private String total;
    private String currency;

    public Amount(String total, String currency) {
        this.total = total;
        this.currency = currency;
    }

    public String getTotal() {
        return total;
    }

    public String getCurrency() {
        return currency;
    }
}
