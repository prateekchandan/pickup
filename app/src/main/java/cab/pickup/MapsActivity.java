package cab.pickup;

import android.graphics.Color;
import android.location.Address;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cab.pickup.util.IOUtil;
import cab.pickup.util.MapUtil;

public class MapsActivity extends MyActivity {
    String url;

    HashMap<Integer, Marker> markers = new HashMap<Integer, Marker>();

    GoogleMap map; // Might be null if Google Play services APK is not available.

    JSONObject gmapResult;

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void loadDir(){
        MapDirectionsTask getDir = new MapDirectionsTask();
        getDir.execute();
    }

    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
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

    class MapDirectionsTask extends AsyncTask<String, Integer, String> {
        public final static String MODE_DRIVING = "driving";
        public final static String MODE_WALKING = "walking";

        static final String TAG = "Directions";

        public MapDirectionsTask() {
        }

        @Override
        protected String doInBackground(String... params) {
            String json = "";

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
            gmapResult = MapUtil.getResult(json);

            Log.d(TAG, gmapResult.toString());

            addPath(MapUtil.getPath(gmapResult));
        }

    }

    private void addPath(ArrayList<LatLng> directions){
        PolylineOptions rectLine = new PolylineOptions().width(4).color(Color.BLUE);

        for(int i = 0 ; i < directions.size() ; i++) {
            rectLine.add(directions.get(i));
        }

        map.addPolyline(rectLine);

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(MapUtil.getLatLngBounds(gmapResult),10));
    }

    @Override
    public void returnLocationSearchValue(Address address, int id){
        super.returnLocationSearchValue(address,id);

        LatLng newPt = new LatLng(address.getLatitude(), address.getLongitude());

        if(!markers.containsKey(id)) {
            markers.put(id, map.addMarker(new MarkerOptions().position(newPt)));
        } else {
            markers.get(id).setPosition(newPt);
        }

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(newPt, 10));
    }
}
