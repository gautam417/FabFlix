package edu.uci.ics.gmehta1.service.billing.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class EmailRequestModel {
    private String email;

    @JsonCreator
    public EmailRequestModel(@JsonProperty(value = "email", required = true) String email)
    {
        this.email = email;
    }
    @JsonProperty
    public String getEmail() { return email; }
}
