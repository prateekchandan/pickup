package cab.pickup.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
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
import cab.pickup.ui.widget.LocationSearchBar;
import cab.pickup.ui.widget.UserListAdapter;
import cab.pickup.common.util.IOUtil;
import cab.pickup.ui.widget.UserProfileView;

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

    ListView user_list_view;
    UserListAdapter user_adapter;
    LocationSearchBar field_start, field_end;

    Dialog userDialog,fareDialog;


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
        mActivityTitle = getTitle().toString();

        setupDrawer();

        field_start = ((LocationSearchBar)findViewById(R.id.field_start));
        field_end = ((LocationSearchBar)findViewById(R.id.field_end));

        field_start.setOnAddressSelectedListener(this);
        field_end.setOnAddressSelectedListener(this);

        userDialog = new Dialog(this);
        userDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        userDialog.setContentView(R.layout.user_list_dialog);

        fareDialog = new Dialog(this);
        fareDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fareDialog.setContentView(R.layout.fare_dialog);

        user_list_view=(ListView)userDialog.findViewById(R.id.summary_user_list);
        //user_list_view.setEmptyView(findViewById(R.id.mates_empty_notif));

        user_adapter=new UserListAdapter(this);
        user_list_view.setAdapter(user_adapter);

        (userDialog.findViewById(R.id.icon_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDialog.hide();
            }
        });

        (fareDialog.findViewById(R.id.icon_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fareDialog.hide();
            }
        });

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

        if(map!=null)
            map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {

                    if(tracker==null)
                        return true;


                    if(tracker.getLastKnownLocation()==null){
                        Toast.makeText(MainActivity.this, "Unable to get current location. Please check your GPS", Toast.LENGTH_LONG).show();
                        return true;
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
                    field_start.setAddress(new Location(tracker.getLatitude(),tracker.getLongitude(),addressText));

                    return true;
                }
            });
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


    private void setJourney(String journey_json) throws JSONException{
        journey = new Journey(new JSONObject(journey_json),MyApplication.getDB());

        ((LocationSearchBar)findViewById(R.id.field_start)).setAddress(journey.start);
        ((LocationSearchBar)findViewById(R.id.field_end)).setAddress(journey.end);

        displayPath();
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

                        if(displayPath())
                            findViewById(R.id.time_picker_card).setVisibility(View.VISIBLE);

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

            if(displayPath())
                findViewById(R.id.time_picker_card).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapLoaded() {
        field_start.setAddress(journey.start);
        field_end.setAddress(journey.end);

        if(displayPath())
            findViewById(R.id.time_picker_card).setVisibility(View.VISIBLE);
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
                    ((TextView)fareDialog.findViewById(R.id.distance_fare)).setText(text);
                    text = String.format(getString(R.string.final_fare_text), distance_fare+35,s);
                    ((TextView)findViewById(R.id.main_fare_text)).setText(Html.fromHtml(text));

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
        user_1.addView(moreTxt);
    }

    public void switchTabs(View v){
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
        }
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
                            Log.d("bestmatch","here");
                            JSONObject usersJson = res.data.getJSONObject("best_match");
                            if(!usersJson.toString().equals("{}")){

                                JSONArray users = usersJson.getJSONArray("user_ids");

                                for (int i = 0; i < users.length(); i++) {
                                    user_adapter.add(users.getString(i));
                                    Log.d("bestmatch", users.getString(i));
                                }
                            }

                            updateMatesCard();

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
        user_adapter.clear();
        findViewById(R.id.summary_fare).setVisibility(View.GONE);
        findViewById(R.id.summary_user_one).setVisibility(View.VISIBLE);
        ((ToggleButton)findViewById(R.id.tab_fares)).setChecked(false);
        ((ToggleButton)findViewById(R.id.tab_mates)).setChecked(true);
    }

    public void clearTimers(){
        ToggleButton time_30 = ((ToggleButton) findViewById(R.id.time_30));
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
        ((CompoundButton)findViewById(R.id.time_60)).setChecked(false);
    }

    public void onAddressChangeClear(){

        findViewById(R.id.fare_and_mates_card).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_confirm).setVisibility(View.INVISIBLE);
        clearFareAndMates();
        clearTimers();
    }

    public void showUserDialog(View V){
        if(user_adapter.getCount()<=0)
            return;
        userDialog.show();
    }

    protected void updateMatesCard(){

        ((TextView)userDialog.findViewById(R.id.head_text)).setText(String.format(getString(R.string.mates_dialog_head), user_adapter.getCount()));

        LinearLayout user_1 = (LinearLayout)findViewById(R.id.summary_user_one);
        if(user_adapter.getCount()>0){
            user_1.removeAllViews();
            UserProfileView userView = new UserProfileView(this);
            userView.setUserId(user_adapter.getItem(0));
            TextView moreTxt = new TextView(this);
            moreTxt.setText("+"+String.valueOf(user_adapter.getCount()-1)+" more");
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
            moreTxt.setText(getString(R.string.no_users_with_you));
            user_1.addView(moreTxt);
        }
    }

    public void showFareDialog(View V){
        if(!show_fare_card)
            return;
        int fare = 35 + (int)(journey.distance*6);
        ((TextView)fareDialog.findViewById(R.id.fare_final)).setText(String.valueOf(fare));
        fareDialog.show();
    }
}
