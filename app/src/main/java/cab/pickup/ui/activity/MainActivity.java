package cab.pickup.ui.activity;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cab.pickup.R;
import cab.pickup.api.SingleJourney;
import cab.pickup.api.User;
import cab.pickup.ui.widget.LocationSearchBar;

public class MainActivity extends MapsActivity implements LocationSearchBar.OnAddressSelectedListener {
    HashMap<Integer, Marker> markers = new HashMap<Integer, Marker>();

    FrameLayout container;

    private static final String TAG = "Main";

    Address start, end;
    private static final int MODE_MAP=0, MODE_DETAILS=1,
                            REQUEST_LOGIN=1, REQUEST_JOURNEY=2;

    private int mode=MODE_MAP;

    SingleJourney journey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpMapIfNeeded();

        container = (FrameLayout) findViewById(R.id.container);

        journey = new SingleJourney();

        ((LocationSearchBar)findViewById(R.id.field_start)).setOnAddressSelectedListener(this);
        ((LocationSearchBar)findViewById(R.id.field_end)).setOnAddressSelectedListener(this);

        Intent i = new Intent();
        i.setClass(this,LoginActivity.class);
       startActivityForResult(i, REQUEST_LOGIN);
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

        //noinspection SimplifiableIfStatement
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
        super.onActivityResult(req,res,data);

        Log.d(TAG, "onActivityResult");
        if(res==RESULT_OK) {
            try {
                if (req == REQUEST_LOGIN)
                    me = new User(new JSONObject(prefs.getString("user_json", "")));
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

        TimePicker journey_time = (TimePicker)findViewById(R.id.journey_time);

        DatePicker date = ((DatePicker)findViewById(R.id.journey_date));

        //yyyy-mm-dd hh:mm:ss
        //0123456789012345678
        journey_time.setCurrentHour(Integer.valueOf(journey.datetime.substring(11,13)));
        journey_time.setCurrentMinute(Integer.valueOf(journey.datetime.substring(14,16)));

        date.updateDate(Integer.valueOf(journey.datetime.substring(0,4)),
                Integer.valueOf(journey.datetime.substring(5,7)),
                Integer.valueOf(journey.datetime.substring(8,10)));

        displayPath();
    }

    public void bookRide(View v){
        if(mode==MODE_MAP) {
            start = ((LocationSearchBar) findViewById(R.id.field_start)).getAddress();
            end = ((LocationSearchBar) findViewById(R.id.field_end)).getAddress();

            if (start == null || end == null) {
                Toast.makeText(this, "Select both start and destination before continuing.", Toast.LENGTH_LONG).show();
                return;
            }

            findViewById(R.id.book_details).setVisibility(View.VISIBLE);

            findViewById(R.id.map).setVisibility(View.GONE);

            mode=MODE_DETAILS;
        } else if(mode==MODE_DETAILS){
            addJourney();
        }
    }

    public void openChat(View v){
        Intent i = new Intent();
        i.setClass(this, ChatActivity.class);

        startActivity(i);
    }

    @Override
    public void onAddressSelected(LocationSearchBar bar, Address address){

        if(address == null) return;

        LatLng newPt = new LatLng(address.getLatitude(), address.getLongitude());

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

    public void addJourney(){

        TimePicker journey_time = (TimePicker)findViewById(R.id.journey_time);
        String time = (journey_time.getCurrentHour()>9?journey_time.getCurrentHour():"0"+journey_time.getCurrentHour())+":"+
                (journey_time.getCurrentMinute()>9?journey_time.getCurrentMinute():"0"+journey_time.getCurrentMinute())+":00";

        DatePicker date = ((DatePicker)findViewById(R.id.journey_date));

        int day=date.getDayOfMonth(), month=date.getMonth()+1;

        String currentDate = date.getYear()+"-"+
                (month>9?month:"0"+month)+"-"+
                (day>9?day:"0"+day)+" "+time;

                Log.d(TAG, currentDate);

        String pm_time = ((TextView)findViewById(R.id.pm_time)).getText().toString();

        Log.d(TAG, me.getJson());

        journey.user=me;
        journey.start=start;
        journey.end=end;
        journey.datetime=currentDate;
        journey.del_time=pm_time;
        journey.cab_preference="1";

        journey.addToServer(this);
    }

    public void showRides(View v){
        startActivityForResult(new Intent(this, RideActivity.class), REQUEST_JOURNEY);
    }

    @Override
    public void onBackPressed(){
        if(mode==MODE_DETAILS){
            findViewById(R.id.book_details).setVisibility(View.GONE);
            findViewById(R.id.map).setVisibility(View.VISIBLE);

            mode=MODE_MAP;
        } else {
            super.onBackPressed();
        }
    }
}
