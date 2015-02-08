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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.List;

import cab.pickup.MyActivity;
import cab.pickup.R;
import cab.pickup.util.MapUtil;

public class LocationSearchDialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "LocationSearchDialog";
    EditText searchField;
    ListView list;

    int searchBarId;

    boolean running, doAgain;

    PlacesAdapter adapter;
    Address addrSelected;

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
                if (!running) new SearchTask().execute(s.toString());
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
