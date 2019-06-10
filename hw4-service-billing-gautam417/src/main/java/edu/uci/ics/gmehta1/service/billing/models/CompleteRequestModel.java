package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class CompleteRequestModel
{
    private String paymentId;
    private String token;
    private String PayerId;

    @JsonCreator
    public CompleteRequestModel(@JsonProperty(value = "paymentId", required = true) String paymentId,
                                @JsonProperty(value = "token", required = true) String token,
                                @JsonProperty(value = "PayerID", required = true) String PayerId)
    {
        this.paymentId = paymentId;
        this.token = token;
        this.PayerId = PayerId;
    }

    @JsonProperty
    public String getPaymentId() { return paymentId; }
    @JsonProperty
    public String getToken() { return token; }
    @JsonProperty
    public String getPayerId() { return PayerId; }
}
