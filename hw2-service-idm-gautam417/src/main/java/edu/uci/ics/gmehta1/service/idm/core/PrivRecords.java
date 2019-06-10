package edu.uci.ics.gmehta1.service.idm.core;

import edu.uci.ics.gmehta1.service.idm.IDMService;
import edu.uci.ics.gmehta1.service.idm.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.idm.models.PrivilegeRequestModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PrivRecords {
    public static boolean retrieveByEmailFromDB(PrivilegeRequestModel requestModel) {
        try {
            // Construct the query
            String query = "SELECT email FROM users " +
                    "WHERE email LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, requestModel.getEmail());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeed.");

            // Retrieve the emails from the Result Set
            ArrayList<Email> emails = new ArrayList<>();
            while (rs.next()) {
                Email e = new Email(rs.getString("email"));
                emails.add(e);
            }
            if (emails.size() >= 1)
                return true;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed");
            e.printStackTrace();
        }
        // No strings were retrieved.
        ServiceLogger.LOGGER.info("No emails found in DB.");
        return false;
    }

    public static boolean CheckPrivFromDB(PrivilegeRequestModel requestModel) {
        try {
            // Construct the query
            String query = "SELECT email,plevel FROM users " +
                    "WHERE email LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, requestModel.getEmail());
//            ps.setInt(2, requestModel.getPlevel());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("query succeed.");

            // Retrieve the emails from the Result Set
            ArrayList<Priv> privs = new ArrayList<>();
            while (rs.next()) {
                Priv p = new Priv(rs.getString("email"), rs.getInt("plevel"));
                ServiceLogger.LOGGER.info("request p level: " + requestModel.getPlevel() );
                ServiceLogger.LOGGER.info("DB p level: " +  p.getPlevel() );

                if (p.getPlevel() > requestModel.getPlevel()) {
                    return false;
                }
                return true;
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed to get email and plevel from DB");
            e.printStackTrace();
        }
        // No strings were retrieved.
        ServiceLogger.LOGGER.info("No emails found in DB.");
        return false;
    }
//    public static int GetPrivLvl(String email) {
//        try {
//            // Construct the query
//            String query = "SELECT plevel FROM users " +
//                    "WHERE email LIKE ?;";
//
//            // Create the prepared statement
//            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
//
//            // Set the arguments
//            ps.setString(1, email);
//            // Save the query result to a Result Set so records may be retrieved
//            ServiceLogger.LOGGER.info("trying query to get Plvl: " + ps.toString());
//            ResultSet rs = ps.executeQuery();
//            ServiceLogger.LOGGER.info("query succeed.");
//
//            // Retrieve the emails from the Result Set
//            while (rs.next()) {
//                Priv p = new Priv(rs.getString("email"), rs.getInt("plevel"));
//                ServiceLogger.LOGGER.info("DB p level: " +  p.getPlevel() );
//                return p.getPlevel();
//            }
//        } catch (SQLException e) {
//            ServiceLogger.LOGGER.warning("Query failed to get email and plevel from DB");
//            e.printStackTrace();
//        }
//        // No strings were retrieved.
//        ServiceLogger.LOGGER.info("No emails found in DB.");
//        return 0;
//    }
}
