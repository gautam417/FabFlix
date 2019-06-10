package edu.uci.ics.gmehta1.service.movies.resources;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.uci.ics.gmehta1.service.movies.core.MovieRecords;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.SearchRequestModel;
import edu.uci.ics.gmehta1.service.movies.models.SearchResponseModel;

import java.util.ArrayList;
import java.util.List;


import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;


@Path("search")
public class SearchPage {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSearchRequest(@Context HttpHeaders headers,
                                     @QueryParam("title") String title, @QueryParam("genre") String genre,
                                     @QueryParam("year") int year, @QueryParam("director") String director,
                                     @QueryParam("hidden") boolean hidden,
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

        ServiceLogger.LOGGER.info("Received request to returns only basic info about movie from the movies & ratings tables.");
        ServiceLogger.LOGGER.info("title: " + title);
        ServiceLogger.LOGGER.info("genre: " + genre);
        ServiceLogger.LOGGER.info("year: " + year);
        ServiceLogger.LOGGER.info("director: " + director);
        ServiceLogger.LOGGER.info("hidden: " + hidden);
        ServiceLogger.LOGGER.info("offset: " + offset);
        ServiceLogger.LOGGER.info("limit: " + limit);
        ServiceLogger.LOGGER.info("direction: " + direction);
        ServiceLogger.LOGGER.info("orderby: " + orderby);

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
        if (!(direction == "asc" || direction == "desc")){
            direction = "desc"; //primary sort
        }
        if (!(orderby == "rating" || orderby == "title")){
            orderby = "rating"; //primary sort
        }

        if (director == null)
        {
            ServiceLogger.LOGGER.info("Director is null.");
            director = "";
        }
        if (genre == null)
        {
            genre = "";
            ServiceLogger.LOGGER.info("Genre is null.");
        }
        if (title == null)
        {
            ServiceLogger.LOGGER.info("Title is null.");
            title = "";
        }
        // Build a JSON node from the query parameters. You'll have to figure this out.
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.createObjectNode();
        if (year == 0)
        {
            ServiceLogger.LOGGER.info("Year is 0.");
        }
        ((ObjectNode) node).put("title",title);
        ((ObjectNode) node).put("genre",genre);
        ((ObjectNode) node).put("director",director);
        ((ObjectNode) node).put("year", year);
        ((ObjectNode) node).put("hidden",hidden);
        ((ObjectNode) node).put("limit",limit);
        ((ObjectNode) node).put("offset",offset);
        ((ObjectNode) node).put("orderby",orderby);
        ((ObjectNode) node).put("direction",direction);

        ServiceLogger.LOGGER.info("Check which values are null: " +  node.toString());

        // Map the new JSON node into a RequestModel. You already know how to do this.
        SearchRequestModel srm;
        SearchResponseModel responseModel; // MAPS TO ITS OWN MOVIE MODEL
        try {
            srm = mapper.readValue(node.toString(), SearchRequestModel.class);
            ServiceLogger.LOGGER.info("Title: " + srm.getTitle());
            ServiceLogger.LOGGER.info("Genre: " + srm.getGenre());
            ServiceLogger.LOGGER.info("Year: " + srm.getYear());
            ServiceLogger.LOGGER.info("Director: " + srm.getDirector());
            ServiceLogger.LOGGER.info("hidden: " + srm.isHidden());
            ServiceLogger.LOGGER.info("Offset: " + srm.getOffset());
            ServiceLogger.LOGGER.info("Limit: " + srm.getLimit());
            ServiceLogger.LOGGER.info("Sortby: " + srm.getDirection());
            ServiceLogger.LOGGER.info("Orderby: " + srm.getOrderby());

            responseModel = MovieRecords.retrieveMoviesFromDB(srm);
            return Response.status(Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID",transactionID).build();

        }catch(IOException e)
        {
            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new SearchResponseModel(-3,"JSON Parse Exception.",null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new SearchResponseModel(-2,"JSON Mapping Exception.",null);
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
