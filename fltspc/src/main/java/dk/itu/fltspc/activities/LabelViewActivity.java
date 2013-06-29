package dk.itu.fltspc.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import dk.itu.fltspc.App;
import dk.itu.fltspc.R;
import dk.itu.fltspc.Surface;
import dk.itu.fltspc.Widget;
import dk.itu.fltspc.WidgetDataManager;

public class LabelViewActivity extends Activity {
	App app;
	Surface surface;
	Widget widgetClone;
	EditText txtContent;
	EditText txtTitle;

	LinearLayout layout;
	GestureDetector gestures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_view);

		gestures = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				Log.i("LabelViewActivity", "onDoubleTap");
				saveContent();
				return super.onDoubleTap(e);
			}
		});


	    app = (App)getApplication();
		txtContent = (EditText)findViewById(R.id.txtContent);
	    txtContent.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View view, MotionEvent motionEvent) {
			    Log.i("LVA", "onTouch text");
			    return gestures.onTouchEvent(motionEvent);
		    }

	    });

	    txtTitle = (EditText)findViewById(R.id.txtTitle);

		layout = (LinearLayout)findViewById(R.id.layout);


	    layout.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View view, MotionEvent motionEvent) {
			    Log.i("LVA", "onTouch layout");
			    return gestures.onTouchEvent(motionEvent);
		    }

	    });
	    ((Button)findViewById(R.id.btnCommit)).setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    saveContent();
		    }
	    });

	    Bundle extras = getIntent().getExtras();
	    if (extras != null) {
		    String id = null;
		    if (extras.containsKey("id")) {
			    id = extras.getString("id");
		    }
		    String surfaceId = extras.getString("surfaceId");
		    surface = app.getSurfaces().getByLongId(surfaceId);

		    if (id != null) {
			    widgetClone = surface.get(id).clone();
				txtContent.setText(widgetClone.getContent());
			    layout.setBackgroundColor(Color.parseColor(widgetClone.getBgColour()));
			    txtTitle.setText(widgetClone.getTitle());

		    }
	    }

	    if (widgetClone == null) {
		    widgetClone = new Widget(surface);
		    widgetClone.setWidth(5);
		    widgetClone.setHeight(5);
		    widgetClone.setLeft(2);
		    widgetClone.setTop(2);
		    widgetClone.setZorder(surface.getHighestZorder() + 1);
		    widgetClone.setType("label");
	    }
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!gestures.onTouchEvent(event))
			return super.onTouchEvent(event);
		return true;
	}

	void saveContent() {
		widgetClone.setTitle(txtTitle.getText().toString());
		final Activity me = this;
		widgetClone.setContent(txtContent.getText().toString());
		try {
			WidgetDataManager.update(widgetClone, true, new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
					try{
						if (response.getBoolean("isError")) {
							Toast send = Toast.makeText(me, "Could not save widget", 500);
							send.show();
						} else {
							Toast send = Toast.makeText(me, "Saved", 500);
							send.show();
						}
					} catch (JSONException ex) {
						Log.e("LabelViewActivity", "saveContent.onResponse", ex);
					}
				}
			});
		} catch (Exception ex) {
			Toast send = Toast.makeText(me, "Could not save", 500);
			send.show();
			Log.e("LabelViewActivity", "saveContent", ex);
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.label_view, menu);
        return true;
    }
    
}
