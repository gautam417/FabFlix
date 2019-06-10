package edu.uci.ics.gmehta1.service.idm.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.gmehta1.service.idm.core.UserRecords;
import edu.uci.ics.gmehta1.service.idm.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.idm.models.RegisterRequestModel;
import edu.uci.ics.gmehta1.service.idm.models.RegisterResponseModel;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.regex.*;

@Path("register")
public class RegisterPage {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReg(@Context HttpHeaders headers, String jsonText)
    {
        ServiceLogger.LOGGER.info("Received request for registering a user.");
//        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        RegisterRequestModel rrm = null;
        RegisterResponseModel responseModel = null;
        Pattern p = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[.@$#!%*?&])[A-Za-z\\d@.$#!%*?&]{7,16}$"); //password regex
        if (jsonText.contains("null"))
        {
            responseModel = new RegisterResponseModel(-12,"Password has invalid length.");
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        try{
            rrm = mapper.readValue(jsonText, RegisterRequestModel.class);
            ServiceLogger.LOGGER.info("Email: " + rrm.getEmail());
//            ServiceLogger.LOGGER.info("Password: " + String.valueOf(rrm.getPassword())); // NEED TO TAKE OUT EVENTUALLY
            Matcher m = p.matcher(String.valueOf(rrm.getPassword()));
            String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; //email regex
            Pattern pt = Pattern.compile(regex);
            if (rrm.getPassword().length == 0)
            {
                responseModel = new RegisterResponseModel(-12,"Password has invalid length.");
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (!pt.matcher(rrm.getEmail()).matches())
            {
                responseModel = new RegisterResponseModel(-11,"Email address has invalid format.");
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (rrm.getEmail().length() >= 51)
            {
                responseModel = new RegisterResponseModel(-10,"Email address has invalid length.");
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            if ((String.valueOf(rrm.getPassword()).length() <= 6 || String.valueOf(rrm.getPassword()).length() >=17) )
            {
                ServiceLogger.LOGGER.info("Length of password: " + String.valueOf(rrm.getPassword()).length());
                responseModel = new RegisterResponseModel(12,"Password does not meet length requirements.");
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else if (!m.matches())
            {
                ServiceLogger.LOGGER.info("Password: " + String.valueOf(rrm.getPassword()).length());
                responseModel = new RegisterResponseModel(13,"Password does not meet character requirements.");
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else if (UserRecords.retrieveByEmailFromDB(rrm))
            {
                responseModel = new RegisterResponseModel(16,"Email already in use.");
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else
            {
                ServiceLogger.LOGGER.info("Credentials are valid.");
                if (UserRecords.insertUserToDb(rrm)){
                    // Insertion was successful
                    ServiceLogger.LOGGER.info("Insertion was successful.");
                    responseModel = new RegisterResponseModel(110,"User registered successfully.");
                    return Response.status(Status.OK).entity(responseModel).build();
                }
                else{
                    // Insertion failed
                    ServiceLogger.LOGGER.info("Insertion failed.");
                    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
                }
            }
//            }

        } catch (IOException e)
        {
            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new RegisterResponseModel(-3,"JSON Parse Exception.");
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new RegisterResponseModel(-2,"JSON Mapping Exception.");
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();

            } else {
                ServiceLogger.LOGGER.warning("IOException.");
            }
        }
        catch (InternalServerErrorException e)
        {
            ServiceLogger.LOGGER.info("Internal Server Error."); //Didnt report as case -1 here
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(null).build();
        }
        return null;
    }
}
