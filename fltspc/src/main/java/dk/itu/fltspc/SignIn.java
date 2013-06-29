package dk.itu.fltspc;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class SignIn {
    public String Email, Password;

    public SignIn() {

    }

    public SignIn(String email, String pass) {
        this.Email = email;
        this.Password = pass;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("email" , Email);
        o.put("password", Password);
        return o;
    }

}
