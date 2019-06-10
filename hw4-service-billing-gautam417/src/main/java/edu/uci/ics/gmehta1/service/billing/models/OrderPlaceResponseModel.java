package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderPlaceResponseModel {
    private int resultCode;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String redirectURL;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String token;

    @JsonCreator
    public OrderPlaceResponseModel
            (
                    @JsonProperty(value = "resultCode", required = true) int resultCode,
                    @JsonProperty(value = "message", required = true) String message,
                    @JsonProperty(value = "redirectURL") String redirectURL,
                    @JsonProperty(value = "token") String token
            )
    {
        this.resultCode = resultCode;
        this.message = message;
        this.redirectURL = redirectURL;
        this.token = token;
    }
    @Override
    public String toString() {return "Result Code: " + Integer.toString(resultCode) + "\nMessage: " + message;}
    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
    @JsonProperty("redirectURL")
    public String getRedirectURL() { return redirectURL; }
    @JsonProperty("token")
    public String getToken() { return token; }
}
