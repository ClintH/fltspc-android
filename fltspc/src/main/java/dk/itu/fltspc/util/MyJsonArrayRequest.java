package dk.itu.fltspc.util;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by Clint Heyer on 25/06/13.
 */
public class MyJsonArrayRequest extends JsonArrayRequest {

    public MyJsonArrayRequest(String url, Response.Listener<JSONArray> listener,
                               Response.ErrorListener errorListener) {
        super(url, listener, errorListener);

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