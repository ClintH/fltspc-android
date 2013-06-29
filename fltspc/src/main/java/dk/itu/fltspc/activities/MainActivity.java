package dk.itu.fltspc.activities;

import dk.itu.fltspc.App;
import dk.itu.fltspc.Surface;
import dk.itu.fltspc.SurfaceDataManager;
import dk.itu.fltspc.util.IComplete;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import dk.itu.fltspc.R;

public class MainActivity extends Activity {
    private App app;
    private TextView txtStatus;
    private Button btnSignIn;
	private ListView lstSurfaces;

    private boolean runOnce = false;
    private SurfaceListAdapter surfaceListAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity", "OnResume");
        if (runOnce)
            CheckSignedInStatus(false);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.mnu_settings:
				startActivity(new Intent(MainActivity.this, SettingsActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

    private void CheckSignedInStatus(final boolean sendToSignInIntent) {
        final App appInstance = this.app;
        if (appInstance == null) return;

        new Thread(new Runnable() {
            public void run() {
                appInstance.getSession().whoAmI(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("MainActivity", "Whoami result: " + response.toString());
                        runOnce = true;
                        try {
                            if (response.getBoolean("isError")) {
                                ((TextView)findViewById(R.id.txtStatus)).setText("Error: " + response.getString("message"));
                            } else {
                                if (!response.has("id")) {
                                    if (sendToSignInIntent) {
                                        goSignin();
                                    } else {
                                        txtStatus.setText("Not signed in");
                                        btnSignIn.setText("Sign in");
                                    }
                                } else {
                                    txtStatus.setText("Signed in");
                                    btnSignIn.setText("Sign out");
	                                refreshSurfaces();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    private void goSignin() {
        Intent signIn = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(signIn);
    }

    private void refreshSurfaces() {
	    final MainActivity me = this;
        txtStatus.setText("Fetching surfaces");
	    try {
		    SurfaceDataManager.getAll(app.getSession(), app.getSurfaces(), new IComplete() {
		        @Override
		        public void onComplete(Object o) {
		            txtStatus.setText("Done!");
			        me.surfaceListAdapter = new SurfaceListAdapter(me, R.layout.lst_surface_row, app.getSurfaces().data.toArray(new Surface[0]));
			        lstSurfaces.setAdapter(surfaceListAdapter);

		        }
		    });
	    } catch (Exception e) {
		    txtStatus.setText(e.getMessage());
	    }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.app = (App)getApplication();
        setContentView(R.layout.main_fullscreen);
        txtStatus = ((TextView)findViewById(R.id.txtStatus));
        btnSignIn = (Button)findViewById(R.id.btnSignin);
		this.lstSurfaces = (ListView)findViewById(R.id.lstSurfaces);
	    this.surfaceListAdapter = new SurfaceListAdapter(this, R.layout.lst_surface_row, new Surface[0]);
		lstSurfaces.setAdapter(surfaceListAdapter);
        final App appInstance = this.app;

        findViewById(R.id.btnSignin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               goSignin();
            }
        });
        findViewById(R.id.btnSurfaces).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshSurfaces();
            }
        });

	    lstSurfaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
			    Surface o = (Surface)view.getTag();
			    Intent intent = new Intent(MainActivity.this, SurfaceViewActivity.class);
			    intent.putExtra("id", o.id);
			    startActivity(intent);
		    }
	    });
        CheckSignedInStatus(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private class SurfaceListAdapter extends ArrayAdapter<Surface> {
	    Context context;
	    int layoutResourceId;
        Surface[] items;

	    public SurfaceListAdapter(Context context, int layoutResourceId, Surface[] items) {
            super(context, layoutResourceId, items);
            this.items = items;
		    this.layoutResourceId = layoutResourceId;
		    this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
            if (row == null) {
                LayoutInflater vi = ((Activity)context).getLayoutInflater();
                row = vi.inflate(R.layout.lst_surface_row, parent, false);
            }
            Surface o = items[position];

            TextView tt = (TextView)row.findViewById(R.id.lstSurfaceTop);
            TextView bt = (TextView)row.findViewById(R.id.lstSurfaceBottom);
            ImageView img = (ImageView)row.findViewById(R.id.lstSurfaceIcon);
			row.setBackgroundColor(Color.parseColor(o.bgColour));
			row.setTag(o);

            if (tt != null) {
                tt.setText(o.title);
                tt.setTextColor(Color.WHITE);
            }
            if(bt != null){
                bt.setText(o.shortId);
	            bt.setTextColor(Color.WHITE);
            }
	        if (img != null) {
		        img.setBackgroundColor(Color.parseColor(o.bgColour));
	        }
            return row;
        }
    }
}
