package edu.uci.ics.gmehta1.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class RegisterResponseModel {
    private int resultCode;
    private String message;

    @JsonCreator
    public RegisterResponseModel
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
