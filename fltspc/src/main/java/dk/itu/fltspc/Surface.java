package dk.itu.fltspc;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import dk.itu.fltspc.util.EventDispatch;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class Surface {
    public String bgColour;
    public String id;
    public String shortId;
    public String title;
    public String owner;
    public Date updatedAt, createdAt;
    public double width, height;
    public boolean publicRead, publicWrite, archived;

	ArrayList<Widget> widgets;
	HashMap<String, Widget> widgetMap;

	public EventDispatch events;

    public Surface(Surfaces surfaces) {
		widgets = new ArrayList<Widget>();
        widgetMap = new HashMap<String, Widget>();
        events = new EventDispatch(surfaces.events);
    }

	public int getHighestZorder() {
		int max = 0;
		for (Widget w : widgets) {
			max = Math.max(max, w.getZorder());
		}
		return max;
	}

	public class MergeResult {
		public ArrayList<Widget> deleted;
		public ArrayList<Widget> created;
		public ArrayList<Widget> changed;
		public ArrayList<HashMap<String, String>> changeSets;
		public MergeResult() {
			deleted = new ArrayList<Widget>();
			created = new ArrayList<Widget>();
			changed = new ArrayList<Widget>();
			changeSets = new ArrayList<HashMap<String, String>>();
		}
	}

	// Called after we get data from server
	// Copies data from incoming instances to existing instances where possible
	public MergeResult mergeUpdate(ArrayList<Widget> widgets) {
		if (widgets == null) throw new IllegalArgumentException("widgets null");
		MergeResult mr = new MergeResult();

		for (Widget w : widgets) {
			if (contains(w.getId())) {
				Widget existing = widgetMap.get(w.getId());
				HashMap<String,String> changes =  w.copyTo(existing);
				if (changes != null) {
					mr.changed.add(existing);
					mr.changeSets.add(changes);
				}
			} else {
				this.widgets.add(w);
				this.widgetMap.put(w.getId(), w);
				mr.created.add(w);
			}
		}
		for (Widget w : this.widgets.toArray(new Widget[this.widgets.size()])) {
			boolean found = false;
			for (Widget ww : widgets) {
				if (ww.getId().equals(w.getId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				mr.deleted.add(w);
				this.widgets.remove(w);
				this.widgetMap.remove(w.getId());
			}
		}
		return mr;
	}

	public boolean contains(String widgetId) {
		return widgetMap.containsKey(widgetId);
	}

	public Widget get(String widgetId) {
		return widgetMap.get(widgetId);
	}

	public int getColour() {
		return Color.parseColor(bgColour);
	}

	public Surface clone(boolean deep) {
		Surface s = new Surface(App.Instance.getSurfaces());
		s.bgColour = bgColour;
		s.id = id;
		s.shortId = shortId;
		s.title = title;
		s.owner = owner;
		s.updatedAt = updatedAt;
		s.createdAt = createdAt;
		s.width = width;
		s.height = height;
		s.publicRead = publicRead;
		s.publicWrite = publicWrite;
		s.archived = archived;

		for (Widget wo:widgets) {
			Widget w = wo;
			if (deep) w = wo.clone();
			s.widgets.add(w);
			s.widgetMap.put(w.getId(), w);
		}

		return s;
	}
}
