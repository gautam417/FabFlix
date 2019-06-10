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

import static edu.uci.ics.gmehta1.service.billing.core.CustomerRecords.*;

@Path("customer")
public class CustomerPage {
    @POST
    @Path("insert")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertCustomer(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to insert a Customer.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        CustomerRequestModel crm = null;
        CustomerResponseModel responseModel = null;
        try {
            crm = mapper.readValue(jsonText,CustomerRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Email: " + crm.getEmail());
            ServiceLogger.LOGGER.info("FirstName: " + crm.getFirstName());
            ServiceLogger.LOGGER.info("LastName: " + crm.getLastName());
            ServiceLogger.LOGGER.info("CCid: " + crm.getCcId());
            ServiceLogger.LOGGER.info("Address: " + crm.getAddress());

            String regex = "[0-9]+"; //isDigit regex
            Pattern pt = Pattern.compile(regex);

            if (!((crm.getCcId().length() >= 16 ) && (crm.getCcId().length() <= 20)))  {
                responseModel = new CustomerResponseModel(321,"Credit card ID has invalid length.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if (!pt.matcher(crm.getCcId()).matches()) {
                responseModel = new CustomerResponseModel(322,"Credit card ID has invalid value.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else{
                responseModel = insertCustomerIntoDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CustomerResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CustomerResponseModel(-2,"JSON Mapping Exception.");
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
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCustomer(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to update a Customer's info.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        CustomerRequestModel crm = null;
        CustomerResponseModel responseModel = null;
        try {
            crm = mapper.readValue(jsonText,CustomerRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Email: " + crm.getEmail());
            ServiceLogger.LOGGER.info("FirstName: " + crm.getFirstName());
            ServiceLogger.LOGGER.info("LastName: " + crm.getLastName());
            ServiceLogger.LOGGER.info("CCid: " + crm.getCcId());
            ServiceLogger.LOGGER.info("Address: " + crm.getAddress());

            String regex = "[0-9]+"; //isDigit regex
            Pattern pt = Pattern.compile(regex);

            if (!((crm.getCcId().length() >= 16 ) && (crm.getCcId().length() <= 20)))  {
                responseModel = new CustomerResponseModel(321,"Credit card ID has invalid length.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if (!pt.matcher(crm.getCcId()).matches()) {
                responseModel = new CustomerResponseModel(322,"Credit card ID has invalid value.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else{
                responseModel = updateCustomerFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CustomerResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CustomerResponseModel(-2,"JSON Mapping Exception.");
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
    public Response getCustomer(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to retrieve a Customer's info.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        MixRequestModel crm = null;
        CustomerResponseModel responseModel = null;
        CustomResponseModel responseModel1 = null;
        try {
            crm = mapper.readValue(jsonText,MixRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("Email: " + crm.getEmail());
            responseModel1 = retrieveCustomerFromDB(crm);
            return Response.status(Response.Status.OK).entity(responseModel1).build();

        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CustomerResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CustomerResponseModel(-2,"JSON Mapping Exception.");
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
