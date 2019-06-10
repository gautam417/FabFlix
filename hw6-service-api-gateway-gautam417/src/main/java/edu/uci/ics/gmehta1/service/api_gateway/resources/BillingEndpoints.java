package edu.uci.ics.gmehta1.service.api_gateway.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.uci.ics.gmehta1.service.api_gateway.GatewayService;
import edu.uci.ics.gmehta1.service.api_gateway.exceptions.ModelValidationException;
import edu.uci.ics.gmehta1.service.api_gateway.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.api_gateway.models.VerifySessionResponseModel;
import edu.uci.ics.gmehta1.service.api_gateway.models.billing.*;
import edu.uci.ics.gmehta1.service.api_gateway.models.idm.EndpointResponseModel;
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
import javax.ws.rs.core.Response.Status;

@Path("billing")
public class BillingEndpoints {
    @Path("cart/insert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertToCartRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to insert a item into a cart.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        CartRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;
        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (CartRequestModel) ModelValidator.verifyModel(jsonText, CartRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCartInsert());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }
    @Path("cart/update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCartRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to update a cart.");

        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        CartRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;
        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (CartRequestModel) ModelValidator.verifyModel(jsonText, CartRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCartUpdate());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }

    @Path("cart/delete")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCartRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to delete an item.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        DeleteRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;
        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (DeleteRequestModel) ModelValidator.verifyModel(jsonText, DeleteRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCartDelete());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }

    @Path("cart/retrieve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveCartRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to retrieve a cart.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);


        MixRequestModel requestModel;
        SessionRequestModel srm;
        EndpointResponseModel responseModel;
        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (MixRequestModel) ModelValidator.verifyModel(jsonText, MixRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCartRetrieve());
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

    @Path("cart/clear")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearCartRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to clear a cart.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        MixRequestModel requestModel;
        SessionRequestModel srm;
        EndpointResponseModel responseModel;
        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (MixRequestModel) ModelValidator.verifyModel(jsonText, MixRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCartClear());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }
    @Path("creditcard/insert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertCreditCardRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to insert a creditcard.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        CreditcardRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;
        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (CreditcardRequestModel) ModelValidator.verifyModel(jsonText, CreditcardRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCcInsert());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }

    @Path("creditcard/update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCreditCardRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to update creditcard's info.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);
        CreditcardRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;

        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (CreditcardRequestModel) ModelValidator.verifyModel(jsonText, CreditcardRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCcUpdate());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }

    @Path("creditcard/delete")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCreditCardRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to delete creditcard info.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        IdRequestModel requestModel;
        SessionRequestModel srm;
        EndpointResponseModel responseModel;

        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (IdRequestModel) ModelValidator.verifyModel(jsonText, IdRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCcDelete());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }


    @Path("creditcard/retrieve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveCreditCardRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to retrieve creditcard info.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);
        IdRequestModel requestModel;
        SessionRequestModel srm;
        EndpointResponseModel responseModel;

        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (IdRequestModel) ModelValidator.verifyModel(jsonText, IdRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCcRetrieve());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }



    @Path("customer/insert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertCustomerRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to insert a Customer.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        CustomerRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;

        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (CustomerRequestModel) ModelValidator.verifyModel(jsonText, CustomerRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCustomerInsert());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }


    @Path("customer/update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCustomerRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to update a Customer's info.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        CustomerRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;

        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (CustomerRequestModel) ModelValidator.verifyModel(jsonText, CustomerRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCustomerUpdate());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }

    @Path("customer/retrieve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveCustomerRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to retrieve a Customer's info.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        MixRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;

        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (MixRequestModel) ModelValidator.verifyModel(jsonText, MixRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCustomerRetrieve());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }


    @Path("order/place")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response placeOrderRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to place an Order.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        MixRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;

        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (MixRequestModel) ModelValidator.verifyModel(jsonText, MixRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPOrderPlace());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }

    @Path("order/retrieve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveOrderRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to retrieve Orders.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        MixRequestModel requestModel;

        SessionRequestModel srm;
        EndpointResponseModel responseModel;

        if (sessionID == null)
        {
            ServiceLogger.LOGGER.info("SessionID is null");
            responseModel = new EndpointResponseModel(-17);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
        if (email == null)
        {
            ServiceLogger.LOGGER.info("Email is null");
            responseModel = new EndpointResponseModel(-16);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
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
                    requestModel = (MixRequestModel) ModelValidator.verifyModel(jsonText, MixRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPOrderRetrieve());
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
                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
            }
        }
        catch (ModelValidationException e)
        {
            return ModelValidator.returnInvalidRequest(e);
        }
    }

}
