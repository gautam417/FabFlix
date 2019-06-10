package edu.uci.ics.gmehta1.service.idm.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.gmehta1.service.idm.core.SessionRecords;
import edu.uci.ics.gmehta1.service.idm.core.UserRecords;
import edu.uci.ics.gmehta1.service.idm.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.idm.models.LoginResponseModel;
import edu.uci.ics.gmehta1.service.idm.models.SessionRequestModel;
//import edu.uci.ics.gmehta1.service.idm.models.SessionResponseModel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.regex.Pattern;

@Path("session")
public class SessionPage {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSesh(String jsonText){

        ServiceLogger.LOGGER.info("Received request for verifying a session.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        SessionRequestModel srm = null;
        LoginResponseModel responseModel = null;
        try{
            srm = mapper.readValue(jsonText, SessionRequestModel.class);
            ServiceLogger.LOGGER.info("Email: " + srm.getEmail());
            ServiceLogger.LOGGER.info("SessionID: " + srm.getSessionID());
            String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; //email regex
            Pattern pt = Pattern.compile(regex);
            if ((String.valueOf(srm.getSessionID()).equals("") || String.valueOf(srm.getSessionID()).equals(null) || String.valueOf(srm.getSessionID()).length() == 0))
            {
                ServiceLogger.LOGGER.info("SessionID is null");
                responseModel = new LoginResponseModel(-17,"SessionID not provided in request header.",null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            if (String.valueOf(srm.getSessionID()).length() == 0 || String.valueOf(srm.getSessionID()).length() >= 129)
            {
                responseModel = new LoginResponseModel(-13,"Token has invalid length.", null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (!pt.matcher(srm.getEmail()).matches())
            {
                responseModel = new LoginResponseModel(-11,"Email address has invalid format.", null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (srm.getEmail().length() >= 51)
            {
                responseModel = new LoginResponseModel(-10,"Email address has invalid length.", null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }

            if (!SessionRecords.retrieveByEmailFromDB(srm))
            {
                responseModel = new LoginResponseModel(14,"User not found.", null);
                return Response.status(Status.OK).entity(responseModel).build();
            }
            int answer = SessionRecords.VerifySessionFromDB(srm);
            if (answer == 0)
            {
                    responseModel = new LoginResponseModel(130,"Session is active.", srm.getSessionID());
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else if (answer == 1 )
            {
                responseModel = new LoginResponseModel(131,"Session is expired.", null);
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else if (answer == 2)
            {
                responseModel = new LoginResponseModel(132,"Session is closed.", null);
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else if (answer == 3)
            {
                responseModel = new LoginResponseModel(133,"Session is revoked.", null);
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else if (answer == 5) // !SessionRecords.retrieveSessionFromDB(srm)
            {
                responseModel = new LoginResponseModel(134,"Session not found.", null);
                return Response.status(Status.OK).entity(responseModel).build();
            }

        }catch(IOException e)
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
                ServiceLogger.LOGGER.warning("IOException .");
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

