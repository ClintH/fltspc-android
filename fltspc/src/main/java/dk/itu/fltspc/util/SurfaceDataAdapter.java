//package dk.itu.fltspc.util;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.lang.reflect.Type;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.List;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.ByteArrayBody;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.util.Log;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//public class SurfaceDataAdapter {
//
//	private static URL url;
//	private static String _FLTSPCurl = "http://fltspc.itu.dk/";
//	private static String _surfaceID = "/515186ff17450f312b001372";
//
//	@SuppressWarnings("unchecked")
//	public static List<SurfaceWidget> GetSurfaceWidgets() {
//		try {
//			HttpURLConnection hurl = getConnectionObject("GET", _FLTSPCurl + "surface" + _surfaceID + "/widgets");
//			hurl.connect();
//
//			String response = getHTTPResponseAsString(hurl);
//			Gson gson = new Gson();
//			Type listType = new TypeToken<List<SurfaceWidget>>() {}.getType();
//			List<SurfaceWidget> swList = (List<SurfaceWidget>) gson.fromJson(response, listType);
//
//			SurfaceWidget tmp = null;
//
//			for (int i = 0; i < swList.size(); i++) {
//				for (int j = swList.size() - 1; j >= i + 1; j--) {
//					if (swList.get(j).getZorder().intValue() < swList.get(j - 1).getZorder().intValue()) {
//						tmp = swList.get(j);
//						swList.add(j, swList.get(j - 1));
//						swList.remove(j + 1);
//						swList.add(j - 1, tmp);
//						swList.remove(j);
//					}
//				}
//			}
//
//			return swList;
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public static void EditWidget(SurfaceWidget sw) {
//		Gson gson = new Gson();
//		String jsonWidget = gson.toJson(sw);// .replace("\"", "\\\"");
//
//		try {
//			HttpURLConnection hurl = getConnectionObject("PUT", _FLTSPCurl + "widget/" + sw.get_id() + "/");
//			System.out.println(hurl.getURL());
//
//			hurl.connect();
//			OutputStreamWriter os = new OutputStreamWriter(hurl.getOutputStream());
//			os.write(jsonWidget);
//			os.flush();
//			os.close();
//			System.out.println(hurl.getResponseCode());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static String AddWidget(SurfaceWidget sw) {
//
//		Gson gson = new Gson();
//		String jsonWidget = gson.toJson(sw);
//
//		try {
//			HttpURLConnection hurl = getConnectionObject("POST", _FLTSPCurl + "widget/");
//			hurl.connect();
//			OutputStreamWriter os = new OutputStreamWriter(hurl.getOutputStream());
//			os.write(jsonWidget);
//			os.flush();
//			BufferedReader rd = new BufferedReader(new InputStreamReader(hurl.getInputStream()));
//			SurfaceWidget responseSw = (SurfaceWidget) gson.fromJson(rd.readLine(), SurfaceWidget.class);
//			os.close();
//
//			return responseSw.get_id();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public static int GetHighiestZorder() {
//		List<SurfaceWidget> widgets = GetSurfaceWidgets();
//		return widgets.get(widgets.size() - 1).getZorder().intValue();
//	}
//
//	public static boolean DeleteWidget(SurfaceWidget sw) {
//
//		Log.v("MY", "GO!");
//		System.out.println("GO!");
//
//		try {
//			HttpURLConnection hurl = getConnectionObject("DELETE", _FLTSPCurl + "widget/" + sw.get_id() + "/");
//			hurl.connect();
//			Log.v("MY", String.valueOf(hurl.getURL()));
//			System.out.println(hurl.getURL());
//			InputStreamReader isr = new InputStreamReader(hurl.getInputStream());
//			isr.read();
//
//			return true;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
//
//	public static Bitmap GetWidgetImage(String url) {
//		Bitmap image = null;
//		try {
//			System.out.println("URL: " + url);
//			image = BitmapFactory.decodeStream((new URL(url).openConnection().getInputStream()));
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return image;
//	}
//
//	public static String uploadImage(Bitmap image) {
//		try {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//			byte[] imageAsBytes = baos.toByteArray();
//
//			HttpClient httpClient = new DefaultHttpClient();
//			HttpPost post = new HttpPost("http://fltspc.itu.dk/upload");
//			MultipartEntity me = new MultipartEntity();
//			me.addPart("file", new ByteArrayBody(imageAsBytes, "image/jpeg", "dummy.jpg"));
//			post.setEntity(me);
//			HttpResponse resp = httpClient.execute(post);
//			HttpEntity ent = resp.getEntity();
//			String tmpUrl = EntityUtils.toString(ent);
//			String imageURL = tmpUrl.replace("[\"", "").replace("\"]", "");
//
//			return imageURL;
//
//		} catch (IOException e) {
//
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	private static String getHTTPResponseAsString(HttpURLConnection hurl) throws IOException {
//		BufferedReader br = new BufferedReader(new InputStreamReader(hurl.getInputStream()));
//		StringBuilder sb = new StringBuilder();
//		String line;
//		while ((line = br.readLine()) != null)
//			sb.append(line);
//		br.close();
//
//		return sb.toString();
//	}
//
//	private static HttpURLConnection getConnectionObject(String requestType, String requestUrl) throws IOException {
//		URL url = new URL(requestUrl);
//		HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
//		hurl.setRequestMethod(requestType);
//
//		if (requestType == "PUT" || requestType == "POST") {
//			hurl.setDoOutput(true);
//			hurl.setRequestProperty("Content-Type", "application/json");
//			hurl.setRequestProperty("Accept", "application/json");
//		}
//
//		return hurl;
//	}
//
//}
