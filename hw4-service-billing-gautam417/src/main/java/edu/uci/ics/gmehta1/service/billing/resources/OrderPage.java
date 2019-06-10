package edu.uci.ics.gmehta1.service.billing.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.billing.models.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static edu.uci.ics.gmehta1.service.billing.core.CartRecords.clearItemsFromDB;
import static edu.uci.ics.gmehta1.service.billing.core.CartRecords.retrieveItemsFromDB;
import static edu.uci.ics.gmehta1.service.billing.core.CustomerRecords.retrieveCustomerFromDB;
import static edu.uci.ics.gmehta1.service.billing.core.OrderRecords.*;

@Path("order")
public class OrderPage {
    @POST
    @Path("place")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response placeOrder(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);

        ServiceLogger.LOGGER.info("Received request to place an Order.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        MixRequestModel crm = null;
        OrderResponseModel responseModel = null;
        OrderPlaceResponseModel responseModel1 = null;
        CustomResponseModel responseModel2 = null;
        ItemsResponseModel responseModel3 = null;
        try {
            crm = mapper.readValue(jsonText, MixRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Email: " + crm.getEmail());
            if (retrieveCustomerFromDB(crm).getResultCode() == 332){
                responseModel2 = retrieveCustomerFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel2).build();
            }
            else if (retrieveItemsFromDB(crm).getResultCode() == 312) {
                responseModel3 = new ItemsResponseModel(341, "Shopping cart for this customer not found.",null);
                return Response.status(Response.Status.OK).entity(responseModel3).build();
            }
            else {
                responseModel1 = insertOrderIntoDB(crm);
                clearItemsFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel1).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new OrderResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new OrderResponseModel(-2,"JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else {
                ServiceLogger.LOGGER.warning("IOException.");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).build();
        }
        return null;
    }
    @POST
    @Path("retrieve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to retrieve Orders.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        MixRequestModel crm = null;
        OrderResponseModel responseModel = null;
        OrdResponseModel responseModel1 = null;
        CustomResponseModel responseModel2 = null;

        try {
            crm = mapper.readValue(jsonText, MixRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Email: " + crm.getEmail());

            if (retrieveCustomerFromDB(crm).getResultCode() == 332){
                responseModel2 = retrieveCustomerFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel2).build();
            }
            else {
                responseModel1 = retrieveOrdersFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel1).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new OrderResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new OrderResponseModel(-2,"JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else {
                ServiceLogger.LOGGER.warning("IOException.");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).build();
        }
        return null;
    }
    @GET
    @Path("complete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeOrder(@Context HttpHeaders headers, @QueryParam("paymentId") String paymentId,
                                  @QueryParam("token") String token,
                                  @QueryParam("PayerID") String PayerID)
    {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);

        ServiceLogger.LOGGER.info("Received request to complete Order.");
        ServiceLogger.LOGGER.info("paymentId: " + paymentId);
        ServiceLogger.LOGGER.info("token: " + token);
        ServiceLogger.LOGGER.info("PayerID: " + PayerID);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.createObjectNode();
        ((ObjectNode) node).put("paymentId", paymentId);
        ((ObjectNode) node).put("token", token);
        ((ObjectNode) node).put("PayerID", PayerID);

        ServiceLogger.LOGGER.info("Check which values are null: " +  node.toString());

        CompleteRequestModel crm = null;
        OrderResponseModel responseModel = null;

        try {
            crm = mapper.readValue(node.toString(), CompleteRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("PaymentId: " + crm.getPaymentId());
            ServiceLogger.LOGGER.info("Token: " + crm.getToken());
            ServiceLogger.LOGGER.info("PayerId: " + crm.getPayerId());
            responseModel = updateTransactionsInDB(crm);
            return Response.status(Response.Status.OK).entity(responseModel).build();

        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new OrderResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new OrderResponseModel(-2,"JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else {
                ServiceLogger.LOGGER.warning("IOException.");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null).build();
        }
        return null;
    }
}