package edu.uci.ics.gmehta1.service.billing.core;


import edu.uci.ics.gmehta1.service.billing.BillingService;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.billing.models.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

public class CartRecords
{
    public static CartResponseModel insertItemIntoDB (CartRequestModel crm)
    {
        ServiceLogger.LOGGER.info("Inserting item into Customers cart...");
        try {
            // Construct the query
            String query =
                    "INSERT INTO carts (email, movieId, quantity) VALUES (?, ?, ?);";
            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            // Set the paremeters

            ps.setString(1, crm.getEmail());
            ps.setString(2, crm.getMovieId());
            ps.setInt(3, crm.getQuantity());
            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.execute();
            return new CartResponseModel (3100,"Shopping cart item inserted successfully.");
        } catch (SQLIntegrityConstraintViolationException e)
        {
            return new CartResponseModel(311,"Duplicate insertion.");
        }
        catch (SQLException e)
        {
            ServiceLogger.LOGGER.warning("Unable to insert item for customer: " + crm.getEmail());
            e.printStackTrace();
        }
        return new CartResponseModel(311,"Duplicate insertion.");
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
    public static CartResponseModel clearItemsFromDB(MixRequestModel crm)
    {
//        ItemsResponseModel responseModel;
        try {
            // Construct the query
            String query = "DELETE FROM carts " +
                    "WHERE email LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, crm.getEmail());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.execute();
            ServiceLogger.LOGGER.info("Query succeeded to clear cart.");
            return new CartResponseModel(3140,"Shopping cart cleared successfully.");
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to clear items from shopping cart.");
            e.printStackTrace();
        }
        ServiceLogger.LOGGER.info("SOMETHING WEIRD HAPPENED.");
        return new CartResponseModel(3140,"Shopping cart cleared successfully.");
    }
    public static CartResponseModel deleteItemFromDB(DeleteRequestModel crm)
    {
//        ItemsResponseModel responseModel;
        try {
            // Construct the query
            String query = "DELETE FROM carts " +
                    "WHERE email LIKE ? AND movieId LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, crm.getEmail());
            ps.setString(2, crm.getMovieId());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            if (ps.executeUpdate() == 0)
            {
                return new CartResponseModel(312,"Shopping item does not exist.");
            }
            ServiceLogger.LOGGER.info("Query succeeded to clear cart .");
            return new CartResponseModel(3120,"Shopping cart item deleted successfully.");
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to delete item from shopping cart.");
            e.printStackTrace();
        }
        return new CartResponseModel(312,"Shopping item does not exist.");
    }
    public static CartResponseModel updateItemFromDB(CartRequestModel crm)
    {
//        ItemsResponseModel responseModel;
        try {
            // Construct the query
            String query = "UPDATE carts " +
                    "SET email = ?, movieId = ?, quantity = ? " +
                    "WHERE email LIKE ? AND movieId LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, crm.getEmail());
            ps.setString(2, crm.getMovieId());
            ps.setInt(3, crm.getQuantity());
            ps.setString(4, crm.getEmail());
            ps.setString(5, crm.getMovieId());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            if (ps.executeUpdate() == 0)
            {
                return new CartResponseModel(312,"Shopping item does not exist.");
            }
            ServiceLogger.LOGGER.info("Query succeeded to update cart .");
            return new CartResponseModel(3110,"Shopping cart item updated successfully.");
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to delete item from shopping cart.");
            e.printStackTrace();
        }
        ServiceLogger.LOGGER.info("SOMETHING WEIRD HAPPENED.");
        return new CartResponseModel(312,"Shopping item does not exist.");
    }

}


