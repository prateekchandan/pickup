package cab.pickup.ui.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cab.pickup.R;
import cab.pickup.api.Location;
import cab.pickup.api.SingleJourney;
import cab.pickup.api.User;
import cab.pickup.gcm.GcmIntentService;
import cab.pickup.server.OnTaskCompletedListener;
import cab.pickup.server.PostTask;
import cab.pickup.server.Result;
import cab.pickup.ui.widget.LocationSearchBar;
import cab.pickup.ui.widget.UserListAdapter;

public class MainActivity extends MapsActivity implements   LocationSearchBar.OnAddressSelectedListener,
                                                            RadioGroup.OnCheckedChangeListener,
                                                            View.OnClickListener,
                                                            OnTaskCompletedListener {
    HashMap<Integer, Marker> markers = new HashMap<Integer, Marker>();

    private static final String TAG = "Main";

    private static final int PAGE_MAIN =1;
    private static final int PAGE_SUMMARY =2;

    int page=PAGE_MAIN;

    Location start, end;
    private static final int REQUEST_LOGIN=1, REQUEST_JOURNEY=2;

    SingleJourney journey;

    RadioGroup timeOption;

    ListView user_list_view;
    UserListAdapter user_adapter;
    BroadcastReceiver mUpdateReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpMapIfNeeded();

        journey = new SingleJourney();

        ((LocationSearchBar)findViewById(R.id.field_start)).setOnAddressSelectedListener(this);
        ((LocationSearchBar)findViewById(R.id.field_end)).setOnAddressSelectedListener(this);

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

        loadPage(PAGE_MAIN);

        Intent i = new Intent();
        i.setClass(this, LoginActivity.class);
        startActivityForResult(i, REQUEST_LOGIN);
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
        tracker.connect();
    }

    @Override
    public void onStop() {
        tracker.stopLocationUpdates();
        tracker.disconnect();
        super.onStop();
    }

    @Override
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
    }

    private void setJourney(String journey_json) throws JSONException{
        journey = new SingleJourney(new JSONObject(journey_json));

        ((LocationSearchBar)findViewById(R.id.field_start)).setAddress(journey.start);
        ((LocationSearchBar)findViewById(R.id.field_end)).setAddress(journey.end);

        displayPath();
    }

    @Override
    public void onAddressSelected(LocationSearchBar bar, Location address){

        if(address == null) return;

        LatLng newPt = new LatLng(address.latitude, address.longitude);

        if(!markers.containsKey(bar.getId())) {
            markers.put(bar.getId(), map.addMarker(new MarkerOptions().position(newPt)));
        } else {
            markers.get(bar.getId()).setPosition(newPt);
        }

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(newPt, 10));

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
        } else {
            findViewById(R.id.location_select).setVisibility(View.GONE);
            findViewById(R.id.time_select).setVisibility(View.GONE);

            findViewById(R.id.order_summary).setVisibility(View.VISIBLE);
        }
    }

    public void proceed(View v){
        if(page==PAGE_MAIN){
            findViewById(R.id.location_select).setVisibility(View.GONE);
            findViewById(R.id.time_select).setVisibility(View.GONE);

            findViewById(R.id.order_summary).setVisibility(View.VISIBLE);

            page=PAGE_SUMMARY;
        }

        Intent i = new Intent(this, RideActivity.class);
        startActivity(i);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        start = ((LocationSearchBar) findViewById(R.id.field_start)).getAddress();
        end = ((LocationSearchBar) findViewById(R.id.field_end)).getAddress();

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

        journey.user=me;
        journey.start=start;
        journey.end=end;
        journey.datetime=formatter.format(now);
        journey.del_time="30";
        journey.cab_preference="1";

        Log.d(TAG, "Journey time : " +journey.datetime);

        journey.addToServer(this, this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onTaskCompleted(Result res) {
        if(res.statusCode == 200) {
            loadPage(PAGE_SUMMARY);
        }
    }
}
