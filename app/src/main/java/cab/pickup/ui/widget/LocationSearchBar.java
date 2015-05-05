package cab.pickup.ui.widget;


import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cab.pickup.R;
import cab.pickup.api.Location;
import cab.pickup.ui.activity.MyActivity;
import cab.pickup.util.LocationTracker;

public class LocationSearchBar extends TextView implements View.OnClickListener{

    private Location address;
    LocationTracker tracker;
    MyActivity context;
    OnAddressSelectedListener addrListener;

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

    @Override
    public void onClick(View v) {
        LocationSearchDialog dialog = new LocationSearchDialog(true);

        dialog.show();
    }

    public void setAddress(Location address) {
        this.address = address;

        setText(address.shortDescription);

        if(addrListener!=null) addrListener.onAddressSelected(this, address);
    }

    public Location getAddress() {
        return address;
    }

    class LocationSearchDialog extends Dialog implements View.OnClickListener{
        private static final String TAG = "LocationSearchDialog";
        EditText searchField;
        ListView list;

        LatLng upperRight = new LatLng(19.289449, 73.174745); // Temporary jugaad... TODO change to user specific location
        LatLng lowerLeft = new LatLng(18.913122, 72.756578);

        boolean running, doAgain,
                myLocationEnabled;

        PlacesAdapter adapter;
        private SearchTask searchTask;

        public LocationSearchDialog(boolean myLocationEnabled) {
            super(context);

            this.myLocationEnabled=myLocationEnabled;
        }

        @Override
        public void onCreate(Bundle savedInstanceState){
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            setContentView(R.layout.widget_location_search_dialog);

            searchField = (EditText)findViewById(R.id.location_search_dialog_edittext);
            list = (ListView) findViewById(R.id.location_search_dialog_list);

            adapter=new PlacesAdapter(context);

            list.setAdapter(adapter);

            list.setOnItemClickListener(new ListView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    setAddress((Location)view.getTag());
                    dismiss();
                }
            });

            findViewById(R.id.location_search_dialog_done).setOnClickListener(this);
            findViewById(R.id.location_search_dialog_myloc).setOnClickListener(this);

            if(myLocationEnabled)
                findViewById(R.id.location_search_dialog_myloc).setVisibility(View.VISIBLE);


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
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.location_search_dialog_myloc:
                    if(tracker.getLastKnownLocation()==null){
                        Toast.makeText(context, "Waiting for location...", Toast.LENGTH_LONG).show();
                        break;
                    }

                    setAddress(new Location(tracker.getLatitude(),tracker.getLongitude(),"My Location"));
                    dismiss();
                    break;
                case R.id.location_search_dialog_home:
                    setAddress(new Location(context.me.home.latitude,context.me.home.longitude,"Home"));
                    dismiss();
                    break;

                case R.id.location_search_dialog_office:
                    setAddress(new Location(context.me.office.latitude,context.me.office.longitude,"Office"));
                    dismiss();
                    break;
            }
        }

        @Override
        public void onStop(){
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

                Geocoder gc = new Geocoder(context);

                List<Location> results=new ArrayList<Location>();
                try {
                    List<Address> res=gc.getFromLocationName(params[0], 5, lowerLeft.latitude, lowerLeft.longitude, upperRight.latitude, upperRight.longitude);

                    for (Address a :res){
                        results.add(new Location(a.getLatitude(),a.getLongitude(),a.getAddressLine(0)));
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Server request timed out");
                }
                return results;
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

}
