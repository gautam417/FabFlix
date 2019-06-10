package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.billing.core.TransactionFee;

public class Transaction_FeeModel {
    private String value;
    private String currency;

    @JsonCreator
    public Transaction_FeeModel(@JsonProperty(value = "value", required = true) String value,
                                @JsonProperty(value = "currency", required = true) String currency) {
        this.value = value;
        this.currency = currency;
    }

    public static Transaction_FeeModel buildModelFromObject(TransactionFee tf) {
        return new Transaction_FeeModel(tf.getValue(),tf.getCurrency());
    }

    public String getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

}
