package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.billing.core.Item;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class ItemModel {
    private String email;
    private String movieId;
    private int quantity;

    @JsonCreator
    public ItemModel(
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "movieId", required = true) String movieId,
            @JsonProperty(value = "quantity", required = true) int quantity)
    {
        this.email = email;
        this.movieId = movieId;
        this.quantity = quantity;
    }
    public static ItemModel buildModelFromObject(Item i) {
        return new ItemModel(i.getEmail(), i.getMovieId(), i.getQuantity());
    }
    // MIGHT NEED TO FINISH

    public String getEmail() { return email; }

    public String getMovieId() { return movieId; }

    public int getQuantity() { return quantity; }
}
