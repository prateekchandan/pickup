package cab.pickup.ui.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cab.pickup.R;
import cab.pickup.api.Event;
import cab.pickup.api.Journey;
import cab.pickup.api.User;
import cab.pickup.gcm.GcmIntentService;
import cab.pickup.server.GetTask;
import cab.pickup.server.OnTaskCompletedListener;
import cab.pickup.server.Result;
import cab.pickup.ui.widget.EventAdapter;
import cab.pickup.ui.widget.EventView;


public class RideActivity extends MapsActivity {
    Journey journey;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    ListView mEventList;
    EventAdapter mEventAdapter;

    BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GCM","update Reciever called");

            NotificationManager mNotificationManager = (NotificationManager)
                    RideActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(intent.getIntExtra("notif_id",0));

            if(intent.getAction().equals(GcmIntentService.JOURNEY_ADD_USER_INTENT_TAG)){
                Toast.makeText(RideActivity.this, "User added : "+intent.getStringExtra("id"), Toast.LENGTH_LONG).show();

                journey.group.mates.add(0, new User(intent.getStringExtra("id"), new OnTaskCompletedListener() {
                    @Override
                    public void onTaskCompleted(Result res) {
                        mEventAdapter.add(new Event(Event.TYPE_USER_ADDED,journey.group.mates.get(0)));
                    }
                }));
            } else if(intent.getAction().equals(GcmIntentService.JOURNEY_ADD_DRIVER_INTENT_TAG)){
                Toast.makeText(RideActivity.this, "Driver added : "+intent.getStringExtra("id"), Toast.LENGTH_LONG).show();

                mEventAdapter.add(new Event(Event.TYPE_DRIVER_ADDED, intent.getStringExtra("id")));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setContentView(R.layout.activity_ride);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        setupDrawer();

        mEventAdapter = new EventAdapter(this, R.layout.event_view);
        mEventList = (ListView) findViewById(R.id.event_box);
        mEventList.setAdapter(mEventAdapter);

        if(getIntent().hasExtra("action")){
            String action = getIntent().getStringExtra("action");
            Log.d("GCM", "Action: " +action);
            if(action.equals(GcmIntentService.JOURNEY_ADD_USER_INTENT_TAG)){
                journey.group.mates.add(0, new User(getIntent().getStringExtra("id"), new OnTaskCompletedListener() {
                    @Override
                    public void onTaskCompleted(Result res) {
                        mEventAdapter.add(new Event(Event.TYPE_USER_ADDED,journey.group.mates.get(0)));
                    }
                }));
            } else if(action.equals(GcmIntentService.JOURNEY_ADD_DRIVER_INTENT_TAG)){
                mEventAdapter.add(new Event(Event.TYPE_DRIVER_ADDED, getIntent().getStringExtra("id")));
            }
        } else {
            Log.d("GCM", "No action");
        }

        //make map invisible
        findViewById(R.id.map).setVisibility(View.INVISIBLE);
    }


    private void setupTextBoxes(){
        ((TextView)findViewById(R.id.field_start)).setText(journey.start.longDescription);
        ((TextView)findViewById(R.id.field_end)).setText(journey.end.longDescription);
        String booktime = journey.datetime;
        ((TextView)findViewById(R.id.book_time)).setText(booktime);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();

        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_DRIVER_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_USER_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_DRIVER_ARRIVED_INTENT_TAG));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("JourneyCheck", "Contains journey: " + prefs.contains("journey"));
        try {
            journey=new Journey(new JSONObject(prefs.getString("journey","")));
            setupTextBoxes();

            JSONArray start_pts = journey.group.json.getJSONObject("path_waypoints").getJSONArray("startwaypoints");
            JSONArray end_pts = journey.group.json.getJSONObject("path_waypoints").getJSONArray("endwaypoints");

            double start_lat = start_pts.getJSONArray(0).getDouble(0);
            double start_lng = start_pts.getJSONArray(0).getDouble(1);

            double end_lat = end_pts.getJSONArray(end_pts.length()-1).getDouble(0);
            double end_lng = end_pts.getJSONArray(end_pts.length()-1).getDouble(1);


            String waypoints="";
            for(int i=1; i<start_pts.length(); i++){
                waypoints+=start_pts.getJSONArray(i).getDouble(0)+","+start_pts.getJSONArray(i).getDouble(1)+"|";
            }

            for(int i=0; i<end_pts.length()-1; i++){
                waypoints+=end_pts.getJSONArray(i).getDouble(0)+","+end_pts.getJSONArray(i).getDouble(1)+"|";
            }
            if(waypoints.length()==0)
                waypoints="|";

            Log.d("WAYPTT",waypoints);
            String url="http://maps.googleapis.com/maps/api/directions/json?origin="
                    + start_lat + "," + start_lng + "&destination="
                    + end_lat + "," + end_lng+"&waypoints="+waypoints.substring(0,waypoints.length()-1);
            Log.d("WAYPTT",url);
            new MapDirectionsTask().execute(url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        prefs.edit().putString("journey",journey.toString());
    }

    public void cancel(View v){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Are you sure to cancel this journey ?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        prefs.edit().remove("journey").apply();

                        new GetTask(RideActivity.this,"Cancelling Journey.. !!"){
                            @Override
                            public void onPostExecute(Result res) {
                                super.onPostExecute(res);
                                if(res.statusCode==200)
                                    Toast.makeText(RideActivity.this,res.statusMessage,Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RideActivity.this, MainActivity.class));
                                finish();
                            }
                        }.execute(getUrl("/cancel_journey/" + journey.id + "?key=" + getKey()));


                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mUpdateReceiver);
        super.onDestroy();
    }

    public void toggleMap(View V){
        View mapView = findViewById(R.id.map);
        View eventView = findViewById(R.id.eventView);
        FloatingActionButton button = ((FloatingActionButton)findViewById(R.id.fab));
        if(mapView.getVisibility()==View.INVISIBLE){
            mapView.setVisibility(View.VISIBLE);
            mapView.setAlpha(0.0f);

            mapView.animate()
                    .alpha(1.0f);
            eventView.setVisibility(View.INVISIBLE);
            button.setImageResource(R.drawable.b_arrow);
        }
        else{
            eventView.setVisibility(View.VISIBLE);
            eventView.setAlpha(0.0f);

            eventView.animate()
                    .alpha(1.0f);
            mapView.setVisibility(View.INVISIBLE);
            button.setImageResource(R.drawable.map);
        }
    }
}
