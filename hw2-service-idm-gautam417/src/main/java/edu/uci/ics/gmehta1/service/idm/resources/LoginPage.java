package edu.uci.ics.gmehta1.service.idm.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.gmehta1.service.idm.core.LoginRecords;
import edu.uci.ics.gmehta1.service.idm.core.UserRecords;
import edu.uci.ics.gmehta1.service.idm.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.idm.models.LoginResponseModel;
import edu.uci.ics.gmehta1.service.idm.models.RegisterRequestModel;
import edu.uci.ics.gmehta1.service.idm.security.Crypto;
import org.apache.commons.codec.binary.Hex;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.regex.Pattern;

@Path("login")
public class LoginPage {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLog(String jsonText)
    {
        ServiceLogger.LOGGER.info("Received request for logging in a user.");
//        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        RegisterRequestModel lrm;
        LoginResponseModel responseModel;
        if (jsonText.contains("null"))
        {
            responseModel = new LoginResponseModel(-12,"Password has invalid length.",null);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        try {
            lrm = mapper.readValue(jsonText, RegisterRequestModel.class);
            ServiceLogger.LOGGER.info("Email: " + lrm.getEmail());
            ServiceLogger.LOGGER.info("Password: " + String.valueOf(lrm.getPassword()));
            String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; //email regex
            Pattern pt = Pattern.compile(regex);
            if (lrm.getPassword().length==0)
            {
                responseModel = new LoginResponseModel(-12,"Password has invalid length.",null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (!pt.matcher(lrm.getEmail()).matches())
            {
                responseModel = new LoginResponseModel(-11,"Email address has invalid format.",null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (lrm.getEmail().length() >= 51)
            {
                responseModel = new LoginResponseModel(-10,"Email address has invalid length.",null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
//            GENERATE SESSION ID HERE
        //- Check database, table "sessions" to see if any session attached to the email currently trying to log in has a status labelled "Active".
        //- If there is, use sql commands to mark the session in the table as "Revoked" and create a new one with Session.createSession(<email logging in>).
        //  Add to the table as "Active".
        //- If there isn't, create a new session with Session.createSession(<email logging in>).
        //  Add to the "sessions" table with the status "Active".
            if (!UserRecords.retrieveByEmailFromDB(lrm))
            {
                responseModel = new LoginResponseModel(14,"User not found.", null);
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else if (!UserRecords.retrievePassFromDB(lrm))
            {
                responseModel = new LoginResponseModel(11,"Passwords do not match.", null );
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else
            {
                String sess = LoginRecords.retrieveLoginFromDB(lrm); //make sure its not null or else the method is wrong
                responseModel = new LoginResponseModel(120,"User logged in successfully.", sess);
                return Response.status(Status.OK).entity(responseModel).build();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("Unable to parse JSON.");
                responseModel = new LoginResponseModel(-3,"JSON Parse Exception.",null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();

            } else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
                responseModel = new LoginResponseModel(-2,"JSON Mapping Exception.",null);
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
