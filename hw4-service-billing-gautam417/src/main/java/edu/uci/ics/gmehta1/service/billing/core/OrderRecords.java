package edu.uci.ics.gmehta1.service.billing.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.api.payments.*;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.PayPalRESTException;
import edu.uci.ics.gmehta1.service.billing.BillingService;
import edu.uci.ics.gmehta1.service.billing.configs.Configs;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.billing.models.*;

import java.io.IOException;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.paypal.base.rest.APIContext;

import javax.ws.rs.core.UriBuilder;
import java.sql.CallableStatement;

public class OrderRecords {
    private static String clientId = "AXa-a2hZrdimPSsBdl89c3envXZMbxeZIycXDbj8w4GLnf4uvR7SiDvg4PmmUqe368HuzTYCSdS-s_jk";
    private static String clientSecret = "EPIcGgCWcmpGiHrnJ0HdZ3BqIGILtsUHbE7BrtOQShZze8aYApn01vnGcsnl__dRERaEFfPpIEfOHk2W";
    public static CustomResponseModel retrieveCustomerFromDB(MixRequestModel crm)
    {
        try {
            // Construct the query
            String query = "SELECT * FROM customers " +
                    "WHERE email LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, crm.getEmail());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded to retrieve items.");
            // Retrieve the Items from the Result Set
            while (rs.next())
            {
                Customer c = new Customer(
                        rs.getString("email"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("ccId"),
                        rs.getString("address")
                );
                ServiceLogger.LOGGER.info("Retrieved customer " + c);
                return CustomResponseModel.buildModelFromList(c);
            }
            // Got the items. Builds the response model
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve creditcard from creditcards table.");
            e.printStackTrace();
        }
        // No items were retrieved. Build response model with null arraylist.
        ServiceLogger.LOGGER.info("No items were retrieved.");
        return CustomResponseModel.buildModelFromList(null);
    }
    public static ItemsResponseModel retrieveItemsFromDB(MixRequestModel crm)
    {
//        ItemsResponseModel responseModel;
        try {
            // Construct the query
            String query = "SELECT email, movieId, quantity FROM carts " +
                    "WHERE email LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, crm.getEmail());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded to retrieve items.");

            // Retrieve the Items from the Result Set
            ArrayList<Item> items = new ArrayList<>();
            while (rs.next()) {
                Item i = new Item(
                        rs.getString("email"),
                        rs.getString("movieId"),
                        rs.getInt("quantity")
                );
                ServiceLogger.LOGGER.info("Retrieved item " + i);
                items.add(i);
            }

            // Got the items. Builds the response model
            return ItemsResponseModel.buildModelFromList(items);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve items from shopping cart.");
            e.printStackTrace();
        }
        // No items were retrieved. Build response model with null arraylist.
        ServiceLogger.LOGGER.info("No items were retrieved.");
        return ItemsResponseModel.buildModelFromList(null);
    }
    public static OrderPlaceResponseModel insertOrderIntoDB (MixRequestModel crm)
    {


        ServiceLogger.LOGGER.info("Inserting sale into sales table and transaction table...");
        try {

            String query = "SELECT movieId, quantity, email, unit_price, discount " +
                    "FROM carts JOIN movie_prices USING (movieID) " +
                    "WHERE email LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, crm.getEmail());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded to retrieve items.");
            // Retrieve the Items from the Result Set
            ArrayList<Float> sumList = new ArrayList<>();
            ArrayList<Item> items = new ArrayList<>();
            float cost = 0;
            while (rs.next())
            {
                Item i = new Item(
                        rs.getString("email"),
                        rs.getString("movieId"),
                        rs.getInt("quantity")
                );
                NewItem i2 = new NewItem(
                        rs.getInt("quantity"),
                        rs.getFloat("unit_price"),
                        rs.getFloat("discount")
                );
                items.add(i);
                cost = i2.getDiscount()*i2.getQuantity()*i2.getUnit_price();
                sumList.add(cost);
                ServiceLogger.LOGGER.info("Retrieved item:\n" + i.toString());
                ServiceLogger.LOGGER.info("Retrieved cost details:\n" + i2.toString());
                ServiceLogger.LOGGER.info("Calculated cost per item in cart: " + cost);
            }

            float sum = 0;
            for (float c: sumList){
                sum+=c;
            }
            String s = String.format("%.2f",sum);
            ServiceLogger.LOGGER.info("Calculated sum: " + sum);

            //Creating a payment here
            Amount amount = new Amount();
            amount.setCurrency("USD");
            amount.setTotal(s);
            //Creating a transaction for the total sum of items in the cart
            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            List<Transaction> transactions = new ArrayList<Transaction>();
            transactions.add(transaction);

            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            Payment payment = new Payment();
            payment.setIntent("sale");
            payment.setPayer(payer);
            payment.setTransactions(transactions);
            // NEED TO BUILD THIS URI FROM SERVICE CONFIGS
            ServiceLogger.LOGGER.info("Scheme: "+ BillingService.getConfigs().getScheme());
            ServiceLogger.LOGGER.info("HostName: "+ BillingService.getConfigs().getHostName());
            ServiceLogger.LOGGER.info("Path: "+ BillingService.getConfigs().getPath());
            ServiceLogger.LOGGER.info("Port: "+ BillingService.getConfigs().getPort());

            URI uri = UriBuilder.fromUri(BillingService.getConfigs().getScheme()+BillingService.getConfigs().getHostName()
                    +BillingService.getConfigs().getPath()).port(BillingService.getConfigs().getPort()).build();

            //"http://andromeda-70.ics.uci.edu:xxxx/<wherever you want to redirect the customer to>.html"
//            URI uri = UriBuilder.fromUri("http://"+"0.0.0.0"+"/api/billing").port(5359).build();
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl(uri.toString()+ "/cancel");
            redirectUrls.setReturnUrl(uri.toString() + "/order/complete"); //sucess URL

            payment.setRedirectUrls(redirectUrls);
            String redirectUrl = "";
            String token = "";
            String transactionId = "";
            try {
                APIContext apiContext = new APIContext(clientId, clientSecret, "sandbox"); //MAKE SURE THIS STILL WORKS AFTER MAKING GLOBAL VARS
                Payment createdPayment = payment.create(apiContext);
                ServiceLogger.LOGGER.info("CreatedPayment: "+ createdPayment);
                if(createdPayment != null)
                {
                    List<Links> links = createdPayment.getLinks();
                    for (Links link : links)
                    {
                        if (link.getRel().equals("approval_url")) {
                            redirectUrl = link.getHref();
                            ServiceLogger.LOGGER.info("Redirect URL: "+ redirectUrl);
                            String[] arr = redirectUrl.split("token=");
                            token = arr[1];
                            ServiceLogger.LOGGER.info("Token: "+ token);
                            break;
                        }
                    }
                }
            } catch (PayPalRESTException e) {
                System.err.println(e.getDetails());
            }
            long millis= System.currentTimeMillis();
            java.sql.Date date = new java.sql.Date(millis);
            for (Item i : items)
            {
                CallableStatement cStmt = BillingService.getCon().prepareCall("{call insert_sales_transactions(?, ?, ?, ?, ?, ?)}");
                cStmt.setString(1,i.getEmail());
                cStmt.setString(2, i.getMovieId());
                cStmt.setInt(3, i.getQuantity());
                cStmt.setDate(4, date);
                cStmt.setString(5, token);
                cStmt.setString(6, transactionId);
                ResultSet rs2 = cStmt.executeQuery();
            }

            return new OrderPlaceResponseModel(3400,"Order placed successfully.",redirectUrl,token); //MAKE SURE THIS IS IN THE RIGHT SPOT
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return new OrderPlaceResponseModel(342,"Create payment failed.",null,null);
    }
    public static OrdResponseModel retrieveOrdersFromDB(MixRequestModel crm) {
        try {
            // Construct the query
            String query = "SELECT DISTINCT transactionId, s1.movieId, quantity, email, unit_price, discount, saleDate " + //Note that one transactionId might be mapped to many sales.
                    "FROM sales s1 JOIN transactions ON (s1.id = transactions.sId)" +
                    "JOIN movie_prices USING (movieID)  " +
                    "WHERE s1.email = ?;";
//                    "GROUP BY transactionId;";

            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, crm.getEmail());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded to retrieve transaction.");
            // Retrieve the Items from the Result Set
            ArrayList<Order> orders = new ArrayList<>();
//            ArrayList<Order> orders2 = new ArrayList<>();

            ArrayList<edu.uci.ics.gmehta1.service.billing.core.Transaction> transactions = new ArrayList<>();
            PreparedStatement ps2 = BillingService.getCon().prepareStatement(query);
            ps2.setString(1, crm.getEmail());

            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next())
            {
                Order o = new Order(rs2.getString("transactionId"),rs2.getString("email"), rs2.getString("movieId"), rs2.getInt("quantity"), rs2.getFloat("unit_price"), rs2.getFloat("discount"), rs2.getDate("saleDate"));
                orders.add(o);
            }
            int len = orders.size();
            OrderModel[] array = new OrderModel[len];
            ServiceLogger.LOGGER.info("Orders size: " + Integer.toString(len));
            for (int i = 0; i < len; ++i) {
                ServiceLogger.LOGGER.info("Adding item " + orders.get(i).getMovieId() + " to array.");
                OrderModel om = OrderModel.buildModelFromObject(orders.get(i));
                array[i] = om;
            }
            APIContext apiContext = new APIContext(clientId, clientSecret, "sandbox");
            ObjectMapper mapper = new ObjectMapper();
            AmountModel am = null;
            Transaction_FeeModel tfm = null;
            while (rs.next())
            {
                Sale sale = Sale.get(apiContext, rs.getString("transactionId"));
                am = mapper.readValue(sale.getAmount().toString(), AmountModel.class); // Unsure if this will map to the correct constructor
                tfm = mapper.readValue(sale.getTransactionFee().toString(), Transaction_FeeModel.class); // Unsure if this will map to the correct constructor
                edu.uci.ics.gmehta1.service.billing.core.Transaction t =
                        new edu.uci.ics.gmehta1.service.billing.core.Transaction(sale.getId(), sale.getState(), am,
                                tfm, sale.getCreateTime(), sale.getUpdateTime(), array);
                transactions.add(t);
            }
            return OrdResponseModel.buildModelFromList(transactions);
        } catch (SQLException | IOException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve transacions.");
            e.printStackTrace();
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        ServiceLogger.LOGGER.info("No transactions were retrieved.");
        return OrdResponseModel.buildModelFromList(null);
    }
    public static OrderResponseModel updateTransactionsInDB(CompleteRequestModel crm)
    {
        Payment payment = new Payment();
        payment.setId(crm.getPaymentId());

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(crm.getPayerId());

        String transactionId ="";
        try {
            APIContext context = new APIContext(clientId, clientSecret, "sandbox");
            Payment createdPayment = payment.execute(context, paymentExecution);
            transactionId = createdPayment.getTransactions().get(0).getRelatedResources().get(0).getSale().getId();
            ServiceLogger.LOGGER.info("TransactionId: "+ transactionId);
            ServiceLogger.LOGGER.info("Created Payment that has everything: "+ createdPayment);

        } catch (PayPalRESTException e) {
            System.err.println(e.getDetails());
        }

        try {
            // Construct the query
            String query = "UPDATE transactions " +
                    "SET transactionId = ? " +
                    "WHERE token LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, transactionId);
            ps.setString(2, crm.getToken());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            if (ps.executeUpdate() == 0)
            {
                return new OrderResponseModel(3421,"Token not found.");
            }
            ServiceLogger.LOGGER.info("Query succeeded to update transaction table .");

            return new OrderResponseModel(3420,"Payment is completed successfully.");
        } catch (SQLException e)
        {
            ServiceLogger.LOGGER.warning("Query failed: Unable to update transaction table.");
            e.printStackTrace();
        }
        return new OrderResponseModel(3422,"Payment can not be completed.");
    }

}
