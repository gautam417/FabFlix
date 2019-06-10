package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.billing.core.Creditcard;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CreditResponseModel {
    private int resultCode;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CreditcardModel creditcards;

    @JsonCreator
    public CreditResponseModel
            (
                    @JsonProperty(value = "resultCode", required = true) int resultCode,
                    @JsonProperty(value = "message", required = true) String message,
                    @JsonProperty(value = "creditcard") CreditcardModel creditcards
            )
    {
        this.resultCode = resultCode;
        this.message = message;
        this.creditcards = creditcards;
    }
    public static CreditResponseModel buildModelFromList(Creditcard creditcards) {
        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
        // Must convert arraylist to array.
        ServiceLogger.LOGGER.info("Creating model...");

        if (creditcards == null)
        { // NOT SURE IF THIS WILL WORK
            ServiceLogger.LOGGER.info("No creditcards list passed to model constructor.");
            return new CreditResponseModel(324,"Credit card does not exist.", null);
        }
        ServiceLogger.LOGGER.info("Creditcards list is not empty...");

        CreditcardModel cm = CreditcardModel.buildModelFromObject(creditcards);

        ServiceLogger.LOGGER.info("Finished building model. Creditcard will be inserted");

        return new CreditResponseModel(3230,"Credit card retrieved successfully", cm);
    }
    public static CustomerResponseModel buildModelFromList2(Creditcard creditcards) {
        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
        // Must convert arraylist to array.
        ServiceLogger.LOGGER.info("Creating model...");

        if (creditcards == null)
        { // NOT SURE IF THIS WILL WORK
            ServiceLogger.LOGGER.info("No creditcards list passed to model constructor.");
            return new CustomerResponseModel(331,"Credit card ID not found.");
        }
        ServiceLogger.LOGGER.info("Creditcards list is not empty...");
        ServiceLogger.LOGGER.info("Finished building model.");
        return new CustomerResponseModel(333,"Duplicate insertion.");
    }
    public static CustomerResponseModel buildModelFromList3(Creditcard creditcards) {
        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
        // Must convert arraylist to array.
        ServiceLogger.LOGGER.info("Creating model...");

        if (creditcards == null)
        { // NOT SURE IF THIS WILL WORK
            ServiceLogger.LOGGER.info("No creditcards list passed to model constructor.");
            return new CustomerResponseModel(331,"Credit card ID not found.");
        }
        ServiceLogger.LOGGER.info("Creditcards list is not empty...");
        ServiceLogger.LOGGER.info("Finished building model.");
        return new CustomerResponseModel(3310,"Customer updated successfully.");
    }
    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
    @JsonProperty("creditcard")
    public CreditcardModel getCreditcards() { return creditcards; }
}
