package edu.uci.ics.gmehta1.service.billing.core;

public class Item {
    private String email;
    private String movieId;
    private int quantity;

    public Item(String email, String movieId, int quantity) {
        this.email = email;
        this.movieId = movieId;
        this.quantity = quantity;
    }
    @Override
    public String toString() {return "Email: " + (email) + "\nMovieId: " + movieId+ "\nQuantity: " + Integer.toString(quantity);}

    public String getEmail() {
        return email;
    }

    public String getMovieId() {
        return movieId;
    }

    public int getQuantity() {
        return quantity;
    }
}

