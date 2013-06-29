package dk.itu.fltspc.util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by mobclhe on 27/06/13.
 */
public class Event  {
	public boolean isError;
	public boolean preventPropagation;
	public String name;
	public JSONObject data;
	public Object source;


	public Event(String name, Object source) {
		this.name = name;
		this.source = source;
	}


}