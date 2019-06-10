package edu.uci.ics.gmehta1.service.idm.core;

import edu.uci.ics.gmehta1.service.idm.IDMService;
import edu.uci.ics.gmehta1.service.idm.configs.Configs;
import edu.uci.ics.gmehta1.service.idm.security.Token;

import java.sql.Time;
import java.sql.Timestamp;

public class Sesh {
    private String email;
    private String sessionID;
    private int status;
    private Timestamp timeCreated;
    private Timestamp lastUsed;
    private Timestamp exprTime;

    public Sesh(String email, String sessionID, int status, Timestamp timeCreated, Timestamp lastUsed, Timestamp exprTime) {
        this.email = email;
        this.sessionID = sessionID;
        this.status = status;
        this.timeCreated = timeCreated;
        this.lastUsed = lastUsed;
        this.exprTime = exprTime;
    }

    public Sesh(String email, String sessionID) {
        this.email = email;
        this.sessionID = sessionID;
    }

//    public static Sesh createSesh (String e, String s){
//        return new Sesh(e,s);
//    }
//    public Sesh(String email, Token token, Timestamp timeCreated, Timestamp lastUsed, Timestamp exprTime) {
//        this.email = email;
//        this.sessionID = sessionID;
//        this.timeCreated = timeCreated;
//        this.lastUsed = lastUsed;
//        this.exprTime = exprTime;
//    }
//    public static Sesh rebuildSesh(String e, Token sessionID, Timestamp timeCreated, Timestamp lastUsed, Timestamp exprTime){
//        return new Sesh (e,sessionID, timeCreated,lastUsed,exprTime);
//    }
//    public boolean isDataValid(){
//        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
//        if (currentTime.after(exprTime))
//        {
//            return false;
//        }
//        return true;
////        Timestamp timeout = new Timestamp(lastUsed.getTime()+);
////        if (currentTime.after(timeout)){
////            return false;
////        }
//    }
//    public void update() {
//        if(isDataValid()){
//            lastUsed = new Timestamp(System.currentTimeMillis());
//        }
//    }
    public int getStatus() {
        return status;
    }

    public Timestamp getTimeCreated() {
        return timeCreated;
    }

    public Timestamp getLastUsed() {
        return lastUsed;
    }

    public Timestamp getExprTime() {
        return exprTime;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
