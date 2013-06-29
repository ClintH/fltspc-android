package dk.itu.fltspc;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import dk.itu.fltspc.util.JsonErrorObject;
import dk.itu.fltspc.util.JsonUtil;
import dk.itu.fltspc.util.MyJsonObjectRequest;
import dk.itu.fltspc.util.MyVolley;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class Session {

    public SignIn Credentials;
    public JSONObject Data;

    private App app;


   // {"lastSurface":"51a0a38474270524ef000db2","id":"4fe1c93c27f5abb578000024","isAdmin":false,"colourScheme":"pastels1","email":"a@a.com","personalColour":"green"}

    public Session(App app) {
        this.app = app;
    }

    public void stop() {

    }

    public String getEmail() {
        try {
            return Data.getString("email");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getId() {
        try {
            return Data.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Try to restore a session, or at least see if one is present
    public void whoAmI(final Response.Listener<JSONObject> l) {
        MyVolley.add(new JsonObjectRequest(app.getBaseUrl() + "acc/whoami", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("Session", "Whoami response: " + response);
                JsonUtil.handle(response, l);
            }
        },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
	            JsonUtil.handle(error, l);
            }
        }));
    }

    public void start(SignIn c, final Response.Listener<JSONObject> l) {
        if (c == null) throw new NullPointerException("SignIn");
        Log.i("session", "Session start");
        this.Credentials = c;
        final Session me = this;

        try {
            MyVolley.add(new MyJsonObjectRequest(Request.Method.POST, app.getBaseUrl() + "acc/login", c.toJson(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("Session", "Signin response: " + response);
                    me.Data = response;
                    app.getSurfaces().clear();
	                JsonUtil.handle(response, l);
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
	                        String msg = error.getMessage();
	                        Throwable cause = error.getCause();
	                        if (cause != null)
	                            Log.i("Session", "Error cause: " + cause.toString());
                            Log.i("Session", "Error Response: " + msg);
                            me.Credentials = null;
                            me.Data = null;
                            JSONObject o = new JsonErrorObject(error);
                            if (msg != null && msg.contains("No authentication challenges found")) {
                                try {
                                    o.put("message", "Invalid username or password");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                           } else {
	                            try {
	                                o.put("message", "Unknown error");
	                            } catch (JSONException e) {
		                            e.printStackTrace();
	                            }
                            }
                           l.onResponse(o);
                        }
                    }
            ));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
