package dk.itu.fltspc.activities;

import dk.itu.fltspc.App;
import dk.itu.fltspc.Surface;
import dk.itu.fltspc.Widget;
import dk.itu.fltspc.WidgetDataManager;
import dk.itu.fltspc.util.JsonUtil;
import dk.itu.fltspc.util.MyVolley;
import dk.itu.fltspc.util.SlideHolder;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.samsung.spensdk.*;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SPenHoverListener;
import com.samsung.spensdk.applistener.SPenTouchListener;

import org.json.JSONException;
import org.json.JSONObject;

import dk.itu.fltspc.R;

/**
 * Edited by Clint Heyer
 * Created by Wiktor Zdziechowski
 *
 */
public class ImageEditActivity extends Activity {
	RelativeLayout canvasContainer;
	SCanvasView canvasView;
	Intent intent;
	boolean isEdit;
	SlideHolder slideHolder;

	App app;
	Widget widgetClone;
	Surface surface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surface_edit);
		app = (App)getApplication();

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
			}
		}

		if (widgetClone == null) {
			// Set up new widget
			widgetClone = new Widget(surface);
			widgetClone.setWidth(34);
			widgetClone.setHeight(19);
			widgetClone.setLeft(2);
			widgetClone.setTop(2);
			widgetClone.setZorder(surface.getHighestZorder() + 1);
			widgetClone.setType("image/jpeg");
		}


		slideHolder = (SlideHolder) findViewById(R.id.SlideHolder);
		slideHolder.setDirection(SlideHolder.DIRECTION_RIGHT);
		slideHolder.setAllowInterceptTouch(false);

		LinearLayout color1 = (LinearLayout) findViewById(R.id.color1);
		LinearLayout color2 = (LinearLayout) findViewById(R.id.color2);
		LinearLayout color3 = (LinearLayout) findViewById(R.id.color3);
		LinearLayout color4 = (LinearLayout) findViewById(R.id.color4);
		LinearLayout color5 = (LinearLayout) findViewById(R.id.color5);
		LinearLayout color6 = (LinearLayout) findViewById(R.id.color6);
		LinearLayout color7 = (LinearLayout) findViewById(R.id.color7);
		LinearLayout color8 = (LinearLayout) findViewById(R.id.color8);
		final LinearLayout[] colors = new LinearLayout[] { color1, color2, color3, color4, color5, color6, color7, color8 };

		for (final LinearLayout color : colors) {
			color.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						for (LinearLayout ll : colors) {
							ImageView tick = (ImageView) ll.getChildAt(0);
							tick.setVisibility(ImageView.INVISIBLE);
						}

						ImageView tick = (ImageView) color.getChildAt(0);
						tick.setVisibility(ImageView.VISIBLE);
						String colour = (String)v.getTag();
						canvasView.setBGColor(Color.parseColor(colour));
						canvasView.setBackgroundColor(Color.parseColor(colour));

						widgetClone.setBgColour((String) v.getTag());
					}
					return true;
				}
			});
		}

		ImageButton galleryButton = (ImageButton) findViewById(R.id.galleryButton);
		galleryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			startActivityForResult(new Intent(getApplicationContext(), SelectImageActivity.class), 1);
			}
		});

		canvasContainer = (RelativeLayout) findViewById(R.id.canvas_container);
		canvasView = new SCanvasView(getBaseContext());
		canvasContainer.addView(canvasView);
		canvasView.createSettingView(canvasContainer, null);

		ActivityGestureDetector _agd = new ActivityGestureDetector(this);
		canvasView.setSPenTouchListener(_agd);

		canvasView.setSPenHoverListener(new SPenHoverListener() {

			@Override
			public void onHoverButtonUp(View arg0, MotionEvent arg1) {
				if (canvasView.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_PEN) {
					canvasView.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
				} else if (canvasView.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_ERASER) {
					canvasView.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
				}
			}

			@Override
			public void onHoverButtonDown(View arg0, MotionEvent arg1) {
			}
			@Override
			public boolean onHover(View arg0, MotionEvent arg1) {
				return false;
			}
		});

		canvasView.setSCanvasInitializeListener(new SCanvasInitializeListener() {

			@Override
			public void onInitialized() {
				//canvasView.setBGColor(Color.BLUE);
				canvasView.setBackgroundColor(Color.RED);
				String imageUrl = widgetClone.getImageUrl();
				if (imageUrl != null) {
					if (imageUrl.startsWith("/")) imageUrl = imageUrl.substring(1);
					loadImage(app.getBaseUrl() + imageUrl);
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (R.id.action_save):
			saveImageToGallery();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.canvas_space, menu);
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void toggleToolSettings() {
		switch (canvasView.getCanvasMode()) {
		case SCanvasConstants.SCANVAS_MODE_INPUT_PEN:
			canvasView.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN);
			break;
		case SCanvasConstants.SCANVAS_MODE_INPUT_ERASER:
			canvasView.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER);
		default:
			// No tool selected
			break;
		}
	}

	public void loadImage(String url) {
		if (url == null) throw new IllegalArgumentException("url is null");
		Log.i("ImageEditActivity", "Fetching: " + url);

		MyVolley.getImageLoader().get(url, new ImageLoader.ImageListener() {
			@Override
			public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
				Bitmap existing = canvasView.getBackgroundImage();
				canvasView.setBackgroundImage(response.getBitmap());
				existing.recycle();
			}

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i("ImageEditActivty", "loadImage:Could not load image: " + error.toString());

			}
		});
	}

	// Creates widget on server if we don't have an id for it.
	// At callback, widgetClone.id should have id, or there was a failure in server communication/creation
	public void ensureCreated(final Response.Listener<JSONObject> l) {
		if (widgetClone.getId() == null) {
			try {
				WidgetDataManager.create(widgetClone, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							if (response.getBoolean("isError")) {
								JsonUtil.handle(response, l);
								return;
							}
						} catch (Exception ex) {
							JsonUtil.handle(ex, l);
							return;
						}
						widgetClone.events.resetFired();

						// Merge data from server with our cached copy
						try {
							Widget fromServer = WidgetDataManager.fromData(response, surface);
							fromServer.copyTo(widgetClone);
						} catch (Exception e) {
							Log.e("ImageEditActivity", "ensureCreated", e);
						}
						JsonUtil.handle(response, l);
					}
				});
			} catch (Exception e) {
				Log.e("ImageEditActivity", "ensureCreated2", e);
			}
		} else {
			JsonUtil.handle(l);
		}
	}

	public void sendToSharedSpace() {
		if (widgetClone == null) throw new IllegalArgumentException("widgetClone is null");
		final Activity me = this;

		ensureCreated(new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					if (response.getBoolean("isError")) {
						Toast send = Toast.makeText(me, "Could not create widget", 500);
						send.show();
						return;
					}
				} catch (Exception ex) {
					Log.e("ImageEditActivity", "sendToSharedSpace", ex);
				}
				sendToSharedSpaceImpl();
			}
		});
	}

	void sendToSharedSpaceImpl() {
		final ImageEditActivity me = this;
		Toast send = Toast.makeText(this, "Sending", 500);
		send.show();

		final Bitmap bmp = canvasView.getCanvasBitmap(false);

		WidgetDataManager.upload(bmp, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
			try {
				if (response.getBoolean("isError")) {
					Toast send = Toast.makeText(me, response.getString("message"), 500);
					send.show();
				} else {
					Toast send = Toast.makeText(me, "Sent!", 500);
					send.show();
					JSONObject o = new JSONObject();
					final String urlPrefix = response.getString("url");
					o.put("url", urlPrefix);
					widgetClone.setContent(o);
					WidgetDataManager.update(widgetClone, true, new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							try {
								if (response.getBoolean("isError")) {
									Toast send = Toast.makeText(me, "Could not save widget", 500);
									send.show();
									return;
								}
								String url = urlPrefix;
								if (urlPrefix.startsWith("/")) url =app.getBaseUrl() + urlPrefix.substring(1);
								MyVolley.getImageLoader().getCache().putBitmap(url, bmp);
							} catch (Exception ex) {
								Log.e("ImageEditActivity", "sendToSharedSpaceImpl.upload", ex);
							}
						}
					});
				}

			} catch (JSONException e) {
				Log.e("ImageEditActivity", "sendToShareSpaceImpl.onResponse", e);
			}
			}
		});
	}

	public void goShowSharedSpace() {
		intent = new Intent(this, SurfaceViewActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		this.finish();
	}

	private void saveImageToGallery() {
		Bitmap bmCanvas = canvasView.getCanvasBitmap(false);
		MediaStore.Images.Media.insertImage(getContentResolver(), bmCanvas, "picture", "Made with canvas test");

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (intent.hasExtra("imageurl")) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			canvasView.setBackgroundImage(BitmapFactory.decodeFile(intent.getExtras().getString("imageurl"), options));
		}
	}
	

	class ActivityGestureDetector implements SPenTouchListener {
		static final String logTag = "ActivitySwipeDetector";
		private ImageEditActivity activity;
		static final int MIN_DISTANCE = 60;
		static final int TAP_MAX_DISTANCE = 5;
		private float downX, downY, upX, upY;

		public ActivityGestureDetector(ImageEditActivity activity) {
			this.activity = activity;
		}

		public void onTopToBottomSwipe() {
			goShowSharedSpace();
		}

		public void onBottomToTopSwipe() {
			sendToSharedSpace();
		}

		public void onRightToLeft() {
			slideHolder.open();
		}

		public void onLeftToRight() {
			slideHolder.close();
		}

		public void onTap() {
			toggleToolSettings();

		}

		@Override
		public void onTouchButtonDown(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTouchButtonUp(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onTouchFinger(View arg0, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				downX = event.getX();
				downY = event.getY();
				return true;
			}
			case MotionEvent.ACTION_UP: {
				upX = event.getX();
				upY = event.getY();

				float deltaX = downX - upX;
				float deltaY = downY - upY;

				if (Math.abs(deltaY) > MIN_DISTANCE) {
					if (deltaY < 0) {
						this.onTopToBottomSwipe();
						return true;
					}
					if (deltaY > 0) {
						this.onBottomToTopSwipe();
						return true;
					}
				} else if (Math.abs(deltaX) > MIN_DISTANCE) {
					if (deltaX > 0) {
						this.onRightToLeft();
					}
					if (deltaX < 0) {
						this.onLeftToRight();
					}
				} else if (Math.abs(deltaX) < TAP_MAX_DISTANCE && Math.abs(deltaY) < TAP_MAX_DISTANCE) {
					this.onTap();
				}
				return true;
			}
			}
			return false;
		}

		@Override
		public boolean onTouchPen(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onTouchPenEraser(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			return false;
		}
	}

}
