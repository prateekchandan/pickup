package cab.pickup.ui.activity;

import android.graphics.Color;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cab.pickup.R;
import cab.pickup.api.Journey;
import cab.pickup.api.Location;
import cab.pickup.util.IOUtil;

public class MapsActivity extends MyActivity{
    GoogleMap map; // Might be null if Google Play services APK is not available.
    SupportMapFragment mapFrag;

    Polyline currPath;
    HashMap<String, Marker> user_pos=new HashMap<>();

    static final LatLng defaultLoc = new LatLng(19.0822508,72.8812041);

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

        if(map.getMyLocation() != null)
        {
            Location location = new Location(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude(),"");
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude, location.longitude), 17));
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 11));
        }
    }

    class MapDirectionsTask extends AsyncTask<String, Integer, String> {
        public final static String MODE_DRIVING = "driving";
        public final static String MODE_WALKING = "walking";

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
                JSONObject path = new JSONObject(json);

                addPath(Journey.getPath(path), Journey.getLatLngBounds(path), "0");
            } catch (JSONException e) {
                Log.e(TAG, "JSON error: "+e.getMessage());

                Toast.makeText(getApplicationContext(), "Error while loading path!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void addPath(ArrayList<LatLng> directions, LatLngBounds bnds, String distance){
        if(currPath!=null) currPath.remove();

        PolylineOptions rectLine = new PolylineOptions().width(4).color(Color.BLUE);

        for(int i = 0 ; i < directions.size() ; i++) {
            rectLine.add(directions.get(i));
        }

        currPath=map.addPolyline(rectLine);
        currPath.setColor(0xFF4433FF);
        currPath.setGeodesic(true);

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bnds, 300, 300, 10));

        //((TextView)findViewById(R.id.field_distance)).setText(distance);
    }

    public void updatePoint(String user_id, double lat, double lng){
        if(user_pos.containsKey(user_id)){
            user_pos.get(user_id).setPosition(new LatLng(lat, lng));
        } else {
            user_pos.put(user_id, map.addMarker(new MarkerOptions().position(new LatLng(lat,lng))));
        }
    }
}
