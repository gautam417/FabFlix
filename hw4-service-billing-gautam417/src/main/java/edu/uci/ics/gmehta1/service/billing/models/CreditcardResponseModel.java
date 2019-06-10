package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreditcardResponseModel {
    private int resultCode;
    private String message;
    //NEED Creditcard ARRAY HERE
//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonCreator
    public CreditcardResponseModel
            (
                    @JsonProperty(value = "resultCode", required = true) int resultCode,
                    @JsonProperty(value = "message", required = true) String message
            )
    {
        this.resultCode = resultCode;
        this.message = message;
    }
    @Override
    public String toString() {return "Result Code: " + Integer.toString(resultCode) + "\nMessage: " + message;}
    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
}
