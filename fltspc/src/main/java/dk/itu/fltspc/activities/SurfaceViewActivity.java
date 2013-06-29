package dk.itu.fltspc.activities;

import dk.itu.fltspc.App;
import dk.itu.fltspc.Surface;
import dk.itu.fltspc.Widget;
import dk.itu.fltspc.WidgetDataManager;
import dk.itu.fltspc.util.IComplete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONObject;

import dk.itu.fltspc.R;

@SuppressWarnings("deprecation")
/**
 * Edited by Clint Heyer
 * Created by Wiktor Zdziechowski
 *
 */
public class SurfaceViewActivity extends Activity {
	int MAX_TOUCH_AREA = 10;
	long TOUCH_HOLD_TIME = 1000;
	int UPDATE_TIME_SPAN = 5000;
	long UPDATE_SINCE_TOUCH_NANO_SECS =10000000000L;
	AbsoluteLayout layout;
	WidgetView viewCurrent;
	WidgetView viewSelected;
	TextView txtSurfaceTitle;

	double scaleFactor = 6;

	boolean pause = false;
	boolean isTouching = false;
	long lastTouch =  0;

	Surface surface;
	App app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surface_view_surface);
		txtSurfaceTitle = (TextView)findViewById(R.id.txtSurfaceTitle);


		app = (App)getApplication();
		layout = (AbsoluteLayout) findViewById(R.id.absLayout);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String surfaceId = extras.getString("id");
			Log.i("SurfaceViewActivity", "Surface id: " + surfaceId);
			surface = app.getSurfaces().getByLongId(surfaceId);
			layout.setBackgroundColor(surface.getColour());
			txtSurfaceTitle.setText(surface.title);
		}


		ImageView button = (ImageView) findViewById(R.id.imageViewAddButton);
		button.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Intent intent = new Intent(getBaseContext(), ImageEditActivity.class);
					intent.putExtra("surfaceId", surface.id);
					startActivity(intent);
				}
				return true;
			}
		});

		layout.setOnTouchListener(layoutTouchListener);
		updateWidgetData.start();

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		scaleFactor = surface.width * surface.height / layout.getWidth() * layout.getHeight();
		if (surface.width > surface.height) {
			// Landscape
			scaleFactor = layout.getWidth() / surface.width;
		} else {
			// Portrait
			scaleFactor=  layout.getHeight() / surface.height;
		}
		Log.i("SurfaceViewActivity" ,"layout w: " + layout.getWidth() + " h: " + layout.getHeight());
		Log.i("SurfaceViewActivity", "Scale factor: " + scaleFactor);

	}

	private OnTouchListener	layoutTouchListener = new OnTouchListener() {
		float deltaX, deltaY, downX, downY;
		boolean isMoving = false;
		int tapCount = 0;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			lastTouch = System.nanoTime();
			if (viewCurrent == null) {
				if (viewSelected != null) {
					viewSelected.invalidate();
					viewSelected = null;
				}
				return true;
			}

			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if (isMoving) {
					LayoutParams lp = new LayoutParams(
							viewCurrent.getWidth(),
							viewCurrent.getHeight(),
							(int) event.getX() - (int) deltaX,
							(int) event.getY() - (int) deltaY);
					viewCurrent.bringToFront();
					viewCurrent.setLayoutParams(lp);
				}

				if (Math.abs(downX - event.getX()) > MAX_TOUCH_AREA || downY - event.getY() > MAX_TOUCH_AREA) {
					isMoving = true;
				} else {
					isMoving = false;
				}
			}

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				tapCount++;
				isTouching = true;

				deltaX = event.getX() - viewCurrent.getX();
				deltaY = event.getY() - viewCurrent.getY();
				downX = event.getX();
				downY = event.getY();

				if (viewCurrent != null) {
					Timer timer = new Timer();
					TimerTask tt = new TimerTask() {
						@Override
						public void run() {
							if (!isMoving && tapCount == 1 && viewCurrent != null) {
								viewCurrent.onLongPressAction();
							}
							tapCount = 0;
						}
					};
					timer.schedule(tt, TOUCH_HOLD_TIME);

					if (tapCount == 2) {
						viewCurrent.onDoubleTapAction();
					}
				}
			}

			if (event.getAction() == MotionEvent.ACTION_UP) {
				isTouching = false;
				viewCurrent.updatePosition();
				viewCurrent = null;
			}
			return true;
		}
	};

	private Thread updateWidgetData = new Thread(new Runnable() {
		@Override
		public void run() {

			while (true) {
				try {
					long interval = System.nanoTime() - lastTouch;
					//Log.i("SVA", "Interval: " + interval);
					if (interval > UPDATE_SINCE_TOUCH_NANO_SECS) {
						if (viewCurrent == null && !isTouching && !pause) {
							updateView();
						}
					}

					Thread.sleep(UPDATE_TIME_SPAN);
				} catch (InterruptedException e) {
					Log.e("SurfaceViewActivty", e.toString());
				}
			}
		}
	});

	public void addViewForWidget(Widget w) {
		WidgetView view = new WidgetView(layout.getContext(), w);
		view.setTag(w.getId());
		layout.addView(view);
		Log.i("SurfaceViewActivity", "addViewForWidget: " + w.getId());

		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				viewCurrent = (WidgetView)v;
				if (viewSelected != null) {
					viewSelected.invalidate();
					viewSelected = viewCurrent;
				} else {
					viewSelected = viewCurrent;
				}
				return false;
			}
		});
	}

	public void updateView() {
		try {
			WidgetDataManager.get(surface, new IComplete() {
				@Override
				public void onComplete(Object o) {
					if (o instanceof JSONObject) {
						// Dummy return value due to error or because we're in the process of changing
						// data
						JSONObject jo = (JSONObject)o;
						try {
							Log.i("SurfaceViewActivity","isError: " + jo.getBoolean("isError"));
							Log.i("SurfaceViewActivity", "message: " + jo.getString("message"));
						} catch (Exception ex) {
							Log.i("SurfaceViewActivity", "updateView.onComplete", ex);
						}
						return;
					}
					ArrayList<Widget> t = (ArrayList<Widget>)o;
					if (isTouching) return;

					Surface.MergeResult mr = surface.mergeUpdate(t);

					// Remove views for dead widgets
					for (Widget w : mr.deleted) {
						Log.i("SVA", "Deleted: " + w.getId());
						boolean removed = false;
						for (int i=0;i< layout.getChildCount(); i++) {
							View v = layout.getChildAt(i);
							if (v.getTag() != null && v.getTag().equals(w.getId())) {
								layout.removeView(v);
								removed = true;

							}
						}
						if (!removed) Log.i("SVA" ,"Warning view could not be found for deleted widget");
					}

					// Add new views
					for (Widget w : mr.created) {
						addViewForWidget(w);
					}

					// Update existing
					for (int i=0;i<mr.changed.size(); i++) {
						Widget w = mr.changed.get(i);
						HashMap<String, String> changeSet = mr.changeSets.get(i);
						Log.i("SVA", "Changed:" + w.getId());
						for (Map.Entry<String, String> e : changeSet.entrySet())
							Log.i("SVA", "Changed:  " + e.getKey() + " - " +  e.getValue());

						for (int x=0;x< layout.getChildCount(); x++) {
							View v = layout.getChildAt(x);
							if (v.getTag() != null && v.getTag().equals(w.getId())) {
								((WidgetView)v).notifyChanged(changeSet);
							}
						}
					}
				}
			});
		} catch (Exception e ) {
			Log.e("SurfaceViewActivity", e.toString());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.shared_screen_view, menu);
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		pause = true;
	}

	@Override
	public void onResume() {
		super.onResume();
		pause = false;
	}

	private class WidgetView extends View {
		Paint widgetPaint;
		Widget data;
		// Scaled values
		int width, height, left, top;

		public WidgetView(Context context, Widget data) {
			super(context);
			this.data = data;
			this.widgetPaint = new Paint();
			this.widgetPaint.setColor(data.getColour());
			updatePosFromModel();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (this == viewSelected) {
				canvas.drawRect(0, 0, width, height, widgetPaint);
				Paint stroke = new Paint();
				stroke.setStyle(Paint.Style.STROKE);
				stroke.setColor(Color.GRAY);
				stroke.setStrokeWidth(4);
				canvas.drawRect(0, 0, width - 1, height - 1, stroke);
			} else {
				canvas.drawRect(0, 0, width, height, widgetPaint);
			}
		}

		public void updatePosFromModel() {
			this.width = (int) Math.round(data.getWidth() * scaleFactor);
			this.height = (int) Math.round(data.getHeight() * scaleFactor);
			this.left = (int) Math.round(data.getLeft() * scaleFactor);
			this.top = (int) Math.round(data.getTop() * scaleFactor);

			LayoutParams lp = new LayoutParams(width, height, left, top);
			this.setLayoutParams(lp);
		}

		public void updatePosition() {
			data.setZorder(data.getSurface().getHighestZorder());
			data.setLeft(getLeft() / scaleFactor);
			data.setTop(getTop() / scaleFactor);
			final View me = this;

			try {
				WidgetDataManager.update(data, true, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							if (response.getBoolean("isError")) {
								Toast send = Toast.makeText(me.getContext(), "Could not save", 500);
								send.show();
							}
						} catch (Exception ex) {
							Log.e("SurfaceViewActivity", "updatePosition", ex);
						}
					}
				});
			} catch (Exception ex) {
				Log.e("SurfaceViewActivity", "updatePosition", ex);
			}
		}

		public void notifyChanged(HashMap<String,String> changes) {
			for (HashMap.Entry<String,String> e : changes.entrySet()) {
				String k = e.getKey();
				if (k.equals("bgColour")){
					this.widgetPaint.setColor(data.getColour());
					this.invalidate();
				} else if (k.equals("left") || k.equals("top") || k.equals("width") || k.equals("height")) {
					updatePosFromModel();
				}
			}
		}

		public void onLongPressAction() {
			SurfaceViewActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
				AlertDialog.Builder alert = new Builder(getContext());
				alert.setTitle("Delete");
				alert.setMessage("Do you really want to delete this widget?");
				alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						WidgetDataManager.delete(data, null);
					}

				});
				alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();

					}
				});

				AlertDialog alertDialog = alert.create();
				alertDialog.show();
				}
			});
		}

		public void onDoubleTapAction() {
			Intent intent = null;
			String type = data.getType();

			Log.i("SurfaceViewActivty", "Type: " + data.getType());
			Log.i("SurfaceViewActivty", "Content: " + data.getContent());

			if (type.equals("label")) {
				intent = new Intent(getBaseContext(), LabelViewActivity.class);
			} else if (type.startsWith("image/")) {
				intent = new Intent(getBaseContext(), ImageEditActivity.class);
			}

			if (intent != null) {
				intent.putExtra("surfaceId", surface.id);
				intent.putExtra("id", data.getId());
				startActivity(intent);
			}
//			if (type.equals("image")) {
//				intent = new Intent(getBaseContext(), ImageEditActivity.class);
//				intent.putExtra("url", data.content);
//				intent.putExtra("id", data.id);
//				intent.putExtra("type", "edit");
//				intent.putExtra("color", data.bgColour);
//				startActivity(intent);
//			} else if (type.equals("link")) {
//				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.content.substring(8, data.content.length() - 2)));
//				startActivity(intent);
//			}
		}

	}
}
