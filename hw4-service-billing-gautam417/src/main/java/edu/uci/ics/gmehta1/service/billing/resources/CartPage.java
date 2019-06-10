package edu.uci.ics.gmehta1.service.billing.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.billing.models.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.regex.Pattern;

import static edu.uci.ics.gmehta1.service.billing.core.CartRecords.*;

@Path("cart")
public class CartPage {
    @POST
    @Path("insert")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertIntoCart(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to insert a item into a cart.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        CartRequestModel crm = null;
        CartResponseModel responseModel = null;
        try {
            crm = mapper.readValue(jsonText,CartRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Email: " + crm.getEmail());
            ServiceLogger.LOGGER.info("MovieId: " + crm.getMovieId());
            ServiceLogger.LOGGER.info("Quantity: " + crm.getQuantity());

            String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; //email regex
            Pattern pt = Pattern.compile(regex);
            if (!pt.matcher(crm.getEmail()).matches())
            {
                responseModel = new CartResponseModel(-11,"Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else if (crm.getEmail().length() >= 51)
            {
                responseModel = new CartResponseModel(-10,"Email address has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            if (crm.getQuantity() <= 0) {
                responseModel = new CartResponseModel(33,"Quantity has invalid value.");
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else  {
                responseModel = insertItemIntoDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CartResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CartResponseModel(-2,"JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else {
                ServiceLogger.LOGGER.warning("IOException.");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
        }
        return null;
    }
    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCart(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to update a cart.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        CartRequestModel crm = null;
        CartResponseModel responseModel = null;
        try {
            crm = mapper.readValue(jsonText,CartRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Email: " + crm.getEmail());
            ServiceLogger.LOGGER.info("MovieId: " + crm.getMovieId());
            ServiceLogger.LOGGER.info("Quantity: " + crm.getQuantity());

            String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; //email regex
            Pattern pt = Pattern.compile(regex);
            if (!pt.matcher(crm.getEmail()).matches())
            {
                responseModel = new CartResponseModel(-11,"Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else if (crm.getEmail().length() >= 51)
            {
                responseModel = new CartResponseModel(-10,"Email address has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            if (crm.getQuantity() <= 0) {
                responseModel = new CartResponseModel(33,"Quantity has invalid value.");
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else{
                responseModel = updateItemFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CartResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CartResponseModel(-2,"JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else {
                ServiceLogger.LOGGER.warning("IOException.");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
        }
        return null;
    }
    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCart(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to delete an item.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        DeleteRequestModel crm = null;
        CartResponseModel responseModel = null;
        try {
            crm = mapper.readValue(jsonText, DeleteRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Email: " + crm.getEmail());
            ServiceLogger.LOGGER.info("MovieId: " + crm.getMovieId());
//            ServiceLogger.LOGGER.info("Quantity: " + crm.getQuantity());

            String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; //email regex
            Pattern pt = Pattern.compile(regex);
            if (!pt.matcher(crm.getEmail()).matches())
            {
                responseModel = new CartResponseModel(-11,"Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else if (crm.getEmail().length() >= 51)
            {
                responseModel = new CartResponseModel(-10,"Email address has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else{
                responseModel = deleteItemFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CartResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CartResponseModel(-2,"JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else {
                ServiceLogger.LOGGER.warning("IOException.");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
        }
        return null;
    }
    @POST
    @Path("retrieve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCart(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to retrieve a cart.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        MixRequestModel crm = null;
        CartResponseModel responseModel = null;
        ItemsResponseModel responseModel1 = null;
        try {
            crm = mapper.readValue(jsonText,MixRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Email: " + crm.getEmail());

            String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; //email regex
            Pattern pt = Pattern.compile(regex);
            if (!pt.matcher(crm.getEmail()).matches())
            {
                responseModel = new CartResponseModel(-11,"Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else if (crm.getEmail().length() >= 51)
            {
                responseModel = new CartResponseModel(-10,"Email address has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else {
                responseModel1 = retrieveItemsFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel1).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CartResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CartResponseModel(-2,"JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else {
                ServiceLogger.LOGGER.warning("IOException.");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
        }
        return null;
    }
    @POST
    @Path("clear")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearCart(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to clear a cart.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        MixRequestModel crm = null;
        CartResponseModel responseModel = null;
        try {
            crm = mapper.readValue(jsonText,MixRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Email: " + crm.getEmail());
//            ServiceLogger.LOGGER.info("Quantity: " + crm.getQuantity());

            String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; //email regex
            Pattern pt = Pattern.compile(regex);
            if (!pt.matcher(crm.getEmail()).matches())
            {
                responseModel = new CartResponseModel(-11,"Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else if (crm.getEmail().length() >= 51)
            {
                responseModel = new CartResponseModel(-10,"Email address has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else{
                responseModel = clearItemsFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CartResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CartResponseModel(-2,"JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else {
                ServiceLogger.LOGGER.warning("IOException.");
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
