package cab.pickup.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.R;
import cab.pickup.api.User;
import cab.pickup.util.IOUtil;
import cab.pickup.util.LocationTracker;

public class MyActivity extends FragmentActivity implements ServiceConnection{
    public User me;

    SharedPreferences prefs;

    LocationTracker tracker;

    @Override
    protected void onCreate(Bundle icicle){
        super.onCreate(icicle);

        prefs=getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        try {
            me=new User(new JSONObject(prefs.getString("user_json","")), false);
            Log.d("MyAct", me.getJson());
        } catch (JSONException e) {
            e.printStackTrace();
            me=new User();
        }

        me.device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        Intent i = new Intent(this, LocationTracker.class);
        //bindService(i,this,BIND_AUTO_CREATE);

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

    public LocationTracker getLocationTracker() {
        return tracker;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        tracker = ((LocationTracker.LocalBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        tracker = null;
    }

    public void onLocationUpdate(Location location){
        // do nothing
    }
}
