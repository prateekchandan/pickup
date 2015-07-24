package cab.pickup.ui.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
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
                                                            RadioGroup.OnCheckedChangeListener,
                                                            OnTaskCompletedListener {
    HashMap<Integer, Marker> markers = new HashMap<Integer, Marker>();

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
        setContentView(R.layout.activity_main);

        setUpMapIfNeeded();

        field_start = ((LocationSearchBar)findViewById(R.id.field_start));
        field_end = ((LocationSearchBar)findViewById(R.id.field_end));

        field_start.setOnAddressSelectedListener(this);
        field_end.setOnAddressSelectedListener(this);

        timeOption = ((RadioGroup)findViewById(R.id.option_time));
        timeOption.setOnCheckedChangeListener(this);

        user_list_view=(ListView)findViewById(R.id.summary_list_user);
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

                    ((TextView)findViewById(R.id.summary_driver)).setText(intent.getStringExtra("id"));
                }
            }
        };

        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_DRIVER_INTENT_TAG));
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ADD_USER_INTENT_TAG));

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent();
            i.setClass(this, SettingsActivity.class);
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        //if(tracker!=null)
        //    tracker.connect();

        if(prefs.contains("journey")){
            try {
                JSONObject journey_data = new JSONObject(prefs.getString("journey", ""));
                journey=new Journey(journey_data);

                field_start.setAddress(journey.start);
                field_end.setAddress(journey.end);

                if(journey.del_time.equals("30"))
                    timeOption.check(R.id.time_30);
                else
                    timeOption.check(R.id.time_60);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            page=PAGE_SUMMARY;
        } else {
            journey = new Journey();
        }

        loadPage(page);
    }

    @Override
    public void onStop() {
        //tracker.stopLocationUpdates();
        //tracker.disconnect();

        if(journey.id!=null) prefs.edit().putString("journey", journey.toString()).apply();
        super.onStop();
    }

    /*@Override
    public void onActivityResult(int req, int res, Intent data){
        super.onActivityResult(req, res, data);

        Log.d(TAG, "onActivityResult");
        if(res==RESULT_OK) {
            try {
                if (req == REQUEST_LOGIN) {
                    me = new User(new JSONObject(prefs.getString("user_json", "")), true);

                    ((LocationSearchBar)findViewById(R.id.field_start)).setAddress(new Location(me.home.latitude, me.home.longitude, "Home"));
                    ((LocationSearchBar)findViewById(R.id.field_end)).setAddress(new Location(me.office.latitude, me.office.longitude, "Office"));
                }
                else if (req == REQUEST_JOURNEY)
                    setJourney(data.getStringExtra("journey_json"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }*/

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

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(newPt, 17));
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

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(newPt, 17));
        }

        displayPath();
    }



    private void displayPath() {
        try {
            LatLng start = markers.get(R.id.field_start).getPosition();
            LatLng end = markers.get(R.id.field_end).getPosition();

            String url="http://maps.googleapis.com/maps/api/directions/json?origin="
                    + start.latitude + "," + start.longitude + "&destination="
                    + end.latitude + "," + end.longitude;
            new MapDirectionsTask().execute(url);
        } catch (NullPointerException e){
            return;
        }
    }

    public void loadPage(int page){
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

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        start = field_start.getAddress();
        end = field_end.getAddress();

        if (start == null || end == null) {
            Toast.makeText(this, "Select both start and destination before continuing.", Toast.LENGTH_LONG).show();
            return;
        }

        Date now = new Date();

        if(checkedId == R.id.time_30)
            now=new Date(now.getTime()+30*60*1000);
        else if(checkedId == R.id.time_60)
            now=new Date(now.getTime()+60*60*1000);

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

    @Override
    public void onTaskCompleted(Result res) {
        if(res.statusCode == 200) {
            loadPage(PAGE_SUMMARY);
        }
    }

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

        Toast.makeText(this,"Confirming your Journey...",Toast.LENGTH_LONG);

    }

    public void edit(View v){
        loadPage(PAGE_MAIN);
    }
}
