package cab.pickup.ui.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
import cab.pickup.common.server.Result;
import cab.pickup.common.util.LocationTracker;
import cab.pickup.ui.widget.LocationSearchBar;
import cab.pickup.ui.widget.UserListAdapter;
import cab.pickup.common.util.IOUtil;
import cab.pickup.ui.widget.UserProfileView;

public class MainActivity extends MapsActivity implements   LocationSearchBar.OnAddressSelectedListener,
                                                            OnTaskCompletedListener{
    HashMap<Integer, Marker> markers = new HashMap<>();

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;


    private static final String TAG = "Main";


    Location start, end;

    Journey journey;

    LocationSearchBar field_start, field_end;


    boolean show_fare_card=false;

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

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        //loadPage(page);
    }

    @Override
    public void onResume(){
        super.onResume();
    }


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




    @Override
    public void onAddressSelected(final LocationSearchBar bar, Location address){
        if(address == null || bar == null || bar.getAddress()==null) return;
        show_fare_card = false;
        if(map==null){
            Toast.makeText(this,getResources().getString(R.string.map_error),Toast.LENGTH_SHORT).show();
            return;
        }


        onAddressChangeClear();
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

                    } catch (ClientProtocolException e) {
                        Log.e(TAG, e.getMessage());
                    } catch (IOException e) {
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

                        if(!markers.containsKey(bar.getId())) {
                            markers.put(bar.getId(), map.addMarker(new MarkerOptions().position(newPt)));
                        } else {
                            markers.get(bar.getId()).setPosition(newPt);
                        }

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(newPt, 17));
                        bar.getAddress().setLatLong(loc.getDouble("lat"), loc.getDouble("lng"));

                        if(displayPath()) {
                            //findViewById(R.id.time_picker_card).setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json");
            sb.append("?key=AIzaSyChiVpPeOyYNFGq7_aR6-zpHnv6HsnwXQo"); // TODO Seperate constants like these
            sb.append("&placeid=" + address.placeId);
            sb.append("&components=country:in");

            locFetch.execute(sb.toString());
        } else {
            LatLng newPt = new LatLng(address.latitude, address.longitude);

            if(!markers.containsKey(bar.getId())) {
                markers.put(bar.getId(), map.addMarker(new MarkerOptions().position(newPt)));
            } else {
                markers.get(bar.getId()).setPosition(newPt);
            }

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(newPt, 17));

            if(displayPath()) {
                //findViewById(R.id.time_picker_card).setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean displayPath() {
        if(!markers.containsKey(R.id.field_end) || !markers.containsKey((R.id.field_start)))
            return false;

        try {
            LatLng start = markers.get(R.id.field_start).getPosition();
            LatLng end = markers.get(R.id.field_end).getPosition();

            String url="http://maps.googleapis.com/maps/api/directions/json?origin="
                    + start.latitude + "," + start.longitude + "&destination="
                    + end.latitude + "," + end.longitude;
            MapDirectionsTask task = new MapDirectionsTask(new OnStringTaskCompletedListener() {
                @Override
                public void onTaskCompleted(String s) {
                    journey.distance = Double.parseDouble(s);
                    show_fare_card = true;
                    int distance_fare = (int)(journey.distance*6);
                    String text =String.format(getString(R.string.distance_fare_text), distance_fare,s);
                    text = String.format(getString(R.string.final_fare_text), distance_fare+35,s);
                    //((TextView)findViewById(R.id.main_fare_text)).setText(Html.fromHtml(text));

                }


            });
            task.execute(url);

            return true;
        } catch (NullPointerException E){
            E.printStackTrace();
        }
        return false;
    }

    public void selectTime(View v) {
/*
        if(!((CompoundButton)v).isChecked()){
            ((CompoundButton)v).setChecked(true);
        }

        clearFareAndMates();

        v.setSelected(true);
        start = field_start.getAddress();
        end = field_end.getAddress();

        if (start == null || end == null) {
            Toast.makeText(this, "Select both start and destination before continuing.", Toast.LENGTH_LONG).show();
            return;
        }

        Date now = new Date();
        now = new Date(now.getTime());

        if(v.getId() == R.id.time_30) {
            ((ToggleButton)findViewById(R.id.time_60)).setChecked(false);
        } else if(v.getId() == R.id.time_60) {
            ((ToggleButton)findViewById(R.id.time_30)).setChecked(false);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        Log.d(TAG, me.getJson());

        journey.user_id=me.id;
        journey.start=start;
        journey.end=end;
        journey.datetime=formatter.format(now);
        journey.del_time="30";
        journey.cab_preference="1";

        Log.d(TAG, "Journey time : " +journey.datetime);

        journey.addToServer(this, this);

        // Setting searching for mates in card
        LinearLayout user_1 = (LinearLayout)findViewById(R.id.summary_user_one);
        user_1.removeAllViews();
        TextView moreTxt = new TextView(this);
        moreTxt.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        moreTxt.setPadding(20, 20, 20, 20);
        moreTxt.setTextColor(Color.parseColor("#999999"));
        moreTxt.setText(getString(R.string.searching_for_mates));
        user_1.addView(moreTxt);*/
    }

    public void switchTabs(View v){/*
        ((ToggleButton)findViewById(R.id.tab_fares)).setChecked(false);
        ((ToggleButton)findViewById(R.id.tab_mates)).setChecked(false);

        ((ToggleButton)v).setChecked(true);
        if(v.getId()==R.id.tab_fares){
            findViewById(R.id.summary_fare).setVisibility(View.VISIBLE);
            findViewById(R.id.summary_user_one).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.summary_fare).setVisibility(View.GONE);
            findViewById(R.id.summary_user_one).setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public void onTaskCompleted(Result res) {
        if(res.statusCode == 200) {
            //findViewById(R.id.fare_and_mates_card).setVisibility(View.VISIBLE);
            //findViewById(R.id.button_confirm).setVisibility(View.VISIBLE);

            new GetTask(this){
                @Override
                public void onPostExecute(Result res) {
                    super.onPostExecute(res);
                    if(res.statusCode==200){
                        try {
                            Log.d("bestmatch","here");
                            JSONObject usersJson = res.data.getJSONObject("best_match");
                            if(!usersJson.toString().equals("{}")){

                                JSONArray users = usersJson.getJSONArray("user_ids");


                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.execute(Constants.getUrl("/get_best_match/" + journey.id + "?key=" + getKey()));
        }else{
            clearTimers();
        }
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
        Location temp = field_end.getAddress();
        field_end.setAddress(field_start.getAddress());
        field_start.setAddress(temp);

    }

    //Clearing Function
    public void clearFareAndMates(){
        /*user_adapter.clear();
        findViewById(R.id.summary_fare).setVisibility(View.GONE);
        findViewById(R.id.summary_user_one).setVisibility(View.VISIBLE);
        ((ToggleButton)findViewById(R.id.tab_fares)).setChecked(false);
        ((ToggleButton)findViewById(R.id.tab_mates)).setChecked(true);*/
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

    public void showUserDialog(View V){
        /*if(user_adapter.getCount()<=0)
            return;
        userDialog.show();*/
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
