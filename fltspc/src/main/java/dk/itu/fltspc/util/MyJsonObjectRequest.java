package dk.itu.fltspc.util;

import android.util.Base64;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by Clint Heyer on 25/06/13.
 */
public class MyJsonObjectRequest extends JsonObjectRequest{

    public MyJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener,
                               Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
//        if (username != null && password != null) {
//            String loginEncoded = new String(Base64.encode((username + ":" + password).getBytes(), Base64.NO_WRAP));
//            this.headers.put("Authorization", "Basic " + loginEncoded);
//        }
//
        headers = new HashMap<String, String>();;
        headers.put("content-type", "application/json; utf-8");
    }

    private Map<String, String> headers;
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }
    public void setHeader(String title, String content) {
        headers.put(title, content);
    }
}