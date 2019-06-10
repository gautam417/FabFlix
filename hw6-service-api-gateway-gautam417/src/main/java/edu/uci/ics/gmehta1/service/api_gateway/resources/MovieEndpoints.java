package edu.uci.ics.gmehta1.service.api_gateway.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.uci.ics.gmehta1.service.api_gateway.GatewayService;
import edu.uci.ics.gmehta1.service.api_gateway.exceptions.ModelValidationException;
import edu.uci.ics.gmehta1.service.api_gateway.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.api_gateway.models.VerifySessionResponseModel;
import edu.uci.ics.gmehta1.service.api_gateway.models.idm.EndpointResponseModel;
import edu.uci.ics.gmehta1.service.api_gateway.models.idm.SessionRequestModel;
import edu.uci.ics.gmehta1.service.api_gateway.models.movie.*;
import edu.uci.ics.gmehta1.service.api_gateway.threadpool.ClientRequest;
import edu.uci.ics.gmehta1.service.api_gateway.threadpool.Constants;
import edu.uci.ics.gmehta1.service.api_gateway.utilities.ModelValidator;
import edu.uci.ics.gmehta1.service.api_gateway.utilities.TransactionIDGenerator;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

@Path("movies")
public class MovieEndpoints {
    @Path("search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchMovieRequest(@Context HttpHeaders headers, @Context UriInfo uriInfo) {
        ServiceLogger.LOGGER.info("Returns only basic information about the movie from the movies and ratings tables.");
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        ServiceLogger.LOGGER.info("The query params are: " + queryParams);

        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

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
                ClientRequest cr = new ClientRequest();
                // get the IDM URI from IDM configs
                cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                // get the register endpoint path from IDM configs
                cr.setEndpoint(GatewayService.getMovieConfigs().getEPMovieSearch());
                // set the request model
                cr.setQueryParams(queryParams); // puts the mapped requestModel into the client request
                // set the transactionID
                cr.setTransactionID(transactionID); // as the random generated
                // set the HttpMethodType (GET/POST)
                cr.setHttpMethodType(Constants.getRequest);
                cr.setEmail(email);
                cr.setSessionID(vsRModel.getSessionID());
//                cr.setSessionID(vsRModel.getSessionID());//NOT SURE IF THATS RIGHT
                // Now that the ClientRequest has been built, we can add it to our queue of requests.
                GatewayService.getThreadPool().getQueue().enqueue(cr);
                return Response.status(Response.Status.NO_CONTENT).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
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

    @Path("get/{movieid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMovieRequest(@Context HttpHeaders headers, @Context UriInfo uriInfo) throws NullPointerException {
        ServiceLogger.LOGGER.info("Returns the full details of a movie, including all fields from movies, ratings, all genres the movie belongs to, and all actors in the movie.");
        MultivaluedMap<String, String> queryParams = uriInfo.getPathParameters();
        ServiceLogger.LOGGER.info("The query params are: " + queryParams);
        ServiceLogger.LOGGER.info("Value: " + queryParams.get("movieid"));


        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

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
                try {
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getMovieConfigs().getMoviesUri()); // needs the get portion
                    ServiceLogger.LOGGER.info("The URI is: " + cr.getURI() +"/get/" +queryParams.get("movieid").toString().replaceAll("[\\[\\],]",""));
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint("/get/"+ queryParams.get("movieid").toString().replaceAll("[\\[\\],]",""));
                    // set the request model
//                cr.setQueryParams(queryParams.get("movieid")); // IDK IF THIS WILL WORK
                    // set the transactionID
                    cr.setTransactionID(transactionID); // as the random generated
                    // set the HttpMethodType (GET/POST)
                    cr.setHttpMethodType(Constants.getRequest);
                    cr.setEmail(email);
                    cr.setSessionID(vsRModel.getSessionID());//NOT SURE IF THATS RIGHT
                    // Now that the ClientRequest has been built, we can add it to our queue of requests.
                    GatewayService.getThreadPool().getQueue().enqueue(cr);
                }
                catch (NullPointerException npe){
                    ServiceLogger.LOGGER.info("Caught NullPointerException");
                }
                return Response.status(Response.Status.NO_CONTENT).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
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

    @Path("add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMovieRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to insert a movie to the database. The genres the movie belongs to must exist before the movie is allowed to be added. The movie must have corresponding entries in the 'genres_in_movies' table.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        AddRequestModel requestModel;

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
//        try
//        {
//            ServiceLogger.LOGGER.info("Verifying Privilege with IDM...");
//
//            // Create a new Client
//            ServiceLogger.LOGGER.info("Building client...");
//            Client client = ClientBuilder.newClient();
//            client.register(JacksonFeature.class);
//
//            // Get the URI for the IDM
//            ServiceLogger.LOGGER.info("Building URI...");
//            String IDM_URI = GatewayService.getIdmConfigs().getIdmUri();
//
//            ServiceLogger.LOGGER.info("Setting path to endpoint...");
//            String IDM_ENDPOINT_PATH = GatewayService.getIdmConfigs().getEPUserPrivilegeVerify();
//
//            // Create a WebTarget to send a request at
//            ServiceLogger.LOGGER.info("Building WebTarget...");
//            WebTarget webTarget = client.target(IDM_URI).path(IDM_ENDPOINT_PATH);
//
//            // Create an InvocationBuilder to create the HTTP request
//            ServiceLogger.LOGGER.info("Starting invocation builder...");
//            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
//
//            // Set the payload
//            ServiceLogger.LOGGER.info("Setting payload of the request");
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode node = mapper.createObjectNode();
//            ((ObjectNode) node).put("email", email);
//            ((ObjectNode) node).put("sessionID", sessionID);
//            ServiceLogger.LOGGER.info("Check which values are null: " +  node.toString());
//
//            srm = (SessionRequestModel) ModelValidator.verifyModel(node.toString(), SessionRequestModel.class);
//
//            // Send the request and save it to a Response
//            ServiceLogger.LOGGER.info("Sending request...");
//            // Check that status code of the request's response
//            Response response = invocationBuilder.post(Entity.entity(srm,MediaType.APPLICATION_JSON));
//            ServiceLogger.LOGGER.info("Sent!");
//
//            ServiceLogger.LOGGER.info("Reading response into model...");
//            VerifySessionResponseModel vsRModel = response.readEntity(VerifySessionResponseModel.class);
//            ServiceLogger.LOGGER.info("Successfully read into model!");
//            ServiceLogger.LOGGER.info("The result code is: " + vsRModel.getResultCode());
//
//            if (vsRModel.getResultCode() == 130)
//            { //Update the session ID (hint pass in a srm object)
//                try
//                {
//                    requestModel = (MixRequestModel) ModelValidator.verifyModel(jsonText, MixRequestModel.class);
//                    ClientRequest cr = new ClientRequest();
//                    // get the IDM URI from IDM configs
//                    cr.setURI(GatewayService.getBillingConfigs().getBillingUri());
//                    // get the register endpoint path from IDM configs
//                    cr.setEndpoint(GatewayService.getBillingConfigs().getEPCustomerRetrieve());
//                    // set the request model
//                    cr.setRequest(requestModel); // puts the mapped requestModel into the client request
//                    // set the transactionID
//                    cr.setTransactionID(transactionID); // as the random generated
//                    // set the HttpMethodType (GET/POST)
//                    cr.setHttpMethodType(Constants.postRequest);
//                    cr.setEmail(email);
//                    cr.setSessionID(vsRModel.getSessionID());//NOT SURE IF THATS RIGHT
//                    // Now that the ClientRequest has been built, we can add it to our queue of requests.
//                    GatewayService.getThreadPool().getQueue().enqueue(cr);
//                    return Response.status(Response.Status.NO_CONTENT).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
//                }
//                catch (ModelValidationException e)
//                {
//                    ServiceLogger.LOGGER.info("Got some error....");
//                    return ModelValidator.returnInvalidRequest(e);
//                }
//            }
//            else {
//                return Response.status(response.getStatus()).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
//            }
//        }
//        catch (ModelValidationException e)
//        {
//            return ModelValidator.returnInvalidRequest(e);
//        }

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
            VerifySessionResponseModel vsRModel = response.readEntity(VerifySessionResponseModel.class);
            ServiceLogger.LOGGER.info("The result code is: " + vsRModel.getResultCode());

            if (vsRModel.getResultCode() == 130)
            { //Update the session ID (hint pass in a srm object)
                try
                {
                    requestModel = (AddRequestModel) ModelValidator.verifyModel(jsonText, AddRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getMovieConfigs().getEPMovieAdd());
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

    @Path("delete/{movieid}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMovieRequest(@Context HttpHeaders headers, @Context UriInfo uriInfo) {
        ServiceLogger.LOGGER.info("Removes a movie from the database by setting the “hidden” field to true.");
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

//        String query = uriInfo.getRequestUri().getQuery();
        ServiceLogger.LOGGER.info("The query params are: " + queryParams);

        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

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
                ClientRequest cr = new ClientRequest();
                // get the IDM URI from IDM configs
                cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                // get the register endpoint path from IDM configs
                cr.setEndpoint(GatewayService.getMovieConfigs().getEPMovieDelete());
                // set the request model
                cr.setQueryParams(queryParams); // puts the mapped requestModel into the client request
                // set the transactionID
                cr.setTransactionID(transactionID); // as the random generated
                // set the HttpMethodType (GET/POST)
                cr.setHttpMethodType(Constants.deleteRequest);
                cr.setEmail(email);
                cr.setSessionID(vsRModel.getSessionID());//NOT SURE IF THATS RIGHT
                // Now that the ClientRequest has been built, we can add it to our queue of requests.
                GatewayService.getThreadPool().getQueue().enqueue(cr);
                return Response.status(Response.Status.NO_CONTENT).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
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

    @Path("genre")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGenresRequest(@Context HttpHeaders headers) {
        ServiceLogger.LOGGER.info("Received request to return a list of all genres in the database.");


        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

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
//                    requestModel = (SearchRequestModel) ModelValidator.verifyModel(query, SearchRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getMovieConfigs().getEPGenreGet());
                    // set the request model
                    //MIGHT NOT WORK BECAUSE REQUEST MODEL IS NULL!!!!
                    // set the transactionID
                    cr.setTransactionID(transactionID); // as the random generated
                    // set the HttpMethodType (GET/POST)
                    cr.setHttpMethodType(Constants.getRequest);
                    cr.setEmail(email);
                    cr.setSessionID(vsRModel.getSessionID());//NOT SURE IF THATS RIGHT
                    // Now that the ClientRequest has been built, we can add it to our queue of requests.
                    GatewayService.getThreadPool().getQueue().enqueue(cr);
                    return Response.status(Response.Status.NO_CONTENT).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
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

    @Path("genre/add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addGenreRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to add a genre to the database.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        GenreAddRequestModel requestModel;

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
            VerifySessionResponseModel vsRModel = response.readEntity(VerifySessionResponseModel.class);
            ServiceLogger.LOGGER.info("The result code is: " + vsRModel.getResultCode());

            if (vsRModel.getResultCode() == 130)
            { //Update the session ID (hint pass in a srm object)
                try
                {
                    requestModel = (GenreAddRequestModel) ModelValidator.verifyModel(jsonText, GenreAddRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getMovieConfigs().getEPGenreAdd());
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

    @Path("genre/{movieid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGenresForMovieRequest(@Context HttpHeaders headers, @Context UriInfo uriInfo) {
        ServiceLogger.LOGGER.info("Received request to return the IDs and names of the genres a movie belongs to.");
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        ServiceLogger.LOGGER.info("The query params are: " + queryParams);

        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        StringRequestModel requestModel;

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
                ClientRequest cr = new ClientRequest();
                // get the IDM URI from IDM configs
                cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                // get the register endpoint path from IDM configs
                cr.setEndpoint(GatewayService.getMovieConfigs().getEPGenreMovie());
                // set the request model
                cr.setQueryParams(queryParams); // puts the mapped requestModel into the client request
                // set the transactionID
                cr.setTransactionID(transactionID); // as the random generated
                // set the HttpMethodType (GET/POST)
                cr.setHttpMethodType(Constants.getRequest);
                cr.setEmail(email);
                cr.setSessionID(vsRModel.getSessionID());//NOT SURE IF THATS RIGHT
                // Now that the ClientRequest has been built, we can add it to our queue of requests.
                GatewayService.getThreadPool().getQueue().enqueue(cr);
                return Response.status(Response.Status.NO_CONTENT).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
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

    @Path("star/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response starSearchRequest(@Context HttpHeaders headers, @Context UriInfo uriInfo) {
        ServiceLogger.LOGGER.info("Received request for searching movie records that a Star is in.");
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        ServiceLogger.LOGGER.info("The query params are: " + queryParams);

        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);


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
                ClientRequest cr = new ClientRequest();
                // get the IDM URI from IDM configs
                cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                // get the register endpoint path from IDM configs
                cr.setEndpoint(GatewayService.getMovieConfigs().getEPStarSearch());
                // set the request model
                cr.setQueryParams(queryParams); // puts the mapped requestModel into the client request
                // set the transactionID
                cr.setTransactionID(transactionID); // as the random generated
                // set the HttpMethodType (GET/POST)
                cr.setHttpMethodType(Constants.getRequest);
                cr.setEmail(email);
                cr.setSessionID(vsRModel.getSessionID());//NOT SURE IF THATS RIGHT
                // Now that the ClientRequest has been built, we can add it to our queue of requests.
                GatewayService.getThreadPool().getQueue().enqueue(cr);
                return Response.status(Response.Status.NO_CONTENT).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
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

    @Path("star/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStarRequest(@Context HttpHeaders headers, @Context UriInfo uriInfo) {
        ServiceLogger.LOGGER.info("Received request for searching movie records. Retrieves the full details of a particular star identified by their ID, including the ID’s and titles of all movies the actor is in.");
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        ServiceLogger.LOGGER.info("The query params are: " + queryParams);

        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

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
                ClientRequest cr = new ClientRequest();
                // get the IDM URI from IDM configs
                cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                // get the register endpoint path from IDM configs
                cr.setEndpoint(GatewayService.getMovieConfigs().getEPStarGet());
                // set the request model
                cr.setQueryParams(queryParams); // puts the mapped requestModel into the client request
                // set the transactionID
                cr.setTransactionID(transactionID); // as the random generated
                // set the HttpMethodType (GET/POST)
                cr.setHttpMethodType(Constants.getRequest);
                cr.setEmail(email);
                cr.setSessionID(vsRModel.getSessionID());//NOT SURE IF THATS RIGHT
                // Now that the ClientRequest has been built, we can add it to our queue of requests.
                GatewayService.getThreadPool().getQueue().enqueue(cr);
                return Response.status(Response.Status.NO_CONTENT).entity(vsRModel).header("sessionID", sessionID).header("transactionID", transactionID).header("delay", GatewayService.getGatewayConfigs().getRequestDelay()).build();
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

    @Path("star/add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addStarRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to add a Star to the database.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        StarAddRequestModel requestModel;

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
            VerifySessionResponseModel vsRModel = response.readEntity(VerifySessionResponseModel.class);
            ServiceLogger.LOGGER.info("The result code is: " + vsRModel.getResultCode());

            if (vsRModel.getResultCode() == 130)
            { //Update the session ID (hint pass in a srm object)
                try
                {
                    requestModel = (StarAddRequestModel) ModelValidator.verifyModel(jsonText, StarAddRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getMovieConfigs().getEPStarAdd());
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

    @Path("star/starsin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addStarToMovieRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Adds a star to the stars_in_movies table. " +
                "Allows insertion of only one movie at a time. " +
                "Request must contain a valid star id and valid movie id.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        StarsInRequestModel requestModel;

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
            VerifySessionResponseModel vsRModel = response.readEntity(VerifySessionResponseModel.class);
            ServiceLogger.LOGGER.info("The result code is: " + vsRModel.getResultCode());

            if (vsRModel.getResultCode() == 130)
            { //Update the session ID (hint pass in a srm object)
                try
                {
                    requestModel = (StarsInRequestModel) ModelValidator.verifyModel(jsonText, StarsInRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getMovieConfigs().getEPStarIn());
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

    @Path("rating")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRatingRequest(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Received request to rate a movie.");
        String email = headers.getHeaderString("email");
        String sessionID = headers.getHeaderString("sessionID");
        String transactionID = TransactionIDGenerator.generateTransactionID();

        ServiceLogger.LOGGER.info("Email: " +  email);
        ServiceLogger.LOGGER.info("SessionID: " +  sessionID);
        ServiceLogger.LOGGER.info("TransactionID: " +  transactionID);

        RatingRequestModel requestModel;

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
            VerifySessionResponseModel vsRModel = response.readEntity(VerifySessionResponseModel.class);
            ServiceLogger.LOGGER.info("The result code is: " + vsRModel.getResultCode());

            if (vsRModel.getResultCode() == 130)
            { //Update the session ID (hint pass in a srm object)
                try
                {
                    requestModel = (RatingRequestModel) ModelValidator.verifyModel(jsonText, RatingRequestModel.class);
                    ClientRequest cr = new ClientRequest();
                    // get the IDM URI from IDM configs
                    cr.setURI(GatewayService.getMovieConfigs().getMoviesUri());
                    // get the register endpoint path from IDM configs
                    cr.setEndpoint(GatewayService.getMovieConfigs().getEPRating());
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
