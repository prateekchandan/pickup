package cab.pickup.ui.widget;

import android.content.Context;
import android.location.Address;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import cab.pickup.R;
import cab.pickup.ui.activity.MyActivity;

public class LocationPickerView extends LinearLayout implements LocationSearchBar.OnAddressSelectedListener, View.OnClickListener{
    MyActivity context;
    public LatLng home, office;
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
        view.findViewById(R.id.location_picker_set).setOnClickListener(this);

        setUpMapIfNeeded();
    }

    @Override
    public void onAddressSelected(LocationSearchBar bar, Address address) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 15));
    }

    @Override
    public void onClick(View v) {
        LatLng newPt = map.getCameraPosition().target;
        if(v.getId()==R.id.location_picker_set_home){
            home=newPt;
            if(home_marker==null)
                home_marker= map.addMarker(new MarkerOptions().position(newPt));
            else
                home_marker.setPosition(newPt);

            searchBar.setAddress(null);
            ((Button)findViewById(R.id.location_picker_set)).setText("Set Office");
        } else {
            office=newPt;
            if(office_marker==null)
                office_marker= map.addMarker(new MarkerOptions().position(newPt));
            else
                office_marker.setPosition(newPt);
        }
    }
}
