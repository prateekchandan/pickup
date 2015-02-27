package cab.pickup.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import cab.pickup.MyActivity;
import cab.pickup.R;
import cab.pickup.util.MapUtil;

public class LocationSearchDialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "LocationSearchDialog";
    EditText searchField;
    ListView list;

    LatLng upperRight = new LatLng(19.289449, 73.174745); // Temporary jugaad... TODO change to user specific location
    LatLng lowerLeft = new LatLng(18.913122, 72.756578);

    int searchBarId;

    boolean running, doAgain;

    PlacesAdapter adapter;
    Address addrSelected;
    private AsyncTask<String, Integer, List<Address>> searchTask;

    public LocationSearchDialog(Context context, int id, Address a) {
        super(context);

        searchBarId=id;

        if(context instanceof Activity)
            setOwnerActivity((Activity)context);

        addrSelected=a;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.location_search_dialog);

        searchField = (EditText)findViewById(R.id.location_search_dialog_edittext);
        list = (ListView) findViewById(R.id.location_search_dialog_list);

        adapter=new PlacesAdapter(getContext());

        list.setAdapter(adapter);

        list.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addrSelected = (Address) view.getTag();

                updateSearch();
            }
        });

        findViewById(R.id.location_search_dialog_done).setOnClickListener(this);

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

        updateSearch();

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void updateSearch() {
        searchField.setText(MapUtil.stringFromAddress(addrSelected));
    }

    @Override
    public void onClick(View v) {
        ((MyActivity)getOwnerActivity()).returnLocationSearchValue(addrSelected, searchBarId);
        dismiss();
    }

    @Override
    public void onStop(){
        if (searchTask!=null) searchTask.cancel(true);
        super.onStop();
    }

    final class SearchTask extends AsyncTask<String, Integer,  List<Address>>{
        @Override
        protected void onPreExecute(){
        running = true;
    }

        @Override
        protected List<Address> doInBackground(String... params) {

            Geocoder gc = new Geocoder(getContext());

            List<Address> results=null;
            try {
                results = gc.getFromLocationName(params[0], 5, lowerLeft.latitude, lowerLeft.longitude, upperRight.latitude, upperRight.longitude);

            } catch (IOException e) {
                Log.e(TAG, "Server request timed out");
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<Address> arr){
            running = false;

            adapter.clear();

            if(arr!=null) {
                for (Address a : arr) {
                    adapter.add(a);
                }
            } else {
                Toast.makeText(getContext(),"No results!", Toast.LENGTH_SHORT).show();
            }

            adapter.notifyDataSetChanged();

            if(doAgain){
                //searchTask.execute(getText().toString());
                doAgain=false;
            }
        }
    }
}
