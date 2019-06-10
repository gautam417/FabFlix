package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerResponseModel {
    private int resultCode;
    private String message;

    @JsonCreator
    public CustomerResponseModel
            (
                    @JsonProperty(value = "resultCode", required = true) int resultCode,
                    @JsonProperty(value = "message", required = true) String message
            )
    {
        this.resultCode = resultCode;
        this.message = message;
    }

    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
}
