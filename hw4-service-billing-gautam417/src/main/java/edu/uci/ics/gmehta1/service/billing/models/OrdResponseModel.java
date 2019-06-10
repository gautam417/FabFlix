package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.billing.core.Order;
import edu.uci.ics.gmehta1.service.billing.core.Transaction;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrdResponseModel {
    private int resultCode;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private TransactionModel[] transactions;
//    @JsonInclude(JsonInclude.Include.NON_EMPTY)
//    private OrderModel[] orders;

    @JsonCreator
    public OrdResponseModel
    (
            @JsonProperty(value = "resultCode", required = true) int resultCode,
            @JsonProperty(value = "message", required = true) String message,
            @JsonProperty(value = "transactions") TransactionModel[] transactions
//            @JsonProperty(value = "items") OrderModel[] orders

    )
    {
        this.resultCode = resultCode;
        this.message = message;
        this.transactions = transactions;
//        this.orders = orders;
    }

    public static OrdResponseModel buildModelFromList(ArrayList<Transaction> transactions) {
        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
        // Must convert arraylist to array.
        ServiceLogger.LOGGER.info("Creating model...");

//        if (orders.size() == 0) {
//            ServiceLogger.LOGGER.info("No items list passed to model constructor.");
////            return new OrdResponseModel(312,"Shopping item does not exist.", null);
//        }
        int len = transactions.size();
        ServiceLogger.LOGGER.info("Transactions size: "+ Integer.toString(len));

        TransactionModel[] array2 = new TransactionModel[len];

        for (int i = 0; i < len; ++i)
        {
            ServiceLogger.LOGGER.info("Adding transaction " + transactions.get(i).getTransactionId() + " to array.");
            // Convert each item in the arraylist to an ItemModel
            TransactionModel tm = TransactionModel.buildModelFromObject(transactions.get(i));
            // If the new model has valid data, add it to array
            array2[i] = tm;
        }
        return new OrdResponseModel(3410,"Orders retrieved successfully.", array2);
    }
    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
    @JsonProperty("transactions") // should be Orders
    public TransactionModel[] getTransactions() { return transactions; }
//    @JsonProperty("orders") // should be Orders
//    public OrderModel[] getOrders() { return orders; }
}
