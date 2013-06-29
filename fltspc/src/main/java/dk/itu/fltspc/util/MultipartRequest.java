package dk.itu.fltspc.util;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * http://stackoverflow.com/questions/16797468/how-to-send-a-multipart-form-data-post-in-android-with-volley
 */
public class MultipartRequest extends Request<String> {

	private MultipartEntity entity = new MultipartEntity();

	private static final String FILE_PART_NAME = "file";
	private static final String STRING_PART_NAME = "text";

	private final Response.Listener<String> mListener;
	private final File mFilePart;
	private final String mStringPart;

	public MultipartRequest(String url, Response.ErrorListener errorListener, Response.Listener<String> listener,
	                        File file, String stringPart) {
		super(Method.POST, url, errorListener);

		mListener = listener;
		mFilePart = file;
		mStringPart = stringPart;
		buildMultipartEntity();

	}

	private void buildMultipartEntity(){
		entity.addPart(FILE_PART_NAME, new FileBody(mFilePart));
		try {
			entity.addPart(STRING_PART_NAME, new StringBody(mStringPart));
		} catch (UnsupportedEncodingException e) {
			Log.e("MultipartRequest","UnsupportedEncodingException");
		}

	}

	@Override
	public String getBodyContentType() {
		return entity.getContentType().getValue();
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

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		return Response.success("Uploaded", getCacheEntry());
	}

	@Override
	protected void deliverResponse(String response) {
		mListener.onResponse(response);
	}
}