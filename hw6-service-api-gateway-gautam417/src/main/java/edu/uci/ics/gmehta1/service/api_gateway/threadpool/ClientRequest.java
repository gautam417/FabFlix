package edu.uci.ics.gmehta1.service.api_gateway.threadpool;

import edu.uci.ics.gmehta1.service.api_gateway.models.RequestModel;

import javax.ws.rs.core.MultivaluedMap;

public class ClientRequest {
    private String email;
    private String sessionID;
    private String transactionID;
    private RequestModel request;
    private String URI; // uri of the endpoint for example billing/payment
    private String endpoint;
    private int httpMethodType;
    private MultivaluedMap<String, String> queryParams;

    // ooen ended, create something to catch the HTTP request type

    public ClientRequest() {
        this.email = null;
        this.sessionID = null;
        this.transactionID = null; // it says NOT NULL on DB
        this.request = null;
        this.URI = null;
        this.endpoint = null;
        this.queryParams = null;
        this.httpMethodType = 101;
    }

    public ClientRequest (String email, String sessionID, String transactionID, RequestModel request, String URI, String endpoint, int httpMethodType, MultivaluedMap<String, String> queryParams) {
        this.email = email;
        this.sessionID = sessionID;
        this.transactionID = transactionID;
        this.request = request;
        this.URI = URI;
        this.endpoint = endpoint;
        this.httpMethodType = httpMethodType;
        this.queryParams = queryParams;
    }

    public MultivaluedMap<String, String> getQueryParams() { return queryParams; }

    public void setQueryParams(MultivaluedMap<String, String> queryParams) { this.queryParams = queryParams; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSessionID() { return sessionID; }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getTransactionID() { return transactionID; }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public RequestModel getRequest() { return request; }

    public void setRequest(RequestModel request) {
        this.request = request;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getHttpMethodType() {
        return httpMethodType;
    }

    public void setHttpMethodType(int httpMethodType) {
        this.httpMethodType = httpMethodType;
    }
}
