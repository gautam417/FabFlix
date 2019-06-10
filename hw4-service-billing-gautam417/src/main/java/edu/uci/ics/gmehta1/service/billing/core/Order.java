package edu.uci.ics.gmehta1.service.billing.core;

import java.sql.Date;

public class Order {
    private String transactionId;
    private String email;
    private String movieId;
    private int quantity;
    private float unit_price;
    private float discount;
    private Date saleDate;

    public Order(String transactionId, String email, String movieId, int quantity, float unit_price, float discount, Date saleDate) {
        this.transactionId= transactionId;
        this.email = email;
        this.movieId = movieId;
        this.quantity = quantity;
        this.unit_price = unit_price;
        this.discount = discount;
        this.saleDate = saleDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getEmail() {
        return email;
    }

    public String getMovieId() {
        return movieId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Date getSaleDate() {
        return saleDate;
    }

    public float getUnit_price() {
        return unit_price;
    }

    public float getDiscount() {
        return discount;
    }
}
