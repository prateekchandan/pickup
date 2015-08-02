package cab.pickup.ui.widget;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.actions.ReserveIntents;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cab.pickup.R;
import cab.pickup.api.Location;
import cab.pickup.ui.activity.MyActivity;
import cab.pickup.util.LocationTracker;


public class LocationSearchBar extends TextView implements View.OnClickListener{

    private final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private final String OUT_JSON = "/json";
    private final String API_KEY = "AIzaSyChiVpPeOyYNFGq7_aR6-zpHnv6HsnwXQo";
    private final String LOG_TAG = "PICKUP_LOCATION";

    private Location address;
    LocationTracker tracker;
    MyActivity context;
    OnAddressSelectedListener addrListener;

    boolean myLocationEnabled=true, homeOfficeEnabled=true;

    public LocationSearchBar(Context context) {
        super(context);

        init(context);
    }

    public LocationSearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context){
        setOnClickListener(this);

        if(context instanceof MyActivity) {
            this.context = (MyActivity) context;
            tracker = this.context.getLocationTracker();
        }
    }

    public void setOnAddressSelectedListener(OnAddressSelectedListener lstn){
        addrListener=lstn;
    }

    public void setMyLocationEnabled(boolean myLocationEnabled) {
        this.myLocationEnabled = myLocationEnabled;
    }

    public void setHomeOfficeEnabled(boolean homeOfficeEnabled) {
        this.homeOfficeEnabled = homeOfficeEnabled;
    }

    @Override
    public void onClick(View v) {
        LocationSearchDialog dialog = new LocationSearchDialog();

        dialog.show();
    }

    public void setAddress(Location address) {
        this.address = address;

        if(address!=null){
            setText(address.shortDescription);
            if(addrListener!=null) addrListener.onAddressSelected(this, address);
        } else {
            setText("");
        }
    }

    public Location getAddress() {
        return address;
    }

    class LocationSearchDialog extends Dialog implements View.OnClickListener{
        private static final String TAG = "LocationSearchDialog";
        EditText searchField;
        ListView list;

        boolean running, doAgain;

        PlacesAdapter adapter;
        private SearchTask searchTask;

        public LocationSearchDialog() {
            super(context);
        }

        @Override
        public void onCreate(Bundle savedInstanceState){
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            setContentView(R.layout.widget_location_search_dialog);

            searchField = (EditText)findViewById(R.id.location_search_dialog_edittext);
            if(searchField.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
            list = (ListView) findViewById(R.id.location_search_dialog_list);

            adapter=new PlacesAdapter(context);

            list.setAdapter(adapter);

            list.setOnItemClickListener(new ListView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    setAddress((Location) view.getTag());
                    dismiss();
                }
            });

            findViewById(R.id.location_search_dialog_myloc).setOnClickListener(this);

            if(myLocationEnabled) {
                findViewById(R.id.location_search_dialog_myloc).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.location_search_dialog_myloc).setVisibility(View.GONE);
            }


            searchField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!running) {
                        searchTask = new SearchTask();
                        searchTask.execute(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getWindow().getAttributes().x=0;
            getWindow().getAttributes().y=0;
        }

        @Override
        public void onClick(View v) {

            tracker = context.getLocationTracker();

            if(tracker==null)
                return;

            switch(v.getId()){
                case R.id.location_search_dialog_myloc:
                    if(tracker.getLastKnownLocation()==null){
                        Toast.makeText(context, "Unable to get current location. Please check your GPS", Toast.LENGTH_LONG).show();
                        break;
                    }

                    setAddress(new Location(tracker.getLatitude(),tracker.getLongitude(),"My Location"));
                    dismiss();
                    break;
                /*case R.id.location_search_dialog_home:
                    setAddress(new Location(context.me.home.latitude,context.me.home.longitude,"Home"));
                    dismiss();
                    break;

                case R.id.location_search_dialog_office:
                    setAddress(new Location(context.me.office.latitude,context.me.office.longitude,"Office"));
                    dismiss();
                    break;*/
            }
        }


        @Override
        public void onStop()                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        {
            if (searchTask!=null) searchTask.cancel(true);
            super.onStop();
        }

        final class SearchTask extends AsyncTask<String, Integer,  List<Location>>{
            @Override
            protected void onPreExecute(){
                running = true;
            }

            @Override
            protected List<Location> doInBackground(String... params) {

                return searchFromString(params[0]);
            }

            @Override
            protected void onPostExecute(List<Location> arr){
                running = false;

                adapter.clear();

                if(arr!=null) {
                    for (Location a : arr) {
                        adapter.add(a);
                    }
                } else {
                    Toast.makeText(context, "No results!", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();

                if(doAgain){
                    //searchTask.execute(getText().toString());
                    doAgain=false;
                }
            }
        }
    }

    public interface OnAddressSelectedListener{
        public void onAddressSelected(LocationSearchBar bar, Location a);
    }

    List<Location> searchFromString(String input){
        List<Location> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        tracker = context.getLocationTracker();
        String locString ="";
        if(tracker!=null){
            if(tracker.getLastKnownLocation()!=null){
                locString=String.valueOf(tracker.getLatitude())+","+String.valueOf(tracker.getLongitude());
            }
        }

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:in");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            if(locString!="")
            {
                sb.append("&location="+locString);
                sb.append("&radius=50");
            }
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(new Location(predsJsonArray.getJSONObject(i).getString("place_id"), predsJsonArray.getJSONObject(i).getString("description")));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;

    }

};
