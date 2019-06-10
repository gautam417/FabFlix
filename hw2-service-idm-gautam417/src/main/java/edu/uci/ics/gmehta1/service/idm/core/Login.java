package edu.uci.ics.gmehta1.service.idm.core;

public class Login {
    private  String sessionID;
    private String email;
    private int status;

    public Login(String sessionID, String email, int status) {
        this.sessionID = sessionID;
        this.email = email;
        this.status = status;
    }

    public String  getSessionID() {
        return sessionID;
    }

    public String getEmail() {
        return email;
    }

    public int getStatus() {
        return status;
    }

}
