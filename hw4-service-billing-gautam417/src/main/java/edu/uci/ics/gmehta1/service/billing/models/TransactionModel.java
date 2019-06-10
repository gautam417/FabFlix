package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.billing.core.Transaction;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;

import java.util.ArrayList;


//BE CARE FUL TO FORMAT THE DATE CORRECTLY
@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class TransactionModel {
    private String transactionId;
    private String state;
    private AmountModel amount;
    private Transaction_FeeModel transaction_fee;
    private String create_time;
    private String update_time;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private OrderModel[] orders;

    public TransactionModel(@JsonProperty(value = "transactionId", required = true) String transactionId,
                            @JsonProperty(value = "state", required = true) String state,
                            @JsonProperty(value = "amount", required = true) AmountModel amount,
                            @JsonProperty(value = "transaction_fee", required = true) Transaction_FeeModel transaction_fee,
                            @JsonProperty(value = "create_time", required = true) String create_time,
                            @JsonProperty(value = "update_time", required = true) String update_time,
                            @JsonProperty(value = "items") OrderModel[] orders)
    {
        this.transactionId = transactionId;
        this.state = state;
        this.amount = amount;
        this.transaction_fee = transaction_fee;
        this.create_time = create_time;
        this.update_time = update_time;
        this.orders = orders;
    }
    //I THINK BUILD THE BUILDMODELFROMLIST FUNCS HERE FOR AMOUNT AND TRANSACTION_FEE_MODEL
    //NEED TO BUILD ORDER MODEL
//    public static OrderModel buildModelFromObject(Order o) {

//    public static CustomResponseModel buildModelFromList(Customer customers) {
//        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
//        // Must convert arraylist to array.
//        ServiceLogger.LOGGER.info("Creating model...");
//
//        if (customers == null)
//        { // NOT SURE IF THIS WILL WORK
//            ServiceLogger.LOGGER.info("No customers list passed to model constructor.");
//            return new CustomResponseModel(332,"Customer does not exist.", null);
//        }
//        ServiceLogger.LOGGER.info("Creditcards list is not empty...");
//
//        CustomerModel cm = CustomerModel.buildModelFromObject(customers);
//        ServiceLogger.LOGGER.info("Finished building model.");
//        return new CustomResponseModel(3320,"Customer retrieved successfully.", cm);
//    }
//    public static OrdResponseModel buildModelFromList(ArrayList<Transaction> transactions) {
//        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
//        // Must convert arraylist to array.
//        ServiceLogger.LOGGER.info("Creating model...");
//
////        if (orders.size() == 0) {
////            ServiceLogger.LOGGER.info("No items list passed to model constructor.");
//////            return new OrdResponseModel(312,"Shopping item does not exist.", null);
////        }
//
//        ServiceLogger.LOGGER.info("Transactions list is not empty...");
//        int len = transactions.size();
//        ServiceLogger.LOGGER.info("Transactions size: "+ Integer.toString(len));
//
//        TransactionModel[] array = new TransactionModel[len];
//
//        for (int i = 0; i < len; ++i)
//        {
//            ServiceLogger.LOGGER.info("Adding item " + transactions.get(i).getTransactionId() + " to array.");
//            // Convert each item in the arraylist to an ItemModel
//            TransactionModel tm = TransactionModel.buildModelFromObject(transactions.get(i));
//            // If the new model has valid data, add it to array
//            array[i] = tm;
//        }
//        ServiceLogger.LOGGER.info("Finished building model. Array of items contains: ");
//        for (TransactionModel tm : array) {
//            ServiceLogger.LOGGER.info("\t" + tm);
//        }
//        return new OrdResponseModel(3410,"Orders retrieved successfully.", array);
//    }
    public static TransactionModel buildModelFromObject(Transaction t) {
        return new TransactionModel(t.getTransactionId(), t.getState(), t.getAmount(),t.getTransaction_fee(),t.getCreate_time(),t.getUpdate_time(),t.getOrders());
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getState() {
        return state;
    }

    public AmountModel getAmount() {
        return amount;
    }

    public Transaction_FeeModel getTransaction_fee() {
        return transaction_fee;
    }

    public String getCreate_time() {
        return create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

//    public OrderModel[] getOrders() {
//        return orders;
//    }
    @JsonProperty("items") // should be Orders
    public OrderModel[] getOrders() { return orders; }
}
