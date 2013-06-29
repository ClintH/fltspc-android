package dk.itu.fltspc.util;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mobclhe on 28/06/13.
 */
public class JsonUtil {
	public static void handle(JSONObject o, Response.Listener<JSONObject> l) {
		if (l == null) return;
		try {
			o.put("isError", false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		l.onResponse(o);
	}
	public static void handle(VolleyError e, Response.Listener<JSONObject> l) {
		if (l == null) return;
		l.onResponse(new JsonErrorObject(e));
	}

	public static void handle(Exception e, Response.Listener<JSONObject> l) {
		if (l == null) return;
		l.onResponse(new JsonErrorObject(e));
	}

	public static void handle(Response.Listener<JSONObject> l) {
		if (l == null) return;
		JSONObject o = new JSONObject();
		try {
			o.put("isError", false);
			o.put("message", "");
		} catch (Exception ex) {

		}
		l.onResponse(o);
	}
}
