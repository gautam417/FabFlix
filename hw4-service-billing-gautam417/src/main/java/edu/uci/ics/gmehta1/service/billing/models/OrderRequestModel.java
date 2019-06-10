package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderRequestModel
{
    private String email;

    @JsonCreator
    public OrderRequestModel(
            @JsonProperty(value = "email", required = true) String email)
    {
        this.email = email;
    }
    @JsonProperty
    public String getEmail() { return email; }
}
