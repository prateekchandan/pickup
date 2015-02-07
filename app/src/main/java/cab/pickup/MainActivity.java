package cab.pickup;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.Session;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class MainActivity extends MyActivity {
    LinearLayout list_start, list_end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list_start=((LinearLayout)findViewById(R.id.list_start));
        list_end=((LinearLayout)findViewById(R.id.list_end));

        Session session = Session.getActiveSession();
        if(session == null || !session.isOpened()){
            Intent i = new Intent();
            i.setClass(this, LoginActivity.class);
            startActivity(i);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent();
            i.setClass(this, SettingsActivity.class);
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void search(View v){
        Geocoder gc = new Geocoder(this);
        String startStr = ((EditText)findViewById(R.id.field_start)).getText().toString();
        String endStr = ((EditText)findViewById(R.id.field_end)).getText().toString();

        try {
            List<Address> startResults = gc.getFromLocationName(startStr,5);
            List<Address> endResults = gc.getFromLocationName(endStr,5);

            ((LinearLayout)findViewById(R.id.list_start)).removeAllViews();
            for(Address a : startResults){
                list_start.addView(getListItem(a));
            }

            ((LinearLayout)findViewById(R.id.list_end)).removeAllViews();
            for(Address a : endResults){
                list_end.addView(getListItem(a));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TextView getListItem(Address a){
        TextView tv = new TextView(this);
        tv.setText(a.getFeatureName()+" ,"+a.getLocality()+" ,"+a.getLatitude()+","+a.getLongitude());
        tv.setTag(a);
        return tv;
    }

    public void showDirections(View v) {
        Address startAddr = (Address)list_start.getChildAt(0).getTag();
        Address endAddr = (Address)list_end.getChildAt(0).getTag();

        Intent i = new Intent();
        i.setClass(this, MapsActivity.class);

        i.putExtra(getString(R.string.extra_start_coord),getLatLng(startAddr));
        i.putExtra(getString(R.string.extra_end_coord),getLatLng(endAddr));

        startActivity(i);
    }

    public void openChat(View v){
        Intent i = new Intent();
        i.setClass(this, ChatActivity.class);

        startActivity(i);
    }

    private LatLng getLatLng(Address a){
        return new LatLng(a.getLatitude(),a.getLongitude());
    }
}
