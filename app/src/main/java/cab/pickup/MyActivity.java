package cab.pickup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.util.IOUtil;
import cab.pickup.util.LocationTracker;
import cab.pickup.util.User;
import cab.pickup.widget.LocationSearchBar;

public class MyActivity extends FragmentActivity {
    public User me;

    SharedPreferences prefs;

    LocationTracker tracker;

    @Override
    protected void onCreate(Bundle icicle){
        super.onCreate(icicle);

        prefs=getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        try {
            me=new User(new JSONObject(prefs.getString("user_json","")));
            Log.d("MyAct", me.getJson());
        } catch (JSONException e) {
            e.printStackTrace();
            me=new User();
        }

        me.device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        tracker=new LocationTracker(this);

        Intent i = new Intent(this, LocationTracker.class);
        startService(i);

        Log.d("MyActivity", me.id==null?"user_id null":me.id);
    }

    public int getAppVersion(){
        return IOUtil.getAppVersion(getApplicationContext());
    }

    public String getUrl(String... path){
        return getString(R.string.base_url)+(path!=null?path[0]:"");
    }

    public String getKey(){
        return getString(R.string.key);
    }

    public SharedPreferences getSharedPreferences(){
        if(prefs==null)
            return getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        else
            return prefs;
    }

    public void returnLocationSearchValue(Address address, int id){
        ((LocationSearchBar)findViewById(id)).setAddress(address);
    }

    public LocationTracker getLocationTracker() {
        return tracker;
    }

    public void onLocationUpdate(Location location){
        // do nothing
    }
}
