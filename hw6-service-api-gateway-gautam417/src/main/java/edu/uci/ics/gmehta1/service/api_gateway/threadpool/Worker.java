package edu.uci.ics.gmehta1.service.api_gateway.threadpool;

import edu.uci.ics.gmehta1.service.api_gateway.GatewayService;
import edu.uci.ics.gmehta1.service.api_gateway.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.api_gateway.models.RequestModel;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Worker extends Thread {
    int id;
    ThreadPool threadPool;

    private Worker(int id, ThreadPool threadPool) {
        this.id=id;
        this.threadPool=threadPool;
    }

    public static Worker CreateWorker(int id, ThreadPool threadPool) {
        return new Worker(id,threadPool);
    }

    public void process() throws InterruptedException {
        ClientRequest currentRequest = threadPool.remove();
        if (currentRequest == null){
            return;
        }
        // Create a new Client
        ServiceLogger.LOGGER.info("Building client...");
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);

        WebTarget webTarget = client.target(currentRequest.getURI()).path(currentRequest.getEndpoint());

        // Create a WebTarget to send a request at
        ServiceLogger.LOGGER.info("Building WebTarget...");

        if (currentRequest.getQueryParams() != null){
            //apply query parameters
            for (Map.Entry<String, List<String>> entry : currentRequest.getQueryParams().entrySet()) {
                ServiceLogger.LOGGER.info("Adding query parameter: " + entry.getKey() + ", value: " + entry.getValue());
                webTarget = webTarget.queryParam(entry.getKey(), entry.getValue().get(0));
            }
        }
        // Create an InvocationBuilder to create the HTTP request
        ServiceLogger.LOGGER.info("Starting invocation builder...");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        // Set the payload
        ServiceLogger.LOGGER.info("Setting payload of the request");
        RequestModel requestModel = currentRequest.getRequest();

        // Send the request and save it to a Response
        ServiceLogger.LOGGER.info("Sending request...");
        Response response = null;
//        response = invocationBuilder.get();

        if (currentRequest.getHttpMethodType() == Constants.postRequest){
            ServiceLogger.LOGGER.info("This is a POST request");
            response = invocationBuilder.post(Entity.entity(requestModel,MediaType.APPLICATION_JSON));
        }
        else if (currentRequest.getHttpMethodType() == Constants.getRequest){
            ServiceLogger.LOGGER.info("This is a GET request");
//            response = invocationBuilder.get();
            response = invocationBuilder.method("GET", Entity.entity(requestModel,MediaType.APPLICATION_JSON));
        }
        else{
            ServiceLogger.LOGGER.info("This is a DELETE request");
            response = invocationBuilder.method("DELETE", Entity.entity(requestModel,MediaType.APPLICATION_JSON));
        }
        ServiceLogger.LOGGER.info("Sent!");

        // Add response to database
        try{
            Connection con = GatewayService.getConPool().requestCon();
            PreparedStatement ps = con.prepareStatement("INSERT INTO responses(transactionId, email, sessionid, response, httpstatus ) " +
                    "VALUES (?, ?, ?, ?, ?)");
            ps.setString(1,currentRequest.getTransactionID());
            ps.setString(2, currentRequest.getEmail());
            ps.setString(3, currentRequest.getSessionID());
            ps.setString(4, response.readEntity(String.class));
            ps.setInt(5, response.getStatus());
            ps.executeUpdate();
            GatewayService.getConPool().releaseCon(con);
        } catch (SQLException e){
            ServiceLogger.LOGGER.warning("INSERT Query in Worker issue.");
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        while (true) {
            try {
                process();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

