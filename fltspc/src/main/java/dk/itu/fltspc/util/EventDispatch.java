package dk.itu.fltspc.util;

import android.util.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.FutureTask;

/**
 * Created by mobclhe on 27/06/13.
 */
public class EventDispatch {
	public EventDispatch parent;

	final ArrayList<IOnEvent> listeners = new ArrayList<IOnEvent>();
	final Multimap<String, IOnEvent> listenerMap = HashMultimap.create();
	Object defaultSource;
	HashMap<String, String> fired;

	public EventDispatch(EventDispatch parent) {
		this.parent = parent;
		this.fired = new HashMap<String, String>();
	}

	public EventDispatch() {
		this.fired = new HashMap<String, String>();
	}

	public void resetFired() {
		fired.clear();
	}

	public String[] getFiredAndReset() {
		// TODO Thread safety
		String[] values = fired.values().toArray(new String[0]);
		fired.clear();
		return values;
	}

	public void bind(String key, IOnEvent listener) {
		synchronized (listeners) {
			listenerMap.put(key, listener);
		}
	}

	public void bind(IOnEvent listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	public void unbind(IOnEvent listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public void unbind(String key, IOnEvent listener) {
		synchronized (listenerMap) {
			listenerMap.remove(key, listener);
		}
	}

	public void trigger(String name) {
		trigger(name, defaultSource);
	}

	public void trigger(String name,Object source) {
		Event e = new Event(name, source);
		trigger(e);
	}

	public void trigger(String name, JSONObject data, Object source) {
		Event e = new Event(name, source);
		e.data = data;
		trigger(e);
	}

	public void trigger(final Event e) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					triggerAsync(e);
				} catch (Exception ex) {
					Log.e("EventDispatch", "Error", ex);
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
	}

	void triggerAsync(Event e) {
		synchronized (listeners) {
			for (IOnEvent listener : listeners) {
				listener.on(e);
			}
		}
		fired.put(e.name,e.name);

		String[] names = new String[1];
		names[0] = e.name;
		if (e.name.contains(":")) {
			String[] split = e.name.split(":");
			names = new String[split.length + 1];
			System.arraycopy(split, 0, names, 0, split.length);
			names[names.length-1] = e.name;
		}

		synchronized (listenerMap) {
			for (int i=0;i<names.length;i++) {
				if (listenerMap.containsKey(names[i])) {
					for (IOnEvent listener : listenerMap.get(names[i])) {
						listener.on(e);
					}
				}
			}
		}

		if (parent != null) {
			if (!e.preventPropagation)
				parent.trigger(e);
		}
	}
}

