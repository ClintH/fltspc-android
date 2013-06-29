package dk.itu.fltspc.util;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class JsonErrorObject extends JSONObject {
    public VolleyError source;

    public JsonErrorObject(VolleyError e) {
	    String msg = null;
	    try {
		    msg = e.getMessage();
	    } catch (NullPointerException ex) {
		    msg = e.toString();
	    }
		if (msg == null) msg = e.getClass().getName();
        Log.i("JsonErrorObject", msg);
        if (e.networkResponse != null)
            Log.i("JsonErrorObject", e.networkResponse.toString());
       try {
        this.put("isError", true);
        this.put("message", msg);
       } catch (Exception ex) {
           Log.e("JsonErrorObject", ex.toString());
       }

        this.source = e;
    }

	public JsonErrorObject(String msg) {
		try {
			this.put("isError", true);
			this.put("message", msg);
		} catch (Exception e) {
			Log.e("JsonErrorObject", "ctor", e);
		}
	}

	public JsonErrorObject(Exception ex) {
		try {
			this.put("isError", true);
			this.put("message", ex.getMessage());
		} catch (Exception e) {
			Log.e("JsonErrorObject", "ctor", e);
		}
	}


}
