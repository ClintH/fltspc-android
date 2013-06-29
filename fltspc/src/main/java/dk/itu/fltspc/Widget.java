package dk.itu.fltspc;

import android.graphics.Color;
import android.util.Log;
import org.json.JSONObject;
import java.util.HashMap;

import dk.itu.fltspc.util.EventDispatch;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class Widget {
	String _bgColour;
	String _id, _type, _surfaceId;

	String _title;
	int _zorder;
	double _width, _height, _left, _top;

	String _content;
	JSONObject _parsedContent;

	public EventDispatch events;

	Surface surface;

	public Widget(Surface surface) {
		this.events = new EventDispatch(surface.events);
		this.surface = surface;
		this._surfaceId = surface.id;
	}

	public Surface getSurface() {
		return surface;
	}

	public HashMap<String, String> copyTo(Widget w) {
		if (!w._id.contentEquals(_id)) throw new IllegalArgumentException("Ids must match");

		boolean changes = false;
		HashMap<String, String> changeSet = new HashMap<String, String>();

		if (!w._bgColour.equalsIgnoreCase(_bgColour)) {
			changeSet.put("bgColour", w._bgColour);
			w.setBgColour(_bgColour);
			changes = true;
		}
		if (!w._surfaceId.equalsIgnoreCase(_surfaceId)) {
			changeSet.put("surfaceId", w._surfaceId);
			w.setSurfaceId(_surfaceId);
			changes = true;
		}
		if (!w._type.equalsIgnoreCase(_type)) {
			changeSet.put("type", w._type);
			w.setType(_type);
			changes = true;
		}
		if (!w._title.equalsIgnoreCase(_title)) {
			changeSet.put("title", w._title);
			w.setTitle(_title);
			changes = true;
		}
		if (!w._content.equalsIgnoreCase(_content)) {
			changeSet.put("content", w._content);
			w.setContent(_content);
			changes = true;
		}
		if (w._width != _width) {
			changeSet.put("width", Double.toString(w._width));
			w.setWidth(_width);
			changes = true;
		}
		if (w._height != _height) {
			changeSet.put("height", Double.toString(w._height));
			w.setHeight(_height);
			changes = true;
		}
		if (w._left != _left) {
			changeSet.put("left", Double.toString(w._left));
			w.setLeft(_left);
			changes = true;
		}
		if (w._top != _top) {
			changeSet.put("top", Double.toString(w._top));
			w.setTop(_top);
			changes = true;
		}
		if (changes)
			return changeSet;
		return null;

	}

	public Widget clone() {
		Widget w = new Widget(this.surface);
		w.setSurfaceId(getSurfaceId());
		w.setBgColour(getBgColour());
		w.setId(getId());
		w.setType(getType());
		w.setTitle(getTitle());
		w.setContent(getContent());
		w.setWidth(getWidth());
		w.setHeight(getHeight());
		w.setLeft(getLeft());
		w.setTop(getTop());
		w.setZorder(getZorder());
		return w;
	}

	public JSONObject getParsedContent() {
		if (_content != null) {
			if (_parsedContent == null) {
				try {
					_parsedContent = new JSONObject(_content);
				} catch (Exception e) {
					Log.i("Widget", "getParsedContent Unparsable: " + _content);
					//Log.i("Widget", "getParsedContent", e);
				}
			}
		}
		return _parsedContent;
	}

	public String getImageUrl() {
		JSONObject o = getParsedContent();
		String url = null;

		try {
			url =o.getString("url");
		} catch (Exception e) {

		}
		return url;
	}

	public void setContent(JSONObject o) {
		_parsedContent = o;
		_content = o.toString();
		events.trigger("change:content", this);
	}

	public void setContent(String c) {
		_content = c;
		_parsedContent = null;
		events.trigger("change:content", this);
	}

	public String getContent() {
		return _content;
	}

	public int getColour() {
		return  Color.parseColor(_bgColour);
	}

	public String getSurfaceId() {
		return _surfaceId;
	}

	public void setSurfaceId(String value) {
		if (value.equalsIgnoreCase(_surfaceId))return;
		_surfaceId = value;
		events.trigger("change:surfaceId", this);

	}
	public void setType(String value) {
		if (value.equalsIgnoreCase(_type)) return;
		_type = value;
		events.trigger("change:type", this);
	}

	public String getType() {
		return _type;
	}

	public void setTitle(String value) {
		if (value.equals(_title)) return;
		_title = value;
		events.trigger("change:title", this);
	}

	public String getTitle() {
		return _title;
	}


	public void setBgColour(String value) {
		if (value.equals(_bgColour)) return;
		_bgColour = value;
		events.trigger("change:bgColour", this);
	}

	public String getBgColour() {
		return _bgColour;
	}

	public double getTop() {
		return _top;
	}

	public void setTop(double value) {
		if (value == _top) return;
		_top = value;
		events.trigger("change:pos");
		events.trigger("change:top");
	}

	public double getLeft() {
		return _left;
	}

	public void setLeft(double value) {
		if (value == _left) return;
		_left = value;
		events.trigger("change:pos");
		events.trigger("change:left");
	}

	public double getHeight() {
		return _height;
	}

	public void setHeight(double value) {
		if (value == _height) return;
		_height = value;
		events.trigger("change:size");
		events.trigger("change:height");
	}


	public double getWidth() {
		return _width;
	}

	public void setWidth(double value) {
		if (value == _width) return;
		_width = value;
		events.trigger("change:size");
		events.trigger("change:width");
	}


	public int getZorder() {
		return _zorder;
	}

	public void setZorder(int value) {
		if (value <= 0) throw new IllegalArgumentException("value must be greater than 0");
		if (value == _zorder) return;
		_zorder = value;
		events.trigger("change:zorder", this);
	}

	public String getId() {
		return _id;
	}

	public void setId(String value) {
		if (value.equals(_id)) return;
		_id = value;
		events.trigger("change:id", this);
	}
}
