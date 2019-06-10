package edu.uci.ics.gmehta1.service.api_gateway.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.uci.ics.gmehta1.service.api_gateway.GatewayService;
import edu.uci.ics.gmehta1.service.api_gateway.exceptions.ModelValidationException;
import edu.uci.ics.gmehta1.service.api_gateway.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.api_gateway.models.VerifySessionResponseModel;
import edu.uci.ics.gmehta1.service.api_gateway.models.idm.EndpointResponseModel;
import edu.uci.ics.gmehta1.service.api_gateway.models.idm.PrivilegeRequestModel;
import edu.uci.ics.gmehta1.service.api_gateway.models.idm.RegisterUserRequestModel;
import edu.uci.ics.gmehta1.service.api_gateway.models.idm.SessionRequestModel;
import edu.uci.ics.gmehta1.service.api_gateway.threadpool.ClientRequest;
import edu.uci.ics.gmehta1.service.api_gateway.threadpool.Constants;
import edu.uci.ics.gmehta1.service.api_gateway.utilities.ModelValidator;
import edu.uci.ics.gmehta1.service.api_gateway.utilities.TransactionIDGenerator;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("idm")
public class IDMEndpoints {
    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUserRequest(String jsonText) {
        ServiceLogger.LOGGER.info("Received request to register user.");
        String transactionID = TransactionIDGenerator.generateTransactionID();
        RegisterUserRequestModel requestModel;
        try
        {
            requestModel = (RegisterUserRequestModel) ModelValidator.verifyModel(jsonText, RegisterUserRequestModel.class);
            ClientRequest cr = new ClientRequest();
            // get the IDM URI from IDM configs
            cr.setURI(GatewayService.getIdmConfigs().getIdmUri());
            // get the register endpoint path from IDM configs
            cr.setEndpoint(GatewayService.getIdmConfigs().getEPUserRegister());
            // set the request model
            cr.setRequest(requestModel); // puts the mapped requestModel into the client request
            // set the transactionID
            cr.setTransactionID(transactionID); // as the random generated
            // set the HttpMethodType (GET/POST)
            cr.setHttpMethodType(Constants.postRequest);
            // Now that the ClientRequest has been built, we can add it to our queue of requests.
            GatewayService.getThreadPool().getQueue().enqueue(cr);
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
        // Generate a NoContentResponseModel to send to the client containing the time to wait before asking for the
        // requested information, and the transactionID.
        return Response.status(Response.Status.NO_CONTENT).entity(EndpointResponseModel.class).header("Access-Control-Expose-Headers", "*") .header("Access-Control-Allow-Headers", "*").header("transactionID" ,transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
    }

    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUserRequest(String jsonText) {
        ServiceLogger.LOGGER.info("Received request to login a user.");
        String transactionID = TransactionIDGenerator.generateTransactionID();
        RegisterUserRequestModel requestModel;
        try
        {
            requestModel = (RegisterUserRequestModel) ModelValidator.verifyModel(jsonText, RegisterUserRequestModel.class);
            ClientRequest cr = new ClientRequest();
            // get the IDM URI from IDM configs
            cr.setURI(GatewayService.getIdmConfigs().getIdmUri());
            // get the register endpoint path from IDM configs
            cr.setEndpoint(GatewayService.getIdmConfigs().getEPUserLogin());
            // set the request model
            cr.setRequest(requestModel); // puts the mapped requestModel into the client request
            // set the transactionID
            cr.setTransactionID(transactionID); // as the random generated
            // set the HttpMethodType (GET/POST)
            cr.setHttpMethodType(Constants.postRequest);
            // Now that the ClientRequest has been built, we can add it to our queue of requests.
            GatewayService.getThreadPool().getQueue().enqueue(cr);
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
        // Generate a NoContentResponseModel to send to the client containing the time to wait before asking for the
        // requested information, and the transactionID.
        return Response.status(Response.Status.NO_CONTENT).entity(EndpointResponseModel.class).header("Access-Control-Expose-Headers", "*") .header("Access-Control-Allow-Headers", "*").header("transactionID" ,transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
    }

    @Path("session")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifySessionRequest(String jsonText) {
        ServiceLogger.LOGGER.info("Received request to verify a session.");
        String transactionID = TransactionIDGenerator.generateTransactionID();
        SessionRequestModel requestModel;
        try
        {
            requestModel = (SessionRequestModel) ModelValidator.verifyModel(jsonText, SessionRequestModel.class);
            ClientRequest cr = new ClientRequest();
            // get the IDM URI from IDM configs
            cr.setURI(GatewayService.getIdmConfigs().getIdmUri());
            // get the register endpoint path from IDM configs
            cr.setEndpoint(GatewayService.getIdmConfigs().getEPSessionVerify());
            // set the request model
            cr.setRequest(requestModel); // puts the mapped requestModel into the client request
            // set the transactionID
            cr.setTransactionID(transactionID); // as the random generated
            // set the HttpMethodType (GET/POST)
            cr.setHttpMethodType(Constants.postRequest);
            // Now that the ClientRequest has been built, we can add it to our queue of requests.
            GatewayService.getThreadPool().getQueue().enqueue(cr);
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
        // Generate a NoContentResponseModel to send to the client containing the time to wait before asking for the
        // requested information, and the transactionID.
        return Response.status(Response.Status.NO_CONTENT).entity(EndpointResponseModel.class).header("transactionID" ,transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
    }

    @Path("privilege")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyUserPrivilegeRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to verify privilege of a user.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);
        PrivilegeRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;
        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }

        try
        {
            ServiceLogger.LOGGER.info("Verifying session with IDM...");

            // Create a new Client
            ServiceLogger.LOGGER.info("Building client...");
            Client client = ClientBuilder.newClient();
            client.register(JacksonFeature.class);

            // Get the URI for the IDM
            ServiceLogger.LOGGER.info("Building URI...");
            String IDM_URI = GatewayService.getIdmConfigs().getIdmUri();
            ServiceLogger.LOGGER.info("Setting path to endpoint...");
            String IDM_ENDPOINT_PATH = GatewayService.getIdmConfigs().getEPSessionVerify();

            // Create a WebTarget to send a request at
            ServiceLogger.LOGGER.info("Building WebTarget...");
            WebTarget webTarget = client.target(IDM_URI).path(IDM_ENDPOINT_PATH);

            // Create an InvocationBuilder to create the HTTP request
            ServiceLogger.LOGGER.info("Starting invocation builder...");
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

            // Set the payload
            ServiceLogger.LOGGER.info("Setting payload of the request");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.createObjectNode();
            ((ObjectNode) node).put("email", email);
            ((ObjectNode) node).put("sessionID", sessionID);
            ServiceLogger.LOGGER.info("Check which values are null: " +  node.toString());

            srm = (SessionRequestModel) ModelValidator.verifyModel(node.toString(), SessionRequestModel.class);

            // Send the request and save it to a Response
            ServiceLogger.LOGGER.info("Sending request...");
            // Check that status code of the request's response
            Response response = invocationBuilder.post(Entity.entity(srm,MediaType.APPLICATION_JSON));
            ServiceLogger.LOGGER.info("Sent!");

            ServiceLogger.LOGGER.info("Reading response into model...");
            VerifySessionResponseModel vsRModel = response.readEntity(VerifySessionResponseModel.class);
            ServiceLogger.LOGGER.info("Successfully read into model!");
            ServiceLogger.LOGGER.info("The result code is: " + vsRModel.getResultCode());

            if (vsRModel.getResultCode() == 130)
            { //Update the session ID (hint pass in a srm object)
                try
                {
                    requestModel = (PrivilegeRequestModel) ModelValidator.verifyModel(jsonText, PrivilegeRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getIdmConfigs().getIdmUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getIdmConfigs().getEPUserPrivilegeVerify());
                    // set the request model
                    cr.setRequest(requestModel); // puts the mapped requestModel into the client request
                    // set the transactionID
                    cr.setTransactionID(transactionID); // as the random generated
                    // set the HttpMethodType (GET/POST)
                    cr.setHttpMethodType(Constants.postRequest);
                    cr.setEmail(email);
                    cr.setSessionID(vsRModel.getSessionID());//NOT SURE IF THATS RIGHT
                    // Now that the ClientRequest has been built, we can add it to our queue of requests.
                    GatewayService.getThreadPool().getQueue().enqueue(cr);
                    return Response.status(Response.Status.NO_CONTENT).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
                }
                catch (ModelValidationException e)
                {
                    ServiceLogger.LOGGER.info("Got some error....");
                    return ModelValidator.returnInvalidRequest(e);
                }
            }
            else {
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", vsRModel.getSessionID()).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }


}