package dk.itu.fltspc.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.net.CookieHandler;
import java.net.CookieManager;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class MyVolley {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;

    private MyVolley() {

    }

    public static void add(Request r) {
        get().add(r);
    }

    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(100));
        CookieManager cookieMgr = new CookieManager();
        CookieHandler.setDefault(cookieMgr);
    }

    public static RequestQueue get() {
        if (mRequestQueue != null)
            return mRequestQueue;
        throw new IllegalStateException("Queue not inited");
    }

    public static ImageLoader getImageLoader() {
        if (mImageLoader != null)
            return mImageLoader;
        throw new IllegalStateException("Image loaded not inited");
    }
}
