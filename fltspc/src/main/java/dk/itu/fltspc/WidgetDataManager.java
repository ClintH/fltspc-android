package dk.itu.fltspc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import dk.itu.fltspc.util.EventDispatch;
import dk.itu.fltspc.util.IComplete;
import dk.itu.fltspc.util.ImageUploadRequest;
import dk.itu.fltspc.util.JsonErrorObject;
import dk.itu.fltspc.util.JsonUtil;
import dk.itu.fltspc.util.MultipartRequest;
import dk.itu.fltspc.util.MyJsonArrayRequest;
import dk.itu.fltspc.util.MyVolley;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class WidgetDataManager {
	static boolean fetchingWidgets;
	static boolean writingWidgets;

	private WidgetDataManager() {
	}

	public static Widget fromData(JSONObject o, Surface surface) throws JSONException, ParseException {
		Widget s = new Widget(surface);
		updateWithData(s, o);
		return s;
	}

	public static void updateWithData(Widget s, JSONObject o)  throws JSONException, ParseException {
		//Log.i("WidgetDataMgr","updateWithData: " +  o.toString());

		JSONObject size = o.getJSONObject("size");
		JSONObject pos = o.getJSONObject("pos");
		if (o.has("id"))
			s.setId(o.getString("id"));
		else
			s.setId(o.getString("_id"));
		s.setType(o.getString("type"));
		s.setContent(o.getString("content"));
		s.setZorder(o.getInt("zorder"));
//		s.pinned = o.getBoolean("pinned");
//		s.flipped = o.getBoolean("flipped");
//		s.contentBack = o.getString("contentBack");
//		s.trans = o.getString("trans");
//		s.collapsed = o.getBoolean("collapsed");
		//createdAt, updatedAt, surfaceId,owner
		s.setTitle(o.getString("title"));
		s.setHeight(size.getDouble("height"));
		s.setWidth(size.getDouble("width"));
		s.setLeft(pos.getDouble("left"));
		s.setTop(pos.getDouble("top"));
		s.setBgColour(o.getString("bgColour"));
	}

	public static JSONObject toData(Widget w) throws JSONException {
		JSONObject o = new JSONObject();
		o.put("id", w.getId());
		o.put("surfaceId", w.getSurfaceId());
		o.put("type", w.getType());
		o.put("content", w.getParsedContent());
		o.put("zorder", w.getZorder());
		o.put("title", w.getTitle());

		JSONObject size = new JSONObject();
		size.put("height", w.getHeight());
		size.put("width", w.getWidth());
		o.put("size", size);

		JSONObject pos = new JSONObject();
		pos.put("left", w.getLeft());
		pos.put("top", w.getTop());
		o.put("pos", pos);

		o.put("bgColour", w.getBgColour());
		return o;
	}

	public static void update(final Widget w, boolean optimise, final Response.Listener<JSONObject> l)  throws JSONException {
		writingWidgets = true;
		JSONObject o = null;
		try {
			o = toData(w);
			String[] dirty = w.events.getFiredAndReset();
			Log.i("WidgetDataMgr", "update: " + o.toString());
			for (String s : dirty)
				Log.i("WidgetDataMgr", "dirty: " + s.toString());

			if (optimise) {
				JSONObject oOpt = new JSONObject();
				for (String s : dirty) {
					String key = s.substring(s.indexOf(":")+1);
					if (o.has(key)) {
						oOpt.put(key, o.get(key));
					}
				}
				o = oOpt;
			}
		} catch (JSONException ex) {
			writingWidgets = false;
			throw ex;
		}

		MyVolley.add(new JsonObjectRequest(Request.Method.PUT, App.Instance.getBaseUrl() + "widget/" + w.getId() +"/", o, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				writingWidgets = false;
				try {
					WidgetDataManager.updateWithData(w, response);
					JsonUtil.handle(response, l);
				} catch (Exception e) {
					Log.e("WidgetDataMgr", "Create",  e);
					JsonUtil.handle(e, l);
				}
			}
		},
		new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				writingWidgets = false;
				JsonUtil.handle(error, l);
			}
		}));

	}

	// Creates a widget on the server. Doesn't add it to local models though
	public static void create(final Widget w, final Response.Listener<JSONObject> l) throws JSONException {
		writingWidgets = true;
		Log.i("WidgetDataMgr", "Creating...");
		JSONObject o = null;
		try {
			o = WidgetDataManager.toData(w);
		} catch (JSONException ex) {
			writingWidgets = false;
			throw ex;
		}
		MyVolley.add(new JsonObjectRequest(Request.Method.POST, App.Instance.getBaseUrl() + "widget/", o,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.i("WidgetDataMgr", "Create ok");
						Log.i("WidgetDataMgr", response.toString());
						writingWidgets = false;
						try {
							WidgetDataManager.updateWithData(w, response);
							JsonUtil.handle(response, l);
						} catch (Exception e) {
							Log.e("WidgetDataMgr", "Create",  e);
							JsonUtil.handle(e, l);
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						writingWidgets = false;
						JsonUtil.handle(error, l);
					}
				}
		));
	}

	public static void delete(Widget w, final Response.Listener<JSONObject> l) {
		writingWidgets = true;
		Log.i("WidgetDataMgr", "Deleting...");
		MyVolley.add(new JsonObjectRequest(Request.Method.DELETE, App.Instance.getBaseUrl() + "widget/" + w.getId() + "/", null,
			new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
					Log.i("WidgetDataMgr", "Delete ok");
					Log.i("WidgetDataMgr", response.toString());

					writingWidgets = false;
					JsonUtil.handle(response, l);
				}
			},
			new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					writingWidgets = false;
					Log.e("WidgetDataMgr", "Delete Error Response: " + error.getMessage());
					JsonUtil.handle(error, l);
				}
			}
		));
	}

	public static void upload(Bitmap image, final Response.Listener<JSONObject> l) {
		Log.i("WidgetDataMgr", "upload");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] imageAsBytes = baos.toByteArray();

		ImageUploadRequest mr = new ImageUploadRequest(App.Instance.getBaseUrl() + "upload", new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				JsonUtil.handle(error, l);
			}
		}, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				JsonUtil.handle(response, l);

			}
		}, "hello.jpg", imageAsBytes);
		MyVolley.add(mr);
	}

	public static Bitmap getImage(String url) {
		Bitmap image = null;
		try {
			image = BitmapFactory.decodeStream((new URL(url).openConnection().getInputStream()));
		} catch (Exception e) {
			Log.e("WidgetDataMgr", "getImage", e);
		}
		return image;
	}

	// Returns ArrayList<Widget> on ICompletion
	// Returns JSONObject if there was an error
	public static void get(final Surface surface, final IComplete completion) throws Exception {
		if (fetchingWidgets) throw new Exception("Already fetching");
		if (writingWidgets) throw new Exception("Writing");
		fetchingWidgets = true;
		final ArrayList<Widget> t = new ArrayList<Widget>();

		MyVolley.add(new MyJsonArrayRequest(App.Instance.getBaseUrl() + "surface/" + surface.id + "/widgets",
			new Response.Listener<JSONArray>() {
				@Override
				public void onResponse(JSONArray response) {
					fetchingWidgets = false;
					if (writingWidgets) {
						if (completion != null) completion.onComplete(new JsonErrorObject("Refusing to merge widgets while saving"));
						return;
					};

					for (int i=0;i<response.length();i++) {
						try {
							t.add(fromData(response.getJSONObject(i), surface));
						} catch (Exception e) {
							Log.e("WidgetDataMgr", "get.onResponse", e);
						}
					}
					if (completion != null)
						completion.onComplete(t);
				}
			},
			new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					fetchingWidgets = false;
					if (completion != null)
						completion.onComplete(new JsonErrorObject(error));
				}
			}
		));
	}
}
