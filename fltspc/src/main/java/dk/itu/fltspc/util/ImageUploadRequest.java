package dk.itu.fltspc.util;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * http://stackoverflow.com/questions/16797468/how-to-send-a-multipart-form-data-post-in-android-with-volley
 */
public class ImageUploadRequest extends Request<JSONObject> {

	private MultipartEntity entity = new MultipartEntity();

	private static final String IMAGE_PART_NAME = "file";
	private static final String STRING_PART_NAME = "text";

	private final Response.Listener<JSONObject> mListener;
	private final byte[] mFilePart;
	private final String mStringPart;
	private final String mFilename;

	public ImageUploadRequest(String url, Response.ErrorListener errorListener, Response.Listener<JSONObject> listener,
	                        String filename, byte[] file) {
		super(Method.POST, url, errorListener);
		mListener = listener;
		mFilePart = file;
		mStringPart = "ohhai.jpg";
		mFilename = filename;
		buildMultipartEntity();
	}

	private void buildMultipartEntity(){
		entity.addPart(IMAGE_PART_NAME, new ByteArrayBody(mFilePart, "image/jpeg", mFilename));
		try {
			entity.addPart(STRING_PART_NAME, new StringBody(mStringPart));
		} catch (UnsupportedEncodingException e) {
			Log.e("MultipartRequest","UnsupportedEncodingException");
		}
	}

	@Override
	public String getBodyContentType() {
		return entity.getContentType().getValue();
		//return "image/jpeg";

	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			entity.writeTo(bos);
		} catch (IOException e) {
			Log.e("MultipartRequest", "IOException writing to ByteArrayOutputStream");
		}
		return bos.toByteArray();
	}

//	@Override
//	protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
//		return Response.success("Uploaded", getCacheEntry());
//	}

	protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString =
					new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			JSONArray data = new JSONArray(jsonString);
			String url = data.getString(0);
			JSONObject jobj = new JSONObject();
			jobj.put("url", url);
			return Response.success(jobj,
					HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JSONException je) {
			return Response.error(new ParseError(je));
		}
	}
	@Override
	protected void deliverResponse(JSONObject response) {
		mListener.onResponse(response);
	}
}