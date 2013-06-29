package dk.itu.fltspc;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.itu.fltspc.util.IComplete;
import dk.itu.fltspc.util.JsonParse;
import dk.itu.fltspc.util.MyJsonArrayRequest;
import dk.itu.fltspc.util.MyJsonObjectRequest;
import dk.itu.fltspc.util.MyVolley;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class SurfaceDataManager {
    private static boolean fetchingSurfaces;

    private SurfaceDataManager() {
    }

    public static Surface create(JSONObject o) throws JSONException, ParseException {
        Surface s = new Surface(App.Instance.getSurfaces());
        s.id = o.getString("id");
        s.updatedAt = JsonParse.parseDateTime("updatedAt", o);
        s.title = o.getString("title");
        s.height= o.getDouble("height");
        s.width = o.getDouble("width");
        s.shortId = o.getString("shortId");
        s.archived = o.getBoolean("archived");
        s.bgColour =o.getString("bgColour");
        s.createdAt = JsonParse.parseDateTime("createdAt", o);
        s.publicRead = o.getBoolean("publicRead");
        s.publicWrite = o.getBoolean("publicWrite");
        s.owner = o.getString("owner");
        return s;
    }


    public static void getAll(Session session, final Surfaces destination, final IComplete completion) throws Exception {
        if (fetchingSurfaces) throw new Exception("Already fetching");
        fetchingSurfaces = true;
        final List<Surface> t = new ArrayList<Surface>();

        MyVolley.add(new MyJsonArrayRequest(App.Instance.getBaseUrl() + "surfaces",
           new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    fetchingSurfaces = false;
                    destination.clear();
                    for (int i=0;i<response.length();i++) {
                        try {
                            destination.add(create(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e("SurfaceDataMgr", e.toString());
                        }
                    }
                    if (completion != null)
                        completion.onComplete(t);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    fetchingSurfaces = false;
                    Log.i("SurfaceDataMgr", "Error Response: " + error.getMessage());
                    if (completion != null)
                        completion.onComplete(error);
                }
            }));
    }
}
