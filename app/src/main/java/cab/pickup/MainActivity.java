package cab.pickup;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import cab.pickup.widget.LocationSearchBar;

public class MainActivity extends MapsActivity {
    HashMap<Integer, Marker> markers = new HashMap<Integer, Marker>();

    private static final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpMapIfNeeded();

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
        user_id=prefs.getString("user_id",null);
    }

    private TextView getListItem(Address a){
        TextView tv = new TextView(this);
        tv.setText(a.getFeatureName()+" ,"+a.getLocality()+" ,"+a.getLatitude()+","+a.getLongitude());
        tv.setTag(a);
        return tv;
    }

    public void bookRide(View v){
        Intent i =new Intent(this, BookActivity.class);

        Address start = ((LocationSearchBar)findViewById(R.id.field_start)).getAddress();
        Address end = ((LocationSearchBar)findViewById(R.id.field_end)).getAddress();

        if(start == null || end == null) {
            Toast.makeText(this, "Select both start and destination before continuing.", Toast.LENGTH_LONG).show();
            return;
        }


        i.putExtra("address_start", start);
        i.putExtra("address_end", end);

        startActivity(i);
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
}
