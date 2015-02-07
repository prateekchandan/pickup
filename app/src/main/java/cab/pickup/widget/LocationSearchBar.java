package cab.pickup.widget;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import java.io.IOException;
import java.util.List;

public class LocationSearchBar extends AutoCompleteTextView{
    private static final String TAG = "LocationSearchBar";
    PlacesAdapter adapter;
    SearchTask searchTask;

    boolean running, doAgain;


    public LocationSearchBar(Context context) {
        super(context);

        init(null);
    }

    public LocationSearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    private void init(AttributeSet attrs){
        adapter = new PlacesAdapter(getContext(), android.R.layout.simple_dropdown_item_1line);
        setAdapter(adapter);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!running){
                    searchTask=new SearchTask();
                    searchTask.execute(s.toString());
                } else
                    doAgain=true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    class SearchTask extends AsyncTask<String, Integer,  List<Address>>{
        @Override
        protected void onPreExecute(){
            running = true;
        }

        @Override
        protected List<Address> doInBackground(String... params) {

            Geocoder gc = new Geocoder(getContext());

            List<Address> results=null;
            try {
                results = gc.getFromLocationName(params[0], 5);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<Address> arr){
            running = false;

            adapter.clear();

            for(Address a : arr){
                adapter.add(a);
            }

            adapter.notifyDataSetChanged();

            if(doAgain){
                //searchTask.execute(getText().toString());
                doAgain=false;
            }
        }
    }
}
