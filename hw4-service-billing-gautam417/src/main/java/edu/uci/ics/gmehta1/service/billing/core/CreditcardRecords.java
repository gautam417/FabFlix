package edu.uci.ics.gmehta1.service.billing.core;

import edu.uci.ics.gmehta1.service.billing.BillingService;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.billing.models.CreditResponseModel;
import edu.uci.ics.gmehta1.service.billing.models.CreditcardRequestModel;
import edu.uci.ics.gmehta1.service.billing.models.CreditcardResponseModel;
import edu.uci.ics.gmehta1.service.billing.models.IdRequestModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

public class CreditcardRecords {
    public static CreditcardResponseModel insertCreditcardIntoDB (CreditcardRequestModel crm)
    {
        ServiceLogger.LOGGER.info("Inserting creditcard info into creditcards table...");
        try {
            // Construct the query
            String query =
                    "INSERT INTO creditcards (id, firstName, lastName, expiration) VALUES (?, ?, ?, ?);";
            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            // Set the paremeters

            ps.setString(1, crm.getId());
            ps.setString(2, crm.getFirstName());
            ps.setString(3, crm.getLastName());
            ps.setDate(4, crm.getExpiration());
            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.execute();
            return new CreditcardResponseModel (3200,"Credit card inserted successfully.");
        } catch (SQLIntegrityConstraintViolationException e)
        {
            return new CreditcardResponseModel(325,"Duplicate insertion.");
        }
        catch (SQLException e)
        {
            ServiceLogger.LOGGER.warning("Unable to insert creditcard info for customer: " + crm.getFirstName());
            e.printStackTrace();
        }
        return new CreditcardResponseModel(325,"Duplicate insertion.");
    }
    public static CreditcardResponseModel updateCreditcardFromDB(CreditcardRequestModel crm)
    {
//        ItemsResponseModel responseModel;
        try {
            // Construct the query
            String query = "UPDATE creditcards " +
                    "SET firstName = ?, lastName = ?, expiration = ? " +
                    "WHERE id LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, crm.getFirstName());
            ps.setString(2, crm.getLastName());
            ps.setDate(3, crm.getExpiration());
            ps.setString(4, crm.getId());


            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            if (ps.executeUpdate() == 0)
            {
                return new CreditcardResponseModel(324,"Credit card does not exist.");
            }
            ServiceLogger.LOGGER.info("Query succeeded to update creditcard info .");
            return new CreditcardResponseModel(3210,"Credit card updated successfully.");
        } catch (SQLException e)
        {
            ServiceLogger.LOGGER.warning("Query failed: Unable to update creditcard from creditcards table.");
            e.printStackTrace();
        }
        ServiceLogger.LOGGER.info("SOMETHING WEIRD HAPPENED.");
        return new CreditcardResponseModel(324,"Credit card does not exist.");
    }
    public static CreditcardResponseModel deleteCreditCardFromDB(IdRequestModel crm)
    {
        try {
        // Construct the query
        String query = "DELETE FROM creditcards " +
                "WHERE id LIKE ?;";

        // Create the prepared statement
        PreparedStatement ps = BillingService.getCon().prepareStatement(query);

        // Set the arguments
        ps.setString(1, crm.getId());

        // Save the query result to a Result Set so records may be retrieved
        ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
        if (ps.executeUpdate() == 0)
        {
            ServiceLogger.LOGGER.info("No changes made to DB, query succeeded still.");
            return new CreditcardResponseModel(324,"Credit card does not exist.");
        }
        ServiceLogger.LOGGER.info("Query succeeded to clear cart .");
        return new CreditcardResponseModel(3220,"Credit card deleted successfully.");
    } catch (SQLException e) {
        ServiceLogger.LOGGER.warning("Query failed: Unable to delete creditcard.");
        e.printStackTrace();
    }
        return new CreditcardResponseModel(324,"Credit card does not exist.");
    }
    public static CreditResponseModel retrieveCreditcardFromDB(IdRequestModel crm)
    {
//        ItemsResponseModel responseModel;
        try {
            // Construct the query
            String query = "SELECT * FROM creditcards " +
                    "WHERE id LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, crm.getId());

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
                return CreditResponseModel.buildModelFromList(c);
            }
            // Got the items. Builds the response model
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve creditcard from creditcards table.");
            e.printStackTrace();
        }
        // No items were retrieved. Build response model with null arraylist.
        ServiceLogger.LOGGER.info("No items were retrieved.");
        return CreditResponseModel.buildModelFromList(null);
    }
}
