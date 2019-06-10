package edu.uci.ics.gmehta1.service.movies.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.uci.ics.gmehta1.service.movies.core.StarRecords;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static edu.uci.ics.gmehta1.service.movies.core.StarRecords.addStar1;
import static edu.uci.ics.gmehta1.service.movies.core.StarRecords.addStarsIn;

@Path("star")
public class StarPage {
    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchStar(@Context HttpHeaders headers,
                               @QueryParam("name") String name, @QueryParam("birthYear") int birthYear, //STRING OR INT???
                               @QueryParam("movieTitle") String movieTitle,
                               @QueryParam("offset") int offset, @QueryParam("limit") int limit,
                               @QueryParam("direction") String direction, @QueryParam("orderby") String orderby)
    {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);

        ServiceLogger.LOGGER.info("Received request for searching movie records that a Star is in.");
        ServiceLogger.LOGGER.info("name: " + name);
        ServiceLogger.LOGGER.info("birthYear: " + birthYear);
        ServiceLogger.LOGGER.info("movieTitle: " + movieTitle);
        ServiceLogger.LOGGER.info("offset: " + offset);
        ServiceLogger.LOGGER.info("limit: " + limit);
        ServiceLogger.LOGGER.info("direction: " + direction);
        ServiceLogger.LOGGER.info("orderby:" + orderby);

        // DO THE OFFSET AND LIMIT DEFAULT VALUE CHECKING HERE FROM SEARCH
        if (limit != 10 || limit != 25 || limit != 50 || limit != 100){
            limit = 10;
        }
        //f the limit for a search is 25, then offset must be 0
        // or some positive integer multiple of 25
        // (i.e. 25, 50, 75, etc.) If some invalid input is provided,
        // revert to using the default value.
        List<Integer> myList = new ArrayList<>(); // CREATING MULTIPLES OF LIMIT
        if (offset > 0)
        {
            for (int i = 0; i < 1000; i+=limit)
            {
                myList.add(i);
            } // Populate the list with multiples of the limit
            if (!myList.contains(offset)){
                offset = 0;
            }
        }
        if (offset <= 0){
            offset = 0;
        }
        if (direction != "asc" || direction != "desc"){
            direction = "asc";
        }
        if (orderby != "rating" || orderby != "title"){
            orderby = "name";
        }

        if (name == null)
        {
            ServiceLogger.LOGGER.info("Name is null.");
            name = "";
        }
        if (birthYear == 0)
        {
            ServiceLogger.LOGGER.info("BirthYear is 0.");
        }
        if (movieTitle == null)
        {
            ServiceLogger.LOGGER.info("MovieTitle is null.");
            movieTitle = "";
        }
        //  Build a JSON node from the query parameters. You'll have to figure this out.
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.createObjectNode();
        ((ObjectNode) node).put("name",name);
        ((ObjectNode) node).put("birthYear",birthYear);
        ((ObjectNode) node).put("movieTitle",movieTitle);
        ((ObjectNode) node).put("offset",offset);
        ((ObjectNode) node).put("limit",limit);
        ((ObjectNode) node).put("direction",direction);
        ((ObjectNode) node).put("orderby",orderby);
        ServiceLogger.LOGGER.info("Check which values are null: " +  node.toString());
        // Map the new JSON node into a RequestModel. You already know how to do this.
        StarRequestModel srm;
        StarResponseModel responseModel;
        try {
            srm = mapper.readValue(node.toString(), StarRequestModel.class);
            responseModel = StarRecords.retrieveStarsFromDB(srm);
            return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID",transactionID).build();

        }catch(IOException e)
        {
            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new StarResponseModel(-3,"JSON Parse Exception.",null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).build();
            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new StarResponseModel(-2,"JSON Mapping Exception.",null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).build();
            } else {
                ServiceLogger.LOGGER.warning("IOException.");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).header("email", email).header("sessionID", sessionID).build();
        }
        return null;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStarId(@Context HttpHeaders headers, @PathParam("id") String id)
    {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request for searching movie records. Retrieves the full details of a particular star identified by their ID, including the ID’s and titles of all movies the actor is in.");
        ServiceLogger.LOGGER.info("id: " + id);

        StarResponseModel responseModel;
        responseModel = StarRecords.retrieveMoviesById(id);
        return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID",transactionID).build();
    }
    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addStar(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " + email);
        ServiceLogger.LOGGER.info("SESSIONID: " + sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " + transactionID);
        ServiceLogger.LOGGER.info("Received request to add a Star to the database.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");

        ObjectMapper mapper = new ObjectMapper();
        StarAddRequestModel sarm ;
        StarAddResponseModel responseModel;
        //CHECK PRIVILEGE LVL HERE

        try {
            sarm = mapper.readValue(jsonText, StarAddRequestModel.class);
            // responseModel = StarRecords.insertStarIntoDB(sarm);
            ServiceLogger.LOGGER.info("Name: " + sarm.getName());
            ServiceLogger.LOGGER.info("BirthYear: " + sarm.getBirthYear());

            responseModel = addStar1(sarm);
            return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID",transactionID).build();

        } catch (IOException e) {
//            e.printStackTrace();
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
    @POST
    @Path("starsin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response starsIn(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " + email);
        ServiceLogger.LOGGER.info("SESSIONID: " + sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " + transactionID);
        ServiceLogger.LOGGER.info("Adds a star to the stars_in_movies table. " +
                "Allows insertion of only one movie at a time. " +
                "Request must contain a valid star id and valid movie id.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");

        ObjectMapper mapper = new ObjectMapper();
        StarsInRequestModel sirm; // starid or starId or moveid or movieId
        StarAddResponseModel responseModel;
        //CHECK PRIVILEGE LVL HERE
        try {
            sirm = mapper.readValue(jsonText, StarsInRequestModel.class);
            ServiceLogger.LOGGER.info("Starid: " + sirm.getStarid());
            ServiceLogger.LOGGER.info("MovieId: " + sirm.getMovieId());

            responseModel = addStarsIn(sirm);
            return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID",transactionID).build();

        }catch(IOException e)
        {
            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new StarAddResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new StarAddResponseModel(-2,"JSON Mapping Exception.");
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
}


