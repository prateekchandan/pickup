package cab.pickup.ui.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import cab.pickup.MyApplication;
import cab.pickup.R;
import cab.pickup.common.Constants;
import cab.pickup.common.api.Driver;
import cab.pickup.common.api.Event;
import cab.pickup.common.api.Journey;
import cab.pickup.common.api.User;
import cab.pickup.gcm.GcmIntentService;
import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.OnTaskCompletedListener;
import cab.pickup.common.server.Result;
import cab.pickup.ui.widget.DriverShortProfileView;
import cab.pickup.ui.widget.EventAdapter;
import cab.pickup.ui.widget.UserListAdapter;
import cab.pickup.ui.widget.UserProfileView;


public class RideActivity extends MapsActivity {
    Journey journey;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    ListView mEventList;
    EventAdapter mEventAdapter;
    private boolean isCancelled = false;

    BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GCM","update Reciever called");

            NotificationManager mNotificationManager = (NotificationManager)
                    RideActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(intent.getIntExtra("notif_id",0));

            loadEventData(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        setupDrawer();

        mEventAdapter = new EventAdapter(this, R.layout.event_view);
        mEventList = (ListView) findViewById(R.id.event_box);
        mEventList.setAdapter(mEventAdapter);

        //make map invisible
        findViewById(R.id.map).setVisibility(View.INVISIBLE);


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_USER_CANCELLED_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_USER_DROPPED_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_USER_PICKED_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_DRIVER_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_USER_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_DRIVER_ARRIVED_INTENT_TAG));

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCommonPath();
        refreshList();
        if(getIntent().hasExtra("action")){
            loadEventData(getIntent());
        } else {
            Log.d("GCM", "No action");
        }
    }

    protected void loadEventData(Intent intent){
        try {
            journey=new Journey(new JSONObject(prefs.getString("journey","")),MyApplication.getDB());

            refreshList();
            setupTextBoxes();
            updateCommonPath();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void refreshList(){

        Log.d("RidePage", "refreshList");

        mEventAdapter.clear();
        String eventString = prefs.getString("events","");
        if(eventString==null)
            return;
        try {
            JSONArray events = new JSONArray(eventString);
            for(int i=0; i<events.length(); i++){
                mEventAdapter.add(new Event(events.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        updateDriverCard();
        updateMatesCard();

        if(prefs.contains("ride_end_fare")){
            startActivity(new Intent(this, RateActivity.class));
        }

    }

    private void setupTextBoxes(){
        ((TextView)findViewById(R.id.field_start)).setText(journey.start.longDescription);
        ((TextView)findViewById(R.id.field_end)).setText(journey.end.longDescription);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setLenient(false);
            Date bookdate = sdf.parse(journey.datetime);
            ((TextView) findViewById(R.id.book_time)).setText("Booked Ride at " + new SimpleDateFormat("h:mm a").format(bookdate));
        }
        catch (Exception E){
            E.printStackTrace();
        }

        ((TextView)findViewById(R.id.jouney_distance)).setText(String.valueOf(journey.distance)+" km");
        updateMatesCard();
        updateDriverCard();
    }

    public void showUserDialog(View v){
        if(journey.group.mates.size()==0){
            return;
        }
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.user_list_dialog);
        ListView user_list_view=(ListView)dialog.findViewById(R.id.summary_user_list);
        UserListAdapter user_adapter=new UserListAdapter(this);
        user_list_view.setAdapter(user_adapter);
        for (User U : journey.group.mates){
            user_adapter.add(U.id);
        }
        ((TextView)dialog.findViewById(R.id.head_text)).setText(String.format(getString(R.string.mates_dialog_head), journey.group.mates.size()));
        ((ImageView)dialog.findViewById(R.id.icon_close)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showDriverDialog(View v){
        if(journey.group.driver==null)
            return;
        final Driver driver = journey.group.driver;
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.driver_detial_dialog);
        (dialog.findViewById(R.id.icon_close)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ((TextView)dialog.findViewById(R.id.driver_name)).setText(driver.driver_name);
        ((TextView)dialog.findViewById(R.id.car_model_name)).setText(driver.car_model);
        ((TextView)dialog.findViewById(R.id.car_number)).setText(driver.car_number);
        (dialog.findViewById(R.id.callBtn)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String uri = "tel:" + driver.phone.trim();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });
        (dialog.findViewById(R.id.smsBtn)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", driver.phone.trim(), null)));
            }
        });
        dialog.show();
    }

    protected void updateDriverCard(){
        LinearLayout user_1 = (LinearLayout)findViewById(R.id.driver_short_view);

        if(journey.group.driver==null){
            user_1.removeAllViews();
            TextView moreTxt = new TextView(this);
            moreTxt.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            moreTxt.setPadding(20, 20, 20, 20);
            moreTxt.setTextColor(Color.parseColor("#999999"));
            moreTxt.setText("Driver Not Allocated Yet");
            user_1.addView(moreTxt);
        }
        else{
            user_1.removeAllViews();
            DriverShortProfileView driverView = new DriverShortProfileView(this);
            driverView.setUser(journey.group.driver);
            user_1.addView(driverView);
        }
    }

    protected void updateMatesCard(){
        LinearLayout user_1 = (LinearLayout)findViewById(R.id.summary_user_one);
        if(journey.group.mates==null)
            return;
        if(journey.group.mates.size()>0){
            user_1.removeAllViews();
            UserProfileView userView = new UserProfileView(this);
            for (User U : journey.group.mates){
                userView.setUser(U);
                break;
            }
            TextView moreTxt = new TextView(this);
            moreTxt.setText("+"+String.valueOf(journey.group.mates.size()-1)+" more");
            LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
                    0,LinearLayout.LayoutParams.MATCH_PARENT, 0.3f);
            LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f);

            userView.setLayoutParams(param2);
            moreTxt.setLayoutParams(param1);
            moreTxt.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            moreTxt.setPadding(0, 15, 15, 15);
            moreTxt.setTextColor(getResources().getColor(R.color.theme_color_dark));
            user_1.setWeightSum(1.0f);
            user_1.addView(userView);
            user_1.addView(moreTxt);
        }
        else{
            user_1.removeAllViews();
            TextView moreTxt = new TextView(this);
            moreTxt.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            moreTxt.setPadding(20, 20, 20, 20);
            moreTxt.setTextColor(Color.parseColor("#999999"));
            moreTxt.setText("No Users with you");
            user_1.addView(moreTxt);
        }
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


    }

    @Override
    protected void onPause() {
        super.onPause();

        if(!isCancelled) {

            SharedPreferences.Editor spe = prefs.edit();
            spe.putString("journey", journey.toString());
            try {
                JSONArray events = new JSONArray();
                for (int i = 0; i < mEventAdapter.getCount(); i++) {
                    events.put(new JSONObject(mEventAdapter.getItem(i).toString()));
                }
                spe.putString("events", events.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            spe.apply();
        }
    }

    public void cancel(View v){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Are you sure to cancel this journey ?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {


                        new GetTask(RideActivity.this,"Cancelling Journey.. !!"){
                            @Override
                            public void onPostExecute(Result res) {
                                super.onPostExecute(res);
                                if(res.statusCode==200)
                                    Toast.makeText(RideActivity.this,res.statusMessage,Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RideActivity.this, MainActivity.class));
                                    isCancelled = true;
                                    SharedPreferences.Editor spe = prefs.edit();
                                    spe.remove("journey");
                                    spe.remove("events");
                                    spe.apply();
                                    finish();
                            }
                        }.execute(Constants.getUrl("/cancel_journey/" + journey.id + "?key=" + getKey()));


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
            button.setImageResource(R.drawable.ic_arrow_back_white_24dp);
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

    public void switchTabs(View v){
        ((ToggleButton)findViewById(R.id.tab_driver)).setChecked(false);
        ((ToggleButton)findViewById(R.id.tab_mates)).setChecked(false);
        ((ToggleButton)v).setChecked(true);
        if(v.getId()==R.id.tab_driver){
            findViewById(R.id.summary_user_one).setVisibility(View.GONE);
            findViewById(R.id.driver_short_view).setVisibility(View.VISIBLE);
        }
        else{
            findViewById(R.id.driver_short_view).setVisibility(View.GONE);
            findViewById(R.id.summary_user_one).setVisibility(View.VISIBLE);
        }
    }

    public void updateCommonPath(){
        Log.d("JourneyCheck", "Contains journey: " + prefs.contains("journey"));
        try {
            journey=new Journey(new JSONObject(prefs.getString("journey","")),MyApplication.getDB());
            setupTextBoxes();
            Log.d("PATHJSON", journey.group.json.toString());
            JSONObject path_waypts = new JSONObject(journey.group.json.getString("path_waypoints"));
            JSONArray start_pts = path_waypts.getJSONArray("startwaypoints");
            JSONArray end_pts = path_waypts.getJSONArray("endwaypoints");

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
                    + URLEncoder.encode(String.valueOf(start_lat),"UTF-8")
                    + ","
                    + URLEncoder.encode(String.valueOf(start_lng),"UTF-8")
                    + "&destination="
                    + URLEncoder.encode(String.valueOf(end_lat),"UTF-8")
                    + ","
                    + URLEncoder.encode(String.valueOf(end_lng),"UTF-8")
                    +"&waypoints="+
                    URLEncoder.encode(String.valueOf(waypoints.substring(0,waypoints.length()-1)), "UTF-8");
            Log.d("WAYPTT",url);
            new MapDirectionsTask().execute(url);
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (UnsupportedEncodingException E){
            E.printStackTrace();
        }
    }
}
