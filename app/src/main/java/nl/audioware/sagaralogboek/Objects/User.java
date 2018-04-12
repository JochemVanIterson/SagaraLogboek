package nl.audioware.sagaralogboek.Objects;

import org.json.JSONObject;

import java.util.Date;

public class User {
    private int id;
    private String UserName;
    private String FirstName;
    private String LastName;
    private String EmailAdress;
    private boolean isAdmin;
    private Date lastLogin;
    public User(JSONObject object){
        id = object.optInt("id");
        UserName = object.optString("username");
        FirstName = object.optString("firstname");
        LastName = object.optString("lastname");
        EmailAdress = object.optString("mail");
        isAdmin = (object.optInt("username")==1);
        //lastLogin = object.optString("username");
    }

    public int getId() {return id;}
    public String getUserName() {return UserName;}
    public String getFirstName() {return FirstName;}
    public String getLastName() {return LastName;}
    public String getEmailAdress() {return EmailAdress;}
    public boolean isAdmin() {return isAdmin;}
    public Date getLastLogin() {return lastLogin;}
}
