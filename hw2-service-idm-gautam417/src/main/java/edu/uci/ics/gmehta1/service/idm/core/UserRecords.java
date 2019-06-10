package edu.uci.ics.gmehta1.service.idm.core;

import edu.uci.ics.gmehta1.service.idm.IDMService;
import edu.uci.ics.gmehta1.service.idm.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.idm.models.RegisterRequestModel;
import edu.uci.ics.gmehta1.service.idm.security.Crypto;
import org.apache.commons.codec.binary.Hex;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRecords {
    public static boolean insertUserToDb(RegisterRequestModel requestModel){
        ServiceLogger.LOGGER.info("Inserting registered user into database...");
        try {
            // Construct the query
            // Salt and hash provided password
            byte[] salt = Crypto.genSalt();
            String hashedPass = Hex.encodeHexString(Crypto.hashPassword(requestModel.getPassword(), salt, Crypto.ITERATIONS, Crypto.KEY_LENGTH));
            String encodedSalt = Hex.encodeHexString(salt);
            // Store the hashed + encoded password, and the encoded salt in the database
            String query =
                    "INSERT INTO users (email, status, plevel, salt, pword) VALUES (?, ?, ?, ?, ?);";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            // Set the paremeters
            ps.setString(1, requestModel.getEmail());
            ps.setInt(2, 1);
            ps.setInt(3,5);
            ps.setString(4, encodedSalt);
            ps.setString(5, hashedPass);
            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.execute();
            return true;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Unable to insert user with email " + requestModel.getEmail());
            e.printStackTrace();
        }
        return false;
    }
    public static boolean retrieveByEmailFromDB(RegisterRequestModel requestModel) {
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
            ServiceLogger.LOGGER.info("Query succeeded.");

            // Retrieve the emails from the Result Set
            ArrayList<Email> emails = new ArrayList<>();
            while(rs.next()){
                Email e = new Email(rs.getString("email"));
                emails.add(e);
            }
            if (emails.size() >= 1)
                return true;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve valid string record by id.");
            e.printStackTrace();
        }
        // No strings were retrieved.
        ServiceLogger.LOGGER.info("No emails found in DB.");
        return false;
    }
    public static boolean retrievePassFromDB(RegisterRequestModel requestModel) { //AND COMPARE
        try {

//            String stringSalt = getHashedPass(salt);
            // Construct the query
            String query = "SELECT email, salt, pword FROM users " +
                    "WHERE email LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, requestModel.getEmail());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query:" + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            // Retrieve the emails from the Result Set
            while (rs.next())
            {

                String dbSalt = rs.getString("salt");
                byte [] salt = convert(dbSalt);
                String dbPword = rs.getString("pword");
                String hashedPass = Hex.encodeHexString(Crypto.hashPassword(requestModel.getPassword(), salt, Crypto.ITERATIONS, Crypto.KEY_LENGTH));
                String encodedSalt = Hex.encodeHexString(salt);
//                ServiceLogger.LOGGER.info("Salt from db: "+ dbSalt);
//                ServiceLogger.LOGGER.info("Byte arr from db salt I made : "+ salt);
//                ServiceLogger.LOGGER.info("Pword I made : "+ hashedPass);
//                ServiceLogger.LOGGER.info("Pword from db: "+ dbPword);
                if (hashedPass.equals(dbPword)){
                    ServiceLogger.LOGGER.info("Something matched.");
                    return true;
                }
            }

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve email, salt, pword by email.");
            e.printStackTrace();
        }
        // No strings were retrieved.
        ServiceLogger.LOGGER.info("Passwords dont match in DB.");
        return false;
    }
    private static byte[] convert(String tok) {
        int len = tok.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(tok.charAt(i), 16) << 4) + Character.digit(tok.charAt(i + 1), 16));
        }
        return data;
    }
}
