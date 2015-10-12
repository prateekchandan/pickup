package cab.pickup.ui.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cab.pickup.MyApplication;
import cab.pickup.R;
import cab.pickup.common.Constants;
import cab.pickup.common.api.Group;
import cab.pickup.common.api.Journey;
import cab.pickup.common.api.Location;
import cab.pickup.common.api.User;
import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.OnStringTaskCompletedListener;
import cab.pickup.common.server.OnTaskCompletedListener;
import cab.pickup.common.server.PostTask;
import cab.pickup.common.server.Result;
import cab.pickup.common.util.LocationTracker;
import cab.pickup.ui.widget.LocationSearchBar;
import cab.pickup.ui.widget.UserListAdapter;
import cab.pickup.common.util.IOUtil;
import cab.pickup.ui.widget.UserProfileView;

public class MainActivity extends MapsActivity implements   LocationSearchBar.OnAddressSelectedListener{

    private HashMap<Integer, Marker> markers = new HashMap<>(); //! Use to keep the google map marker for a location

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private AppCompatButton ride_now_btn,ride_later_btn,confirm_btn;

    // LOG TAG
    private static final String TAG = "Main";


    Location start, end;

    Journey journey;

    LocationSearchBar field_start, field_end;



    /**
     * onCreate - Called on creation of MainActivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        setupDrawer();

        field_start = ((LocationSearchBar)findViewById(R.id.field_start));
        field_end = ((LocationSearchBar)findViewById(R.id.field_end));

        field_start.setOnAddressSelectedListener(this);
        field_end.setOnAddressSelectedListener(this);
        setupUIClicks();

        ride_now_btn = (AppCompatButton) findViewById(R.id.ride_now);
        ride_later_btn = (AppCompatButton) findViewById(R.id.ride_after);
        confirm_btn = (AppCompatButton) findViewById(R.id.button_confirm);
        ride_now_btn.setSupportBackgroundTintList(new ColorStateList(new int[][]{new int[0]}, new int[]{getResources().getColor(R.color.complement_color)}));
        confirm_btn.setSupportBackgroundTintList(new ColorStateList(new int[][]{new int[0]}, new int[]{getResources().getColor(R.color.complement_color)}));
        ride_later_btn.setSupportBackgroundTintList(new ColorStateList(new int[][]{new int[0]}, new int[]{getResources().getColor(R.color.primary_color_80)}));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Sets Up the side Drawer
     */
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

    /**
     * Called when the activity is destroyed
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       //Removed the menu
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(prefs.contains("journey")){
            try {
                JSONObject journey_data = new JSONObject(prefs.getString("journey", ""));
                journey=new Journey(journey_data, MyApplication.getDB());

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("journeyError", e.getMessage());
                journey = new Journey();
            }

        } else {
            journey = new Journey();
        }
        try {
            Log.d("JourneyNow", journey.toString());
        }catch (Exception E){E.printStackTrace();}
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    /**
     * Sets the Start location to correct Location
     */
    public void setStartToCurrentLocation(){
        if(tracker==null)
            return;


        if(tracker.getLastKnownLocation()==null){
            Toast.makeText(MainActivity.this, "Unable to get current location. Please check your GPS", Toast.LENGTH_LONG).show();
            return;
        }
        String addressText="MyLocation";
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses  = geocoder.getFromLocation(tracker.getLatitude(),tracker.getLongitude(), 1);
            int numLines = addresses.get(0).getMaxAddressLineIndex();
            addressText="";
            for (int i = 0;i<numLines-1;i++) {
                if(i!=0)
                    addressText+=", ";
                addressText += addresses.get(0).getAddressLine(i);
            }
            ((TextView)findViewById(R.id.pickup_help_text)).setText(getString(R.string.current_location));
        }catch (Exception E){
            E.printStackTrace();
        }
        field_start.setAddress(new Location(tracker.getLatitude(), tracker.getLongitude(), addressText));
    }

    @Override
    public void onStop() {
        if(journey.id!=null) {
            SharedPreferences.Editor spe=prefs.edit();
            spe.putString("journey", journey.toString());
            spe.apply();
        }
        super.onStop();
    }

    /**
     * Setup UI Clicks for different items in Activity
     */
    public void setupUIClicks(){
        findViewById(R.id.field_end_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                field_end.performClick();
            }
        });
        findViewById(R.id.field_start_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                field_start.performClick();
            }
        });
    }


    /**
     * onAddressSelected : This function is called whenever a location is changed for the locationsearchbar
     *
     * @param bar : The Location bar on which address is changed
     * @param address : The new location of the bar
     */
    @Override
    public void onAddressSelected(final LocationSearchBar bar, Location address){
        // If map is not set , Then throw the error and return
        if(map==null){
            Toast.makeText(this,getResources().getString(R.string.map_error),Toast.LENGTH_SHORT).show();
            return;
        }

        // If the location bar is null , means some logical error occured
        if(bar==null){
            Log.e(TAG, "The bar is null");
            return;
        }

        // If address is null then clear the map markers
        if(address == null || bar.getAddress()==null){
            // Clear the marker from the map
            if(markers.containsKey(bar.getId()))
                markers.get(bar.getId()).remove();
            // Remove the marker from hashmap
            markers.remove(bar.getId());
            return;
        }

        int id=bar.getId();

        //Bring back the rides option if not present
        onAddressChangeClear();

        // If the address doesn't have latitude and longitude but only placeID
        if(!address.locUpdated){
            AsyncTask<String, Void, String> locFetch = new AsyncTask<String,Void,String>(){
                @Override
                protected String doInBackground(String... params){
                    String ret="", url=params[0];

                    AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
                    HttpGet httpget = new HttpGet(url);
                    try {
                        HttpResponse response = httpclient.execute(httpget);
                        ret= IOUtil.buildStringFromIS(response.getEntity().getContent());

                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    httpclient.close();
                    return ret;
                }

                @Override
                public void onPostExecute(String res){
                    try {
                        JSONObject loc = new JSONObject(res).getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
                        LatLng newPt = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

                        setMarkerOnMap(bar,newPt);

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            String locURL = "https://maps.googleapis.com/maps/api/place/details/json";
            locURL += "?key=AIzaSyChiVpPeOyYNFGq7_aR6-zpHnv6HsnwXQo"; // TODO Seperate constants like these
            locURL += "&placeid=" + address.placeId;
            locURL += "&components=country:in";

            locFetch.execute(locURL);
        } else {
            LatLng newPt = new LatLng(address.latitude, address.longitude);
            setMarkerOnMap(bar, newPt);
        }
    }

    /**
     * This function is called after onAddressChange to set Marker on Map
     * @param bar
     * @param newPt
     */
    private void setMarkerOnMap(final LocationSearchBar bar, LatLng newPt){
        if(!markers.containsKey(bar.getId())) {
            markers.put(bar.getId(), map.addMarker(new MarkerOptions().position(newPt)));
        } else {
            markers.get(bar.getId()).setPosition(newPt);
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(newPt, 17));
        bar.getAddress().setLatLong(newPt.latitude, newPt.longitude);

        if(displayPath()) {
            displayRidesButton();
        }

        int id = bar.getId();
        if(id==R.id.field_start)
            ((TextView)findViewById(R.id.pickup_help_text)).setText("");
        else if(id==R.id.field_end)
            ((TextView)findViewById(R.id.drop_help_text)).setText("");

        if(tracker==null || tracker.getLastKnownLocation()==null) {
            return;
        }
    }

    /**
     * Display path on the map from the start and end location
     */
    private boolean displayPath() {
        if(field_start.getAddress()==null ||field_end.getAddress()==null)
            return false;

        try {
            LatLng start = new LatLng(field_start.getAddress().latitude,field_start.getAddress().longitude);
            LatLng end =  new LatLng(field_end.getAddress().latitude,field_end.getAddress().longitude);

            String url="http://maps.googleapis.com/maps/api/directions/json?origin="
                    + start.latitude + "," + start.longitude + "&destination="
                    + end.latitude + "," + end.longitude;

            // MapDirectionTask is in MapsActivity , used to display the path between start and end location
            new MapDirectionsTask().execute(url);
            // If display path is success return true
            return true;
        } catch (NullPointerException E){
            E.printStackTrace();
        }
        return false;
    }

    /**
     * Display the Ride after and Ride now Button in the Main Activity
     */
    private void displayRidesButton(){
        findViewById(R.id.confirm_btn_group).setVisibility(View.GONE);
        LinearLayout rideBtnGroup = (LinearLayout)findViewById(R.id.ride_btn_group);
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        rideBtnGroup.setVisibility(View.VISIBLE);
        rideBtnGroup.startAnimation(slideUp);
    }

    /**
     * Display the Ride after and Ride now Button in the Main Activity
     */
    private void displayConfirmButton(){
        LinearLayout confirmBtnGroup = (LinearLayout)findViewById(R.id.confirm_btn_group);
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        confirmBtnGroup.setVisibility(View.VISIBLE);
        confirmBtnGroup.startAnimation(slideUp);
    }

    /**
     * Hide the Ride after and Ride now Button in the Main Activity
     */

    public void selectTime(View v) {

        start = field_start.getAddress();
        end = field_end.getAddress();

        if (start == null || end == null) {
            Toast.makeText(this, "Select both start and destination before continuing.", Toast.LENGTH_LONG).show();
            return;
        }

        Date now = new Date();
        now = new Date(now.getTime());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        Log.d(TAG, me.getJson());

        journey.user_id=me.id;
        journey.start=start;
        journey.end=end;
        journey.datetime=formatter.format(now);
        journey.del_time="30"; // TODO : Change this to 30 or 60
        journey.cab_preference="1";

        Log.d(TAG, "Journey time : " + journey.datetime);
        requestJourney();

    }

    /**
     * Register journey on server
     */
    private void requestJourney(){
        new PostTask(this,"Requesting for Journey"){

            @Override
            public List<NameValuePair> getPostData(String[] params, int i) {
                List<NameValuePair> nameValuePairs = new ArrayList<>(2);

                nameValuePairs.add(new BasicNameValuePair("user_id", me.id));
                nameValuePairs.add(new BasicNameValuePair("key", Constants.getKey()));

                nameValuePairs.add(new BasicNameValuePair("start_lat", journey.start.latitude+""));
                nameValuePairs.add(new BasicNameValuePair("start_long", journey.start.longitude+""));
                nameValuePairs.add(new BasicNameValuePair("end_lat", journey.end.latitude+""));
                nameValuePairs.add(new BasicNameValuePair("end_long", journey.end.longitude+""));

                nameValuePairs.add(new BasicNameValuePair("journey_time", journey.datetime));
                nameValuePairs.add(new BasicNameValuePair("margin_before", journey.del_time));
                nameValuePairs.add(new BasicNameValuePair("margin_after", journey.del_time));
                nameValuePairs.add(new BasicNameValuePair("preference","1"));

                nameValuePairs.add(new BasicNameValuePair("start_text",journey.start.longDescription));
                nameValuePairs.add(new BasicNameValuePair("end_text", journey.end.longDescription));

                return nameValuePairs;
            }

            @Override
            public void onPostExecute(Result ret){
                if(ret.statusCode==200){
                    journey.id = ret.data.optString("journey_id");
                }
                showConfirmButton(ret);
                super.onPostExecute(ret);
            }
        }.execute(Constants.getUrl("/journey_request"));
    }

    /**
     * Display of confirm button
     * @param ret : The result set from
     */
    private void showConfirmButton(Result ret){
        findViewById(R.id.ride_btn_group).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.confirm_text)).setText(ret.data.optString("message"));
        double distance = ret.data.optInt("distance")/1000.0;
        int fare = ret.data.optInt("estimated_fare");
        ((TextView)findViewById(R.id.confirm_distance_text)).setText(String.format(getString(R.string.confirm_distance_text),distance));
        ((TextView)findViewById(R.id.confirm_fare_text)).setText(String.format(getString(R.string.confirm_fare_text),fare));
        displayConfirmButton();
    }


    public void confirmRide(View v){
        GetTask confirmTask = new GetTask(this,getString(R.string.confirming_your_journey)){
            @Override
            public void onPostExecute(Result res) {
                super.onPostExecute(res);
                if(res.statusCode==200){
                    Log.d("group",res.data.toString());
                    try {
                        journey.group = new Group(res.data.getJSONObject("group"),MyApplication.getDB());
                        journey.group.group_id = String.valueOf(res.data.getInt("group_id"));
                    }catch (Exception E){
                        E.printStackTrace();
                    }

                    SharedPreferences.Editor spe = prefs.edit();

                    spe.putString("journey",journey.toString());
                    spe.apply();

                    Intent i = new Intent(MainActivity.this,RideActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };

        confirmTask.execute(Constants.getUrl("/confirm/" + journey.id + "?key=" + getKey()));
    }

    public void exchangeLocations(View v){
        Location temp_e = field_end.getAddress();
        Location temp_s = field_start.getAddress();
        field_start.setAddress(null);
        field_end.setAddress(temp_s);
        field_start.setAddress(temp_e);

    }



    public void clearTimers(){
      /*  ToggleButton time_30 = ((ToggleButton) findViewById(R.id.time_30));
        ToggleButton time_60 = ((ToggleButton) findViewById(R.id.time_60));

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        String time_now = sdf.format(cal.getTime());
        cal.add(Calendar.MINUTE,30);
        String time_to30 = sdf.format(cal.getTime());
        cal.add(Calendar.MINUTE,30);
        String time_to60 = sdf.format(cal.getTime());

        time_30.setTextOff(time_now+" - "+time_to30);
        time_30.setTextOn(time_now+" - "+time_to30);
        time_60.setTextOff(time_to30+" - "+time_to60);
        time_60.setTextOn(time_to30+" - "+time_to60);

        ((CompoundButton)findViewById(R.id.time_30)).setChecked(false);
        ((CompoundButton)findViewById(R.id.time_60)).setChecked(false);*/
    }

    public void onAddressChangeClear(){
        /*
        findViewById(R.id.fare_and_mates_card).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_confirm).setVisibility(View.INVISIBLE);
        clearFareAndMates();
        clearTimers();*/
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service){
        tracker = ((LocationTracker.LocalBinder) service).getService();
        tracker.connect(new OnStringTaskCompletedListener() {
            @Override
            public void onTaskCompleted(String res) {
                setStartToCurrentLocation();
            }
        });

    }
}
