package dk.itu.fltspc.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import dk.itu.fltspc.R;

/**
 * Edited by Clint Heyer
 * Created by Wiktor Zdziechowski
 *
 */
public class SelectImageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		
		LinearLayout gallery = (LinearLayout) findViewById(R.id.gallery);
		List<String> imagesUri = getCameraImages(getApplicationContext());
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 5;
		
		for (final String url : imagesUri){
			ImageView picture = new ImageView(getApplicationContext());
			picture.setImageBitmap(BitmapFactory.decodeFile(url, options));
			gallery.addView(picture);
			
			picture.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					setResult(1, new Intent().putExtra("imageurl", url));
					finish();
				}
			});
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gallery, menu);
		return true;
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		finish();
	}
	
	public List<String> getCameraImages(Context context) {
		final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
		final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
		final String[] projection = { MediaStore.Images.Media.DATA };
		final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
		final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
		final Cursor cursor = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
		ArrayList<String> result = new ArrayList<String>(cursor.getCount());
		if (cursor.moveToFirst()) {
			final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			do {
				final String data = cursor.getString(dataColumn);
				result.add(data);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
	}

	public String getBucketId(String path) {
		return String.valueOf(path.toLowerCase().hashCode());
	}

	

}
