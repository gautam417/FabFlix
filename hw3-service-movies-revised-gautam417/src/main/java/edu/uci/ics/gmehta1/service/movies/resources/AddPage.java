package edu.uci.ics.gmehta1.service.movies.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.AddRequestModel;
import edu.uci.ics.gmehta1.service.movies.models.AddResponseModel;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

import static edu.uci.ics.gmehta1.service.movies.core.MovieRecords.addMovie;

@Path("add")
public class AddPage {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMovieRecord(@Context HttpHeaders headers, String jsonText){
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to insert a movie to the database. The genres the movie belongs to must exist before the movie is allowed to be added. The movie must have corresponding entries in the 'genres_in_movies' table.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        AddRequestModel arm;
        AddResponseModel responseModel;
        //CHECK PRIV LVL HERE!!!!!

        try{
            arm = mapper.readValue(jsonText, AddRequestModel.class);
            ServiceLogger.LOGGER.info("Title: " + arm.getTitle());
            ServiceLogger.LOGGER.info("Director: " + arm.getDirector());
            ServiceLogger.LOGGER.info("Year: " + arm.getYear());
            ServiceLogger.LOGGER.info("BackdropPath: " + arm.getBackdrop_path());
            ServiceLogger.LOGGER.info("Budget: " + arm.getBudget());
            ServiceLogger.LOGGER.info("Overview: " + arm.getOverview());
            ServiceLogger.LOGGER.info("PosterPath: " + arm.getPoster_path());
            ServiceLogger.LOGGER.info("Revenue: " + arm.getRevenue());
//            ServiceLogger.LOGGER.info("Genres: " + arm.getGenres());
            responseModel = addMovie(arm);
            return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
        }catch(IOException e)
        {
            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new AddResponseModel(-3,"JSON Parse Exception.",null,null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new AddResponseModel(-2,"JSON Mapping Exception.",null,null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            } else {
                ServiceLogger.LOGGER.warning("IOException .");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(null).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
        }
        return null;
    }
}
