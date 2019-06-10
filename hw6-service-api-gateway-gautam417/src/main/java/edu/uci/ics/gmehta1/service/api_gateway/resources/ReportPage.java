package edu.uci.ics.gmehta1.service.api_gateway.resources;

import edu.uci.ics.gmehta1.service.api_gateway.GatewayService;
import edu.uci.ics.gmehta1.service.api_gateway.logger.ServiceLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("report")
public class ReportPage {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response report(@Context HttpHeaders headers) {
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);

        // Get response From database
        try{
            String query = "SELECT response, httpstatus " +
                    "FROM responses " +
                    "WHERE transactionId = ?;";
            Connection con = GatewayService.getConPool().requestCon();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, transactionID);

            ResultSet rs = ps.executeQuery();

            int getHttpStatus;
            String responseModel;
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            rs.next();
            {
                getHttpStatus = rs.getInt("httpstatus");
                responseModel = rs.getString("response");
            }
            GatewayService.getConPool().releaseCon(con);
            return Response.status(Response.Status.fromStatusCode(getHttpStatus)).header("Access-Control-Expose-Headers", "*") .header("Access-Control-Allow-Headers", "*").entity(responseModel).build(); // THIS ALSO NEEDS ACCESSCONTROL
        } catch (SQLException e)
        {
            ServiceLogger.LOGGER.warning("SELECT Query in Report issue.");
            e.printStackTrace();
        }
//
        return Response.status(Response.Status.NO_CONTENT).header("Access-Control-Expose-Headers", "*") .header("Access-Control-Allow-Headers", "*").header("transactionID" ,transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
    }
}
