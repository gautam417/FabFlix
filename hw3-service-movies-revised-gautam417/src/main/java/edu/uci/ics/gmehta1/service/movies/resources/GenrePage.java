package edu.uci.ics.gmehta1.service.movies.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.GenreAddRequestModel;
import edu.uci.ics.gmehta1.service.movies.models.GenreAddResponseModel;
import edu.uci.ics.gmehta1.service.movies.models.GenreResponseModel;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static edu.uci.ics.gmehta1.service.movies.core.GenreRecords.*;

@Path("genre")
public class GenrePage {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGenreList(@Context HttpHeaders headers) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " + email);
        ServiceLogger.LOGGER.info("SESSIONID: " + sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " + transactionID);
        ServiceLogger.LOGGER.info("Received request to return a list of all genres in the database.");
        GenreResponseModel responseModel;
        //NOT CHECKING PRIVILEGE LVL HERE ANYMORE
        try {
            responseModel = retrieveGenresFromDB();
            return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID",transactionID).build();
        } catch (InternalServerErrorException e) {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).build();
        }
    }

    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addGenre(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " + email);
        ServiceLogger.LOGGER.info("SESSIONID: " + sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " + transactionID);
        ServiceLogger.LOGGER.info("Received request to add a genre to the database.");
        ObjectMapper mapper = new ObjectMapper();
        GenreAddRequestModel garm;
        GenreAddResponseModel responseModel;
        //CHECK PRIVILEGE LVL HERE
        try {
            garm = mapper.readValue(jsonText, GenreAddRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Name: " + garm.getName());
            responseModel = addIntoDB(garm);
            return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
        }catch(IOException e)
        {
            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new GenreAddResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new GenreAddResponseModel(-2,"JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            } else {
                ServiceLogger.LOGGER.warning("IOException .");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
        }
        return null;
    }
    @GET
    @Path("{movieId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMovieRecord(@Context HttpHeaders headers, @PathParam("movieId") String movieId)
    {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("MovieId: " + movieId);
        ServiceLogger.LOGGER.info("Received request to return the IDs and names of the genres a movie belongs to.");

        GenreResponseModel responseModel = null;
        //CHECK PRIVILEGE LVL HERE
        //THIS ENDPOINT MUST RETURN A VerifyPrivilegeResponseModel IF THE USER HAS INSUFFICIENT PRIVILEGE!
        try {

            responseModel = retrieveGenresById(movieId);
            return Response.status(Response.Status.OK).entity(responseModel).build();
        } catch (InternalServerErrorException e) {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).build();
        }
    }
}
