package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.uci.ics.gmehta1.service.billing.core.Order;

import java.sql.Date;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class OrderModel {
    private String email;
    private String movieId;
    private int quantity;
    private float unit_price;
    private float discount;
    @JsonSerialize(as = Date.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date saleDate;

    public OrderModel(@JsonProperty(value = "email", required = true) String email,
                 @JsonProperty(value = "movieId", required = true) String movieId,
                 @JsonProperty(value = "quantity", required = true) int quantity,
                 @JsonProperty(value = "unit_price", required = true) float unit_price,
                      @JsonProperty(value = "discount", required = true) float discount,
                 @JsonProperty(value = "saleDate", required = true) Date saleDate)
    {
        this.email = email;
        this.movieId = movieId;
        this.quantity = quantity;
        this.unit_price = unit_price;
        this.discount = discount;
        this.saleDate = saleDate;
    }

    public static OrderModel buildModelFromObject(Order o) {
        return new OrderModel(o.getEmail(), o.getMovieId(), o.getQuantity(), o.getUnit_price(), o.getDiscount(), o.getSaleDate());
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

    public float getUnit_price() {
        return unit_price;
    }

    public float getDiscount() {
        return discount;
    }

    public Date getSaleDate() {
        return saleDate;
    }
}
