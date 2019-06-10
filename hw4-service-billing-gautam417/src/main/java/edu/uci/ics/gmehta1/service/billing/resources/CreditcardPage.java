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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static edu.uci.ics.gmehta1.service.billing.core.CreditcardRecords.*;

@Path("creditcard")
public class CreditcardPage {
    @POST
    @Path("insert")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertCreditcard(@Context HttpHeaders headers, String jsonText) throws ParseException {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to insert a creditcard.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();

        CreditcardRequestModel crm = null;
        CreditcardResponseModel responseModel = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            crm = mapper.setDateFormat(dateFormat).readValue(jsonText,CreditcardRequestModel.class);
            ServiceLogger.LOGGER.info("id: " + crm.getId());
            ServiceLogger.LOGGER.info("FirstName: " + crm.getFirstName());
            ServiceLogger.LOGGER.info("LastName: " + crm.getLastName());
            ServiceLogger.LOGGER.info("Expiration: " + crm.getExpiration());
            String regex = "[0-9]+"; //isDigit regex
            Pattern pt = Pattern.compile(regex);

            long millis= System.currentTimeMillis();
            java.sql.Date date =new java.sql.Date(millis);

            Date date2 = dateFormat.parse(date.toString()); // TODAYS DATE
            ServiceLogger.LOGGER.info("Todays Date: " + date2);


            Date date3 = dateFormat.parse(crm.getExpiration().toString());
            ServiceLogger.LOGGER.info("Expiration Date: " + date3);

            //WHEN USING SQL DATE IT IS VERY SMART AND JUMPS TO JSON MAPPING ISSUE
//            String regex2 = "(\\d\\d\\d\\d)(-)(\\d\\d)(-)(\\d\\d)"; // DOUBLE BACK SLASHES HERE
//            Pattern pt2 = Pattern.compile(regex2);
//            ServiceLogger.LOGGER.info("Expiration after toString: " + crm.getExpiration().toString());

            if (!((crm.getId().length() >= 16 ) && (crm.getId().length() <= 20)))  {
                responseModel = new CreditcardResponseModel(321,"Credit card ID has invalid length.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if (!pt.matcher(crm.getId()).matches()) {
                responseModel = new CreditcardResponseModel(322,"Credit card ID has invalid value.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if (date2.compareTo(date3) > 0 || date2.compareTo(date3)==0) { // !pt2.matcher(crm.getExpiration().toString()).matches()
//                ServiceLogger.LOGGER.info("Expiration after toString: " + crm.getExpiration().toString());
                ServiceLogger.LOGGER.info("Todays Date is after Expiration Date");
                responseModel = new CreditcardResponseModel(323,"expiration has invalid value.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else{
                responseModel = insertCreditcardIntoDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CreditcardResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CreditcardResponseModel(-2,"JSON Mapping Exception.");
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
    public Response updateCreditcard(@Context HttpHeaders headers, String jsonText) throws ParseException {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to update creditcard's info.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        CreditcardRequestModel crm = null;
        CreditcardResponseModel responseModel = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            crm = mapper.setDateFormat(dateFormat).readValue(jsonText,CreditcardRequestModel.class);

            ServiceLogger.LOGGER.info("id: " + crm.getId());
            ServiceLogger.LOGGER.info("FirstName: " + crm.getFirstName());
            ServiceLogger.LOGGER.info("LastName: " + crm.getLastName());
            ServiceLogger.LOGGER.info("Expiration: " + crm.getExpiration());
            String regex = "[0-9]+"; //isDigit regex
            Pattern pt = Pattern.compile(regex);

            long millis= System.currentTimeMillis();
            java.sql.Date date =new java.sql.Date(millis);

            Date date2 = dateFormat.parse(date.toString()); // TODAYS DATE
            ServiceLogger.LOGGER.info("Todays Date: " + date2);


            Date date3 = dateFormat.parse(crm.getExpiration().toString());
            ServiceLogger.LOGGER.info("Expiration Date: " + date3);

            if (!((crm.getId().length() >= 16 ) && (crm.getId().length() <= 20)))  {
                responseModel = new CreditcardResponseModel(321,"Credit card ID has invalid length.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if (!pt.matcher(crm.getId()).matches()) {
                responseModel = new CreditcardResponseModel(322,"Credit card ID has invalid value.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if (date2.compareTo(date3) > 0 || date2.compareTo(date3)==0) { // !pt2.matcher(crm.getExpiration().toString()).matches()
//                ServiceLogger.LOGGER.info("Expiration after toString: " + crm.getExpiration().toString());
                ServiceLogger.LOGGER.info("Todays Date is after Expiration Date");
                responseModel = new CreditcardResponseModel(323,"expiration has invalid value.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else{
                responseModel = updateCreditcardFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CreditcardResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CreditcardResponseModel(-2,"JSON Mapping Exception.");
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
    @Path("delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCreditcard(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to delete creditcard info.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        IdRequestModel crm = null;
        CreditcardResponseModel responseModel = null;
        try {
            crm = mapper.readValue(jsonText,IdRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("id: " + crm.getId());
            String regex = "[0-9]+"; //isDigit regex
            Pattern pt = Pattern.compile(regex);

            if (!((crm.getId().length() >= 16 ) && (crm.getId().length() <= 20)))  {
                responseModel = new CreditcardResponseModel(321,"Credit card ID has invalid length.");
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else if (!pt.matcher(crm.getId()).matches()) {
                responseModel = new CreditcardResponseModel(322,"Credit card ID has invalid value.");
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else{
                responseModel = deleteCreditCardFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CreditcardResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CreditcardResponseModel(-2,"JSON Mapping Exception.");
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
    public Response getCreditcardInfo(@Context HttpHeaders headers, String jsonText) {
        // Get the email and sessionID from the HTTP header
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = headers.getHeaderString("transactionID");

        ServiceLogger.LOGGER.info("EMAIL: " +  email);
        ServiceLogger.LOGGER.info("SESSIONID: " +  sessionID);
        ServiceLogger.LOGGER.info("TRANSACTIONID: " +  transactionID);
        ServiceLogger.LOGGER.info("Received request to retrieve creditcard info.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        IdRequestModel crm = null;
        CreditcardResponseModel responseModel = null;
        CreditResponseModel responseModel1 = null;
        try {
            crm = mapper.readValue(jsonText,IdRequestModel.class); // Unsure if this will map to the correct constructor
            ServiceLogger.LOGGER.info("id: " + crm.getId());
            String regex = "[0-9]+"; //isDigit regex
            Pattern pt = Pattern.compile(regex);

            if (!((crm.getId().length() >= 16 ) && (crm.getId().length() <= 20)))  {
                responseModel = new CreditcardResponseModel(321,"Credit card ID has invalid length.");
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else if (!pt.matcher(crm.getId()).matches()) {
                responseModel = new CreditcardResponseModel(322,"Credit card ID has invalid value.");
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
            else{
                responseModel1 = retrieveCreditcardFromDB(crm);
                return Response.status(Response.Status.OK).entity(responseModel1).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();
            }
        }catch(IOException e)
        {
//            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new CreditcardResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).header("email", email).header("sessionID", sessionID).header("transactionID", transactionID).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new CreditcardResponseModel(-2,"JSON Mapping Exception.");
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
