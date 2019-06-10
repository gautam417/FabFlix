package edu.uci.ics.gmehta1.service.api_gateway.models;

public class VerifySessionResponseModel extends Model {
    public int resultCode;
    public String message;
    public String sessionID;

    public VerifySessionResponseModel() {
        resultCode = -100;
        message = null;
        sessionID = null;
    }

    public VerifySessionResponseModel(int rs, String msg, String sID) {
        resultCode = rs;
        message = msg;
        sessionID = sID;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionID() {
        return sessionID;
    }
}