package edu.uci.ics.gmehta1.service.billing.core;

import edu.uci.ics.gmehta1.service.billing.BillingService;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.billing.models.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class CustomerRecords {
    public static CustomerResponseModel insertCustomerIntoDB (CustomerRequestModel crm)
    {
        ServiceLogger.LOGGER.info("Inserting customer into Customers table...");
        try {
            // Construct the query
            String query =
                    "INSERT INTO customers (email, firstName, lastName, ccId, address) VALUES (?, ?, ?, ?, ?);";
            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            // Set the paremeters

            ps.setString(1, crm.getEmail());
            ps.setString(2, crm.getFirstName());
            ps.setString(3, crm.getLastName());
            ps.setString(4, crm.getCcId());
            ps.setString(5, crm.getAddress());
            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.execute();
            return new CustomerResponseModel (3300,"Customer inserted successfully.");
        } catch (SQLIntegrityConstraintViolationException e)
        {
            try {
                // Construct the query
                String query = "SELECT * FROM creditcards " +
                        "WHERE id LIKE ?;";

                // Create the prepared statement
                PreparedStatement ps = BillingService.getCon().prepareStatement(query);

                // Set the arguments
                ps.setString(1, crm.getCcId());

                // Save the query result to a Result Set so records may be retrieved
                ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
                ResultSet rs = ps.executeQuery();
                ServiceLogger.LOGGER.info("Query succeeded to retrieve items.");
                // Retrieve the Items from the Result Set
                while (rs.next())
                {
                    Creditcard c = new Creditcard(
                            rs.getString("id"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getDate("expiration")
                    );
                    ServiceLogger.LOGGER.info("Retrieved creditcard " + c);
                    return CreditResponseModel.buildModelFromList2(c);
                }
            } catch (SQLException e2) {
                ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve creditcard from creditcards table.");
                e2.printStackTrace();
            }
            // No items were retrieved. Build response model with null arraylist.
            ServiceLogger.LOGGER.info("No items were retrieved.");
            return CreditResponseModel.buildModelFromList2(null);
        }
        catch (SQLException e)
        {
            ServiceLogger.LOGGER.warning("Unable to insert item for customer: " + crm.getEmail());
            e.printStackTrace();
        }
        ServiceLogger.LOGGER.info("SOMETHING WEIRD HAPPENED");
        return new CustomerResponseModel(3300,"Customer inserted successfully.");
    }
    public static CustomerResponseModel updateCustomerFromDB(CustomerRequestModel crm)
    {
//        ItemsResponseModel responseModel;
        try {
            // Construct the query
            String query = "UPDATE customers " +
                    "SET email = ?, firstName = ?, lastName = ?, ccId = ?, address = ? " +
                    "WHERE email LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, crm.getEmail());
            ps.setString(2, crm.getFirstName());
            ps.setString(3, crm.getLastName());
            ps.setString(4, crm.getCcId());
            ps.setString(5, crm.getAddress());
            ps.setString(6, crm.getEmail());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            if (ps.executeUpdate() == 0)
            {
                return new CustomerResponseModel(332,"Customer does not exist.");
            }
            ServiceLogger.LOGGER.info("Query succeeded to update customer .");
            return new CustomerResponseModel(3310,"Customer updated successfully.");
        }catch (SQLIntegrityConstraintViolationException e)
        {
            try {
                // Construct the query
                String query = "SELECT * FROM creditcards " +
                        "WHERE id LIKE ?;";

                // Create the prepared statement
                PreparedStatement ps = BillingService.getCon().prepareStatement(query);

                // Set the arguments
                ps.setString(1, crm.getCcId());

                // Save the query result to a Result Set so records may be retrieved
                ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
                ResultSet rs = ps.executeQuery();
                ServiceLogger.LOGGER.info("Query succeeded to retrieve items.");
                // Retrieve the Items from the Result Set
                while (rs.next())
                {
                    Creditcard c = new Creditcard(
                            rs.getString("id"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getDate("expiration")
                    );
                    ServiceLogger.LOGGER.info("Retrieved creditcard " + c);
                    return CreditResponseModel.buildModelFromList3(c);
                }
            } catch (SQLException e2) {
                ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve creditcard from creditcards table.");
                e2.printStackTrace();
            }
            // No items were retrieved. Build response model with null arraylist.
            ServiceLogger.LOGGER.info("No items were retrieved.");
            return CreditResponseModel.buildModelFromList3(null);
        }
        catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to delete item from shopping cart.");
            e.printStackTrace();
        }
        ServiceLogger.LOGGER.info("SOMETHING WEIRD HAPPENED.");
        return new CustomerResponseModel(3310,"Customer updated successfully.");
    }

    public static CustomResponseModel retrieveCustomerFromDB(MixRequestModel crm)
    {
//        ItemsResponseModel responseModel;
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
}
