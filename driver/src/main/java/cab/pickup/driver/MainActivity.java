package cab.pickup.driver;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cab.pickup.driver.widget.LocationSearchBar;


public class MainActivity extends FragmentActivity implements LocationSearchBar.OnAddressSelectedListener {
    Driver me;
    Journey journey;
    LocationTracker tracker;
    LocationSearchBar field_start, field_end;
    HashMap<Integer, Marker> markers = new HashMap<Integer, Marker>();


    GoogleMap map; // Might be null if Google Play services APK is not available.
    SupportMapFragment mapFrag;

    Polyline currPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        field_start=(LocationSearchBar)findViewById(R.id.field_start);
        field_end=(LocationSearchBar)findViewById(R.id.field_end);


        field_start.setOnAddressSelectedListener(this);
        field_end.setOnAddressSelectedListener(this);

        tracker=new LocationTracker(this);
        tracker.connect();

        me=new Driver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

    }

    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            mapFrag = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            map = mapFrag.getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        map.setMyLocationEnabled(true);
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng((start.latitude + end.latitude) / 2, (start.longitude + end.longitude) / 2), 12));
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public LocationTracker getLocationTracker() {
        return tracker;
    }

    @Override
    public void onDestroy(){
        tracker.disconnect();
    }


    public void startNavigation(View v){
        Intent intentMap = new Intent(Intent.ACTION_VIEW,
                //Uri.parse("http://maps.google.com/maps?mode=driving&saddr="+field_start.getAddress().latlngString()
                        //+"&daddr=" + field_end.getAddress().latlngString()));
                Uri.parse("google.navigation:q="+field_end.getAddress().latlngString()));
        startActivity(intentMap);
    }

    @Override
    public void onAddressSelected(LocationSearchBar bar, Location address) {
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

    public void onLocationUpdate(Location location) {
        if(journey.navigation!=null) {
            for (Location endp : journey.navigation) {
                if(Math.abs(endp.latitude-location.latitude)<0.00002
                    && Math.abs(endp.longitude-location.longitude)<0.00002){
                    ((TextView)findViewById(R.id.nav_bar)).setText(endp.shortDescription);
                }
            }
        }
    }

    class MapDirectionsTask extends AsyncTask<String, Integer, String> {

        static final String TAG = "Directions";

        public MapDirectionsTask() {
        }

        @Override
        protected String doInBackground(String... params) {
            String json=null;
            String url = params[0];

            HttpResponse response;
            HttpGet request;
            AndroidHttpClient client = AndroidHttpClient.newInstance("somename");

            try {
                request = new HttpGet(url);
                response = client.execute(request);

                json = IOUtil.buildStringFromIS(response.getEntity().getContent());


            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            client.close();

            return json;
        }

        @Override
        public void onPostExecute(String json) {
            try {
                Log.d(TAG, json);
                journey = new Journey(new JSONObject(json));

                addPath(journey.getPath(), journey.getLatLngBounds());
            } catch (JSONException e) {
                Log.e(TAG, "JSON error: "+e.getMessage());

                Toast.makeText(getApplicationContext(), "Error while loading path!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void addPath(ArrayList<LatLng> directions, LatLngBounds bnds){
        if(currPath!=null) currPath.remove();

        PolylineOptions rectLine = new PolylineOptions().width(4).color(Color.BLUE);

        for(int i = 0 ; i < directions.size() ; i++) {
            rectLine.add(directions.get(i));
        }

        currPath=map.addPolyline(rectLine);

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bnds, 10));
    }
}
