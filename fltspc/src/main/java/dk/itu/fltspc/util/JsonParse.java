package dk.itu.fltspc.util;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Clint Heyer on 25/06/13.
 */
public class JsonParse {
    private JsonParse() {

    }
    //http://www.java2s.com/Code/Java/Data-Type/ISO8601dateparsingutility.htm
    public static Date parseDateTime( String input ) throws java.text.ParseException {

        //NOTE: SimpleDateFormat uses GMT[-+]hh:mm for the TZ which breaks
        //things a bit.  Before we go on we have to repair this.
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssz" );

        //this is zero time so we need to add that TZ indicator for
        if ( input.endsWith( "Z" ) ) {
            input = input.substring( 0, input.length() - 1) + "GMT-00:00";
        } else {
            int inset = 6;

            String s0 = input.substring( 0, input.length() - inset );
            String s1 = input.substring( input.length() - inset, input.length() );

            input = s0 + "GMT" + s1;
        }

        return df.parse( input );

    }

    public static Date parseDateTime(String key, JSONObject o) throws ParseException {
       Date d = null;
        try {
            d = parseDateTime(o.getString(key));
        } catch (Exception e) {

        }
        return d;
    }
}
