package edu.uci.ics.gmehta1.service.idm.core;

import edu.uci.ics.gmehta1.service.idm.IDMService;
import edu.uci.ics.gmehta1.service.idm.configs.Configs;
import edu.uci.ics.gmehta1.service.idm.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.idm.models.SessionRequestModel;
import edu.uci.ics.gmehta1.service.idm.security.Session;
import edu.uci.ics.gmehta1.service.idm.security.Token;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class SessionRecords {
    public static boolean retrieveByEmailFromDB(SessionRequestModel requestModel) {
        try {
            // Construct the query
            String query = "SELECT email FROM users " +
                    "WHERE email = ?;";

            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, requestModel.getEmail());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded. ");

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
    public static boolean retrieveSessionFromDB(SessionRequestModel requestModel)
    {
        try{
            // Construct the query
            String query = "SELECT sessionID,email FROM sessions " +
                    "WHERE sessionID,email LIKE ?,?;";

            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, requestModel.getSessionID());
            ps.setString(2, requestModel.getEmail());


            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded. ");

            // Retrieve the emails from the Result Set
            ArrayList<Sessions> sessions = new ArrayList<>();
            while(rs.next())
            {
                Sessions s = new Sessions(rs.getString("sessionID"),rs.getString("email"));
                sessions.add(s);
            }
            if (sessions.size() >= 1)
                return true;

        }catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve valid string record by id.");
            e.printStackTrace();
        }
        ServiceLogger.LOGGER.info("No sessions found in DB.");
        return false;
    }
    public static int VerifySessionFromDB(SessionRequestModel requestModel) {
        try {
            // Construct the query
            String query = "SELECT sessionID, email, status, timeCreated, lastUsed, exprTime FROM sessions " +
                    "WHERE sessionID LIKE ?;";
            Configs configs = IDMService.getConfigs();
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);

            // Set the arguments
            ps.setString(1, requestModel.getSessionID());

            // Save the query result to a Result Set so records may be retrieved
            ServiceLogger.LOGGER.info("Trying VERIFY SESSSION query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded. ");

            Timestamp currentTime = new Timestamp(System.currentTimeMillis()); //t1
            Session session;
            rs.next();{
            session = Session.rebuildSession(requestModel.getEmail(), Token.rebuildToken(requestModel.getSessionID()),rs.getTimestamp("timeCreated"),
                    rs.getTimestamp("lastUsed"),
                    rs.getTimestamp("exprTime"));
                if (rs.getInt("status")  == 1 ) { //(currentTime.getTime()-session.getLastUsed().getTime() < configs.getTimeout())||
                    session.update();
//                    RevokeSesh(session);
//                    createSesh(session);
                    return 0; //ACTIVE
                }
                else if (rs.getInt("status") == 4) { //currentTime.getTime()-session.getLastUsed().getTime() > configs.getTimeout() ||
                    ServiceLogger.LOGGER.info("New Session Obj's: " + session.getSessionID());
                    session.update();
                    RevokeSesh(session);
                    createSesh(session);
                    return 3; //REVOKED
                }
                else if (!session.isDataValid()|| rs.getInt("status") == 3) {
                    session.update();
                    expireSesh(session);
//                    createSesh(session);
                    return 1;//EXPIRED
                }
                else if(rs.getInt("status")  == 2) {
                    return 2; //CLOSED
                }

            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve valid string record by id.");
            e.printStackTrace();
        }
        ServiceLogger.LOGGER.info("No sessions found in DB.");
        return 5;
    }
    private static void RevokeSesh (Session s){
        try{
            String query = "UPDATE sessions, users " +
                    "SET sessions.status = 4 " +
                    "WHERE users.email = \'" + s.getEmail()  +
                    "\' AND sessions.status = 1 AND sessions.sessionID = \'" + s.getSessionID() + "\';";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ServiceLogger.LOGGER.info("Trying Revoke sesh query: " + ps.toString());
            ps.executeUpdate();
            ServiceLogger.LOGGER.info("Query succeeded. ");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void expireSesh (Session s){
        try{
            String query = "UPDATE sessions, users " +
                    "SET sessions.status = 3 " +
                    "WHERE users.email = \'" + s.getEmail() +
                    "\' AND sessions.status = 1 AND sessions.sessionID = \'" + s.getSessionID() + "\';";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ServiceLogger.LOGGER.info("Trying expire Sesh query: " + ps.toString());

            ps.executeUpdate();
            ServiceLogger.LOGGER.info("Query succeeded. ");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void createSesh(Session s){
        Session newSesh;
        newSesh =Session.createSession(s.getEmail());

        try{
            String query = "INSERT INTO sessions(sessionID, email, status, timeCreated, lastUsed, exprTime) " +
                    "VALUES (?, ?, ?, ?, ?, ?);";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setString(1, newSesh.getSessionID().toString());
            ps.setString(2, newSesh.getEmail());
            ps.setInt(3,1);
            ps.setTimestamp(4, newSesh.getTimeCreated());
            ps.setTimestamp(5, newSesh.getLastUsed());
            ps.setTimestamp(6, newSesh.getExprTime());
            ServiceLogger.LOGGER.info("Trying create Sesh query: " + ps.toString());

            ps.executeUpdate();
            ServiceLogger.LOGGER.info("Query succeeded. ");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
