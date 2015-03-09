package cab.pickup;

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

import java.util.HashMap;

import cab.pickup.util.Journey;
import cab.pickup.widget.LocationSearchBar;

public class MainActivity extends MapsActivity {
    HashMap<Integer, Marker> markers = new HashMap<Integer, Marker>();

    FrameLayout container;

    private static final String TAG = "Main";

    Address start, end;
    private static final int MODE_MAP=0, MODE_DETAILS=1;

    private int mode=MODE_MAP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpMapIfNeeded();

        container = (FrameLayout) findViewById(R.id.container);

        Intent i = new Intent();
        i.setClass(this,LoginActivity.class);
        startActivityForResult(i, 1);
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
    public void onActivityResult(int req, int res, Intent data){
        super.onActivityResult(req,res,data);

        Log.d(TAG, "onActivityResult");
        me.id=prefs.getString("user_id",null);
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
    public void returnLocationSearchValue(Address address, int id){
        super.returnLocationSearchValue(address,id);

        if(address == null) return;

        LatLng newPt = new LatLng(address.getLatitude(), address.getLongitude());

        if(!markers.containsKey(id)) {
            markers.put(id, map.addMarker(new MarkerOptions().position(newPt)));
        } else {
            markers.get(id).setPosition(newPt);
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

        Journey new_journey=new Journey(me, start, end, currentDate, pm_time, "1");

        new_journey.addToServer(this);
    }

    public void showRides(View v){
        startActivity(new Intent(this, RideActivity.class));
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
