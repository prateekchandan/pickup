package cab.pickup.ui.widget;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cab.pickup.R;
import cab.pickup.common.api.Location;
import cab.pickup.ui.activity.MyActivity;
import cab.pickup.common.util.IOUtil;

public class LocationPickerView extends LinearLayout implements LocationSearchBar.OnAddressSelectedListener, View.OnClickListener{
    private static final String TAG = "LocationPickerView";
    MyActivity context;
    public Location home, office;
    Marker home_marker, office_marker;

    LocationSearchBar searchBar;
    GoogleMap map;

    public LocationPickerView(Context context) {
        super(context);
        init((MyActivity)context);
    }

    public LocationPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init((MyActivity)context);
    }

    public LocationPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        init((MyActivity)context);
    }

    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) context.getSupportFragmentManager().findFragmentById(R.id.location_picker_map))
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

    private void init(MyActivity context){
        this.context = context;

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.widget_location_picker, this, true);

        searchBar=(LocationSearchBar)view.findViewById(R.id.location_picker_search_field);
        searchBar.setOnAddressSelectedListener(this);
        searchBar.setHomeOfficeEnabled(false);

        view.findViewById(R.id.location_picker_set_home).setOnClickListener(this);
        view.findViewById(R.id.location_picker_set_office).setOnClickListener(this);

        setUpMapIfNeeded();
    }

    @Override
    public void onAddressSelected(LocationSearchBar bar, Location address) {
        if(address == null) return;

        if(!address.locUpdated){
            AsyncTask<String,Void,String> locFetch = new AsyncTask<String,Void,String>(){
                @Override
                protected String doInBackground(String... params){
                    String ret="", url=params[0];

                    AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
                    HttpGet httpget = new HttpGet(url);
                    try {
                        HttpResponse response = httpclient.execute(httpget);

                        ret=IOUtil.buildStringFromIS(response.getEntity().getContent());

                    } catch (ClientProtocolException e) {
                        Log.e(TAG, e.getMessage());
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    httpclient.close();
                    return ret;
                }

                @Override
                public void onPostExecute(String res){
                    try {
                        JSONObject loc = new JSONObject(res).getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
                        LatLng newPt = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(newPt, 17));

                        Log.d(TAG,"Downloaded: " +newPt.latitude+","+newPt.longitude);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json");
            sb.append("?key=AIzaSyChiVpPeOyYNFGq7_aR6-zpHnv6HsnwXQo"); // TODO Seperate constants like these
            sb.append("&placeid=" + address.placeId);
            sb.append("&components=country:in");

            Log.d(TAG, sb.toString());

            locFetch.execute(sb.toString());
        } else {
            LatLng newPt = new LatLng(address.latitude, address.longitude);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(newPt, 17));
        }
    }

    @Override
    public void onClick(View v) {
        LatLng newPt = map.getCameraPosition().target;
        if(searchBar.getAddress()==null){
            Toast.makeText(context,"Select a Location in the search bar first",Toast.LENGTH_SHORT).show();
            return;
        }

        if(v.getId()==R.id.location_picker_set_home){
            home=new Location(newPt.latitude,newPt.longitude,searchBar.getAddress().shortDescription,searchBar.getAddress().longDescription);
            if(home_marker==null)
                home_marker= map.addMarker(new MarkerOptions().position(newPt));
            else
                home_marker.setPosition(newPt);
        } else {
            office=new Location(newPt.latitude,newPt.longitude,searchBar.getAddress().shortDescription,searchBar.getAddress().longDescription);
            if(office_marker==null)
                office_marker= map.addMarker(new MarkerOptions().position(newPt));
            else
                office_marker.setPosition(newPt);
        }

        searchBar.setAddress(null);
    }
}
