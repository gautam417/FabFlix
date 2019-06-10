package edu.uci.ics.gmehta1.service.api_gateway.models.idm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class LoginResponseModel {
    @JsonProperty(required = true) // Forces this field to be required in the JSON
    private int resultCode;
    @JsonProperty(required = true) // Forces this field to be required in the JSON
    private String message;
    private String sessionID;

    @JsonCreator
    public LoginResponseModel
            (
                    @JsonProperty(value = "resultCode", required = true) int resultCode,
                    @JsonProperty(value = "message", required = true) String message,
                    @JsonProperty(value = "sessionID") String sessionID
            )
    {
        this.resultCode = resultCode;
        this.message = message;
        this.sessionID = sessionID;
    }
    @Override
    public String toString() {return "Result Code: " + Integer.toString(resultCode) + "\nMessage: " + message;}
    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
    @JsonProperty("sessionID")
    public String getSessionID() { return sessionID; }
}
