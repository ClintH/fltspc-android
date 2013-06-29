package dk.itu.fltspc;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import dk.itu.fltspc.util.Event;
import dk.itu.fltspc.util.EventDispatch;
import dk.itu.fltspc.util.IOnEvent;
import dk.itu.fltspc.util.MyVolley;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class App extends Application {
    //public static String PREFS = "prefs";
    public static App Instance;

    private Session session;
    private final Surfaces surfaces = new Surfaces(this);

    private boolean inited = false;

	public EventDispatch events;

    public App() {
        Log.i("App", "Ctor");
        App.Instance = this;
		this.events = new EventDispatch();

	    this.events.bind(new IOnEvent() {
		    @Override
		    public void on(Event e) {
			    Log.i("App", e.name + " - " + e.toString());
		    }
	    });
    }

    public Session getSession() {
        if (!inited) init();
        return session;
    }

    public String getBaseUrl() {
        if (!inited) init();

	    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
	    String baseUrl = sp.getString("pref_server", "http://192.168.1.12:3000/");
	    if (!baseUrl.endsWith("/")) baseUrl += "/";
	    return baseUrl;
    }

    public Surfaces getSurfaces() {
        if (!inited) init();
        return surfaces;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void init() {
        if (inited) return;
        inited = true;
        Log.i("App", "Initialised");
        MyVolley.init(this);
        session = new Session(this);

        Log.i("App", "Base url: " + getBaseUrl());
    }
}
