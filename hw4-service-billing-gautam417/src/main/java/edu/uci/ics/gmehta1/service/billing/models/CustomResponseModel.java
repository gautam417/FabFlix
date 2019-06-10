package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.billing.core.Customer;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CustomResponseModel {
    private int resultCode;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CustomerModel customers;

    @JsonCreator
    public CustomResponseModel
            (
                    @JsonProperty(value = "resultCode", required = true) int resultCode,
                    @JsonProperty(value = "message", required = true) String message,
                    @JsonProperty(value = "customer") CustomerModel customers
            )
    {
        this.resultCode = resultCode;
        this.message = message;
        this.customers = customers;
    }

    public static CustomResponseModel buildModelFromList(Customer customers) {
        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
        // Must convert arraylist to array.
        ServiceLogger.LOGGER.info("Creating model...");

        if (customers == null)
        { // NOT SURE IF THIS WILL WORK
            ServiceLogger.LOGGER.info("No customers list passed to model constructor.");
            return new CustomResponseModel(332,"Customer does not exist.", null);
        }
        ServiceLogger.LOGGER.info("Creditcards list is not empty...");

        CustomerModel cm = CustomerModel.buildModelFromObject(customers);
        ServiceLogger.LOGGER.info("Finished building model.");
        return new CustomResponseModel(3320,"Customer retrieved successfully.", cm);
    }

    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
    @JsonProperty("customer")
    public CustomerModel getCustomers() { return customers; }
}
