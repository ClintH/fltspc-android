package dk.itu.fltspc;

import java.util.ArrayList;
import java.util.List;

import dk.itu.fltspc.util.EventDispatch;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class Surfaces {
    private App app;
    public ArrayList<Surface> data;
	public EventDispatch events;

    public Surfaces(App app) {
        this.app = app;
        this.data = new ArrayList<Surface>();
        this.events = new EventDispatch(app.events);
    }

	public Surface getByLongId(String id) {
		for (Surface s : data) {
			if (s.id.contentEquals(id)) return s;
		}
		return null;
	}

    public void clear() {
        this.data.clear();
    }

    public void add(Surface s) {
	    for (Surface existing : data) {
		    if (existing.id == s.id) throw new IllegalArgumentException("Surface exists");
	    }
        data.add(s);
    }

	public void replace(Surface old, Surface newSurface) {
		data.remove(old);
		data.add(newSurface);
	}

	public void remove(String longId) {
		for (Surface existing: data) {
			if (existing.id.contentEquals(longId))
				data.remove(existing);
		}
	}

	public void remove(Surface s) {
		data.remove(s);
	}

    public int size() {
        return data.size();
    }
}
