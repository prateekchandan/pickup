package cab.pickup;

import android.graphics.Color;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.util.ArrayList;

import cab.pickup.util.IOUtil;
import cab.pickup.util.MapUtil;

public class MapsActivity extends FragmentActivity {

    private GoogleMap map; // Might be null if Google Play services APK is not available.

    LatLng start, end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        start = getIntent().getParcelableExtra(getString(R.string.extra_start_coord));
        end = getIntent().getParcelableExtra(getString(R.string.extra_end_coord));

        setUpMapIfNeeded();

        MapDirectionsTask getDir = new MapDirectionsTask();
        getDir.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
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

        map.addMarker(new MarkerOptions().position(start).title("Start"));
        map.addMarker(new MarkerOptions().position(end).title("End"));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng((start.latitude + end.latitude) / 2, (start.longitude + end.longitude) / 2), 12));
    }

    class MapDirectionsTask extends AsyncTask<String, Integer, String> {
        public final static String MODE_DRIVING = "driving";
        public final static String MODE_WALKING = "walking";

        static final String TAG = "Directions";

        public MapDirectionsTask() {
        }

        @Override
        protected String doInBackground(String... params) {
            String url =
                    "http://maps.googleapis.com/maps/api/directions/json?origin="
                            + start.latitude + "," + start.longitude + "&destination="
                            + end.latitude + "," + end.longitude + "&sensor=false&mode=" + MODE_DRIVING;

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

            return json;
        }

        @Override
        public void onPostExecute(String json) {
            addPath(map, MapUtil.getPath(json));
        }

    }

    private void addPath(GoogleMap map, ArrayList<LatLng> directions){
        PolylineOptions rectLine = new PolylineOptions().width(4).color(Color.BLUE);

        for(int i = 0 ; i < directions.size() ; i++) {
            rectLine.add(directions.get(i));
        }

        map.addPolyline(rectLine);
    }
}
