package edu.uci.ics.gmehta1.service.billing.core;

import edu.uci.ics.gmehta1.service.billing.models.AmountModel;
import edu.uci.ics.gmehta1.service.billing.models.OrderModel;
import edu.uci.ics.gmehta1.service.billing.models.Transaction_FeeModel;

public class Transaction {
    private String transactionId;
    private String state;
    private AmountModel amount;
    private Transaction_FeeModel transaction_fee;
    private String create_time;
    private String update_time;
    private OrderModel[] orders;


    public Transaction(String transactionId, String state, AmountModel amount, Transaction_FeeModel transaction_fee, String create_time, String update_time, OrderModel[] orders) {
        this.transactionId = transactionId;
        this.state = state;
        this.amount = amount;
        this.transaction_fee = transaction_fee;
        this.create_time = create_time;
        this.update_time = update_time;
        this.orders = orders;
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

    public OrderModel[] getOrders() {
        return orders;
    }
}
