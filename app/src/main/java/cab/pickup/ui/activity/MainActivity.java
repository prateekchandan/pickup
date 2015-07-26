package cab.pickup.ui.activity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cab.pickup.R;
import cab.pickup.api.Journey;
import cab.pickup.api.Location;
import cab.pickup.api.User;
import cab.pickup.gcm.GcmIntentService;
import cab.pickup.server.GetTask;
import cab.pickup.server.OnTaskCompletedListener;
import cab.pickup.server.Result;
import cab.pickup.ui.widget.LocationSearchBar;
import cab.pickup.ui.widget.UserListAdapter;
import cab.pickup.util.IOUtil;

public class MainActivity extends MapsActivity implements   LocationSearchBar.OnAddressSelectedListener,
                                                            OnTaskCompletedListener {
    HashMap<Integer, Marker> markers = new HashMap<>();

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;


    private static final String TAG = "Main";

    private static final int PAGE_MAIN =1;
    private static final int PAGE_SUMMARY =2;

    int page=PAGE_MAIN;

    Location start, end;

    Journey journey;

    RadioGroup timeOption;

    ListView user_list_view;
    UserListAdapter user_adapter;
    BroadcastReceiver mUpdateReceiver;
    LocationSearchBar field_start, field_end;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        setupDrawer();

        field_start = ((LocationSearchBar)findViewById(R.id.field_start));
        field_end = ((LocationSearchBar)findViewById(R.id.field_end));

        field_start.setOnAddressSelectedListener(this);
        field_end.setOnAddressSelectedListener(this);

        user_list_view=(ListView)findViewById(R.id.summary_user_list);
        user_adapter=new UserListAdapter(this);
        user_list_view.setAdapter(user_adapter);

        mUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NotificationManager mNotificationManager = (NotificationManager)
                        MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.cancel(intent.getIntExtra("notif_id",0));

                if(intent.getAction().equals(GcmIntentService.JOURNEY_ADD_USER_INTENT_TAG)){
                    Toast.makeText(MainActivity.this, "User added : "+intent.getStringExtra("id"), Toast.LENGTH_LONG).show();

                    user_adapter.add(intent.getStringExtra("id"));

                    user_adapter.notifyDataSetChanged();
                } else if(intent.getAction().equals(GcmIntentService.JOURNEY_ADD_DRIVER_INTENT_TAG)){
                    Toast.makeText(MainActivity.this, "Driver added : "+intent.getStringExtra("id"), Toast.LENGTH_LONG).show();

                    //((TextView)findViewById(R.id.summary_driver)).setText(intent.getStringExtra("id"));
                }
            }
        };

        //registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_DRIVER_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_USER_INTENT_TAG));

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
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mUpdateReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if(id== R.id.action_profile){
            startActivity(new Intent(this,ProfileActivity.class));
            return true;
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(tracker!=null)
            tracker.connect();

        if(prefs.contains("journey")){
            try {
                JSONObject journey_data = new JSONObject(prefs.getString("journey", ""));
                journey=new Journey(journey_data);

                field_start.setAddress(journey.start);
                field_end.setAddress(journey.end);

                /*if(journey.del_time.equals("30"))
                    timeOption.check(R.id.time_30);
                else
                    timeOption.check(R.id.time_60);*/
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("journeyError", e.getMessage());
                journey = new Journey();
            }

            page=PAGE_SUMMARY;
        } else {
            journey = new Journey();
        }

        //loadPage(page);
    }

    @Override
    public void onStop() {
        if(journey.id!=null) prefs.edit().putString("journey", journey.toString()).apply();
        super.onStop();
    }


    private void setJourney(String journey_json) throws JSONException{
        journey = new Journey(new JSONObject(journey_json));

        ((LocationSearchBar)findViewById(R.id.field_start)).setAddress(journey.start);
        ((LocationSearchBar)findViewById(R.id.field_end)).setAddress(journey.end);

        displayPath();
    }

    @Override
    public void onAddressSelected(final LocationSearchBar bar, Location address){
        if(address == null) return;

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
                        displayPath();
                        bar.getAddress().setLatLong(loc.getDouble("lat"), loc.getDouble("lng"));

                        if(markers.size()>=2) {
                            findViewById(R.id.time_picker_card).setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
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
            displayPath();
        }
    }



    private void displayPath() {
        try {
            LatLng start = markers.get(R.id.field_start).getPosition();
            LatLng end = markers.get(R.id.field_end).getPosition();

            String url="http://maps.googleapis.com/maps/api/directions/json?origin="
                    + start.latitude + "," + start.longitude + "&destination="
                    + end.latitude + "," + end.longitude;
            new MapDirectionsTask().execute(url);
        } catch (NullPointerException E){
            E.printStackTrace();
        }
    }

/*    public void loadPage(int page){
        if(page==PAGE_MAIN){
            findViewById(R.id.location_select).setVisibility(View.VISIBLE);
            findViewById(R.id.time_select).setVisibility(View.VISIBLE);

            findViewById(R.id.order_summary).setVisibility(View.GONE);

            field_start.setAddress(me.home);
            field_end.setAddress(me.office);
        } else {
            findViewById(R.id.location_select).setVisibility(View.GONE);
            findViewById(R.id.time_select).setVisibility(View.GONE);

            findViewById(R.id.order_summary).setVisibility(View.VISIBLE);

            user_adapter.addAll(journey.mates_id);

            displayPath();
        }
    }
*/
    public void selectTime(View v) {

        if(!((CompoundButton)v).isChecked()){
            ((CompoundButton)v).setChecked(true);
        }

        v.setSelected(true);
        start = field_start.getAddress();
        end = field_end.getAddress();

        if (start == null || end == null) {
            Toast.makeText(this, "Select both start and destination before continuing.", Toast.LENGTH_LONG).show();
            return;
        }

        Date now = new Date();

        if(v.getId() == R.id.time_30) {
            now = new Date(now.getTime() + 30 * 60 * 1000);
            ((ToggleButton)findViewById(R.id.time_60)).setChecked(false);
        } else if(v.getId() == R.id.time_60) {
            now = new Date(now.getTime() + 60 * 60 * 1000);
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
    }

    public void switchTabs(View v){
        ((ToggleButton)findViewById(R.id.tab_fares)).setChecked(false);
        ((ToggleButton)findViewById(R.id.tab_mates)).setChecked(false);

        ((ToggleButton)v).setChecked(true);
    }

    @Override
    public void onTaskCompleted(Result res) {
        if(res.statusCode == 200) {
            findViewById(R.id.fare_and_mates_card).setVisibility(View.VISIBLE);
            findViewById(R.id.button_confirm).setVisibility(View.VISIBLE);

            new GetTask(this){
                @Override
                public void onPostExecute(Result res) {
                    super.onPostExecute(res);
                    if(res.statusCode==200){
                        try {
                            JSONArray users = res.data.getJSONObject("best_match").getJSONArray("user_ids");

                            for(int i =0; i<users.length(); i++){
                                user_adapter.add(users.getString(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.execute(getUrl("/get_best_match/"+journey.id+"?key="+getKey()));
        }
    }
/*
    public void confirm(View v){
        GetTask confirmTask = new GetTask(this){
            @Override
            public void onPostExecute(Result res) {
                super.onPostExecute(res);
                if(res.statusCode==200){
                    String groupId = res.data.optString("group_id");

                    journey.group_id=groupId;

                    Intent i = new Intent(MainActivity.this,RideActivity.class);
                    i.putExtra("group_id",groupId);

                    startActivity(i);
                    finish();
                }
            }
        };

        confirmTask.execute(getUrl("/confirm/"+journey.id+"?key="+getKey()));

        Toast.makeText(this, "Confirming your Journey...",Toast.LENGTH_LONG);

    }

    public void edit(View v){
        loadPage(PAGE_MAIN);
    }*/
}
