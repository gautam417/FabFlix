package edu.uci.ics.gmehta1.service.movies.resources;


import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.DeleteResponseModel;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static edu.uci.ics.gmehta1.service.movies.core.MovieRecords.deleteMovie;

@Path("delete")
public class DeletePage {
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{movieId}")
    public Response deleteMovieRecord(@Context HttpHeaders headers, @PathParam("movieId") String movieId) {
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);

        ServiceLogger.LOGGER.info("Received request to removes a movie from the database by setting the “hidden” field to true.");
        ServiceLogger.LOGGER.info("MovieId: " +movieId);
        DeleteResponseModel responseModel;
        //NEED TO CHECK PRIV LVL HERES
        responseModel = deleteMovie(movieId);
        return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID",transactionID).build();
    }
}