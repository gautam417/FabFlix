package edu.uci.ics.gmehta1.service.idm.core;

import edu.uci.ics.gmehta1.service.idm.IDMService;
import edu.uci.ics.gmehta1.service.idm.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.idm.models.RegisterRequestModel;
import edu.uci.ics.gmehta1.service.idm.security.Session;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

//- Check database, table "sessions" to see if any session attached to the email currently trying to log in has a status labelled "Active".
//- If there is, use sql commands to mark the session in the table as "Revoked" and create a new one with Session.createSession(<email logging in>).
//  Add to the table as "Active".
//- If there isn't, create a new session with Session.createSession(<email logging in>).
//  Add to the "sessions" table with the status "Active".

public class LoginRecords {
    public static String retrieveLoginFromDB(RegisterRequestModel requestModel){
        try {
            // Construct the query
            String query = "SELECT sessionID,email,status,timeCreated,lastUsed,exprTime FROM sessions " +
                    "WHERE email LIKE ?;";

            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            // Set the arguments
            ps.setString(1, requestModel.getEmail());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query:" + ps.toString());
            ResultSet rs = ps.executeQuery();

            ServiceLogger.LOGGER.info("Query succeeded.");

            // Retrieve the emails from the Result Set
            ArrayList<Login> logins = new ArrayList<>();
            while(rs.next()){
                Login l = new Login(rs.getString("sessionID"),rs.getString("email"),rs.getInt("status"));
                ServiceLogger.LOGGER.info("Retrieved login: " + l.getSessionID());
                if (l.getStatus() == 1){
                    try{
                        String query2 = "UPDATE sessions " +
                                "SET status = 4 " +
                                "WHERE sessionID LIKE ?;";
                        PreparedStatement ps2 = IDMService.getCon().prepareStatement(query2);

                        // Set the arguments
                        ps2.setString(1, l.getSessionID());

                        // Save the query result to a Result Set so records may be retrieved
                        ServiceLogger.LOGGER.info("Trying UPDATE query:" + ps2.toString());
                        ps2.executeUpdate();
                        ServiceLogger.LOGGER.info("UPDATE query succeeded.");
//                        rs.refreshRow();
                        Session newSesh = Session.createSession(l.getEmail());
                        rs.moveToInsertRow();
                        rs.updateString("sessionID", newSesh.getSessionID().toString());
                        rs.updateString("email", l.getEmail());
                        rs.updateInt("status", 1);
                        rs.updateTimestamp("timeCreated", newSesh.getTimeCreated());
                        rs.updateTimestamp("lastUsed", newSesh.getLastUsed());
                        rs.updateTimestamp("exprTime", newSesh.getExprTime());
                        rs.insertRow();
                        return newSesh.getSessionID().toString();
                    }catch (SQLException e) {
                        ServiceLogger.LOGGER.warning("Query failed");
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed.");
            e.printStackTrace();
        }
        try{
            Session newSesh2 = Session.createSession(requestModel.getEmail());

            // Construct the query
            String query3 = "INSERT INTO sessions " +
                    "VALUES (?, ?, ?, ?, ?, ?);";

            // Create the prepared statement
            PreparedStatement ps3 = IDMService.getCon().prepareStatement(query3);

            // Set the arguments
            ps3.setString(1, newSesh2.getSessionID().toString());
            ps3.setString(2, newSesh2.getEmail());
            ps3.setInt(3, 1);
            ps3.setTimestamp(4, newSesh2.getTimeCreated());
            ps3.setTimestamp(5, newSesh2.getLastUsed());
            ps3.setTimestamp(6, newSesh2.getExprTime());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Insertion query for sessions:" + ps3.toString());
            ps3.executeUpdate();

            ServiceLogger.LOGGER.info("Insertion Query for sessions succeeded.");
            return newSesh2.getSessionID().toString();
        }catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Insertion Query failed.");
            e.printStackTrace();
        }
        return null;
    }

}
