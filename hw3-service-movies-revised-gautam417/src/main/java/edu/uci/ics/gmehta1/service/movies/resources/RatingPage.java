package edu.uci.ics.gmehta1.service.movies.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.gmehta1.service.movies.core.RatingRecords;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.RatingRequestModel;
import edu.uci.ics.gmehta1.service.movies.models.StarAddResponseModel;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("rating")     //ONLY TEST THIS EP AFTER INSERT IS FINISHED SO THAT DATA DOES NOT NEED TO BE REINSERTED
public class RatingPage {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response rating(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " + email);
        ServiceLogger.LOGGER.info("SESSIONID: " + sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " + transactionID);

        ServiceLogger.LOGGER.info("Received request to update a movie with a new rating. " +
                "Must contain the rating the user gave the movie. " +
                "Endpoint updates the overall rating and increments the vote count.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");

        ObjectMapper mapper = new ObjectMapper();
        RatingRequestModel rrm;
        StarAddResponseModel responseModel;
        try {
            rrm = mapper.readValue(jsonText, RatingRequestModel.class);
            //FIRST RUN A FUNC TO CHECK IF MOVIE EXISTS!!
            //RETURN 211 IS IT DOESNT
            responseModel = RatingRecords.updateRatingFromDB(rrm);
            return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID",transactionID).build();
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new StarAddResponseModel(-3, "JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new StarAddResponseModel(-2, "JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else {
                ServiceLogger.LOGGER.warning("IOException.");
            }
        } catch (InternalServerErrorException e) {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
        }
        return null;
    }
}