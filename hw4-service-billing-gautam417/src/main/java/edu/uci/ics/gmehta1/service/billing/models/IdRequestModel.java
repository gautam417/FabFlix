package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class IdRequestModel {
    private String id;

    @JsonCreator
    public IdRequestModel(@JsonProperty(value = "id", required = true) String id)
    {
        this.id = id;
    }
    @JsonProperty
    public String getId() { return id; }
}
