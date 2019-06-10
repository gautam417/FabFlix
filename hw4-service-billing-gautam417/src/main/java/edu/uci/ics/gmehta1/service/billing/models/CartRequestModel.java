package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class CartRequestModel {
    private String email;
    private String movieId;
    private int quantity;


    @JsonCreator
    public CartRequestModel(
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "movieId", required = true) String movieId,
            @JsonProperty(value = "quantity", required = true) int quantity)
    {
        this.email = email;
        this.movieId = movieId;
        this.quantity = quantity;
    }
    @JsonProperty
    public String getEmail() { return email; }
    @JsonProperty
    public String getMovieId() { return movieId; }
    @JsonProperty
    public int getQuantity() { return quantity; }
}
