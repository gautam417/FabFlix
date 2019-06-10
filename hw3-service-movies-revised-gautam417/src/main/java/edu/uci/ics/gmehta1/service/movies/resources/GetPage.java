package edu.uci.ics.gmehta1.service.movies.resources;

import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.GetResponseModel;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static edu.uci.ics.gmehta1.service.movies.core.MovieRecords.getMoviesFromDB;

@Path("get")
public class GetPage {
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
        ServiceLogger.LOGGER.info("Received request to return the full details of a movie, including all fields from movies, ratings, all genres the movie belongs to, and all actors in the movie.");
        ServiceLogger.LOGGER.info("MovieId: " +  movieId);

        GetResponseModel responseModel ;
        responseModel = getMoviesFromDB(movieId);
        return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID",transactionID).build();
    }

}
