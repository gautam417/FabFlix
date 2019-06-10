package edu.uci.ics.gmehta1.service.idm.resources;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.gmehta1.service.idm.core.Priv;
import edu.uci.ics.gmehta1.service.idm.core.PrivRecords;
import edu.uci.ics.gmehta1.service.idm.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.idm.models.PrivilegeRequestModel;
import edu.uci.ics.gmehta1.service.idm.models.RegisterResponseModel;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.regex.Pattern;

@Path("privilege")
public class PrivPage {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPriv(String jsonText) {
        ServiceLogger.LOGGER.info("Received request for verifying a privilege.");
        ServiceLogger.LOGGER.info("Request:\n" +jsonText +"\n");
        ObjectMapper mapper = new ObjectMapper();
        PrivilegeRequestModel prm = null;
        RegisterResponseModel responseModel = null;
        try {
            prm = mapper.readValue(jsonText, PrivilegeRequestModel.class);
            ServiceLogger.LOGGER.info("Email: " + prm.getEmail());
            ServiceLogger.LOGGER.info("Plevel: " + prm.getPlevel());
            String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$"; //email regex
            Pattern pt = Pattern.compile(regex);
            if (prm.getPlevel() > 5 || prm.getPlevel() <=0)
            {
                responseModel = new RegisterResponseModel(-14,"Privilege level out of valid range.");
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (!pt.matcher(prm.getEmail()).matches())
            {
                responseModel = new RegisterResponseModel(-11,"Email address has invalid format.");
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (prm.getEmail().length() >= 51)
            {
                responseModel = new RegisterResponseModel(-10,"Email address has invalid length.");
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }
            if (!PrivRecords.retrieveByEmailFromDB(prm))
            {
                responseModel = new RegisterResponseModel(14,"User not found.");
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else if (PrivRecords.CheckPrivFromDB(prm))
            {
                responseModel = new RegisterResponseModel(140,"User has sufficient privilege level.");
                return Response.status(Status.OK).entity(responseModel).build();
            }
            else if (!PrivRecords.CheckPrivFromDB(prm))
            {
                responseModel = new RegisterResponseModel(141,"User has insufficient privilege level.");
                return Response.status(Status.OK).entity(responseModel).build();
            }

        }catch(IOException e)
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
