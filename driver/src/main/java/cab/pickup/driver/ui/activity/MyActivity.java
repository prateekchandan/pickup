package cab.pickup.driver.ui.activity;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.common.Constants;
import cab.pickup.common.api.Driver;
import cab.pickup.common.api.User;
import cab.pickup.common.util.LocationTracker;
import cab.pickup.driver.MyApplication;
import cab.pickup.driver.R;
import cab.pickup.common.util.IOUtil;
import cab.pickup.driver.api.Group;
import cab.pickup.driver.util.DriverTracker;

/**
 * Created by prateek on 4/8/15.
 */
public class MyActivity extends AppCompatActivity implements ServiceConnection {

    SharedPreferences prefs;
    public Driver me;
    public Group group;
    DriverTracker tracker;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        prefs=getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        try {
            me=new Driver(new JSONObject(prefs.getString("driver_json","")));
            MyApplication.driver = me;
            Log.d("MyAct", me.getJson());
        } catch (JSONException e) {
            e.printStackTrace();
            me = new Driver();
        }

        try {
            group=new Group(new JSONObject(prefs.getString("group","")));
            Log.d("MyAct Group", group.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if(DriverTracker.instance==null){
            Log.d("Service","here");
            startService(new Intent(this,DriverTracker.class));
        }else{
            tracker = DriverTracker.instance;
            Log.d("Service","already initiated");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent i = new Intent(this, LocationTracker.class);
        bindService(i, this, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public int getAppVersion(){
        return IOUtil.getAppVersion(getApplicationContext());
    }

    public String getKey(){
        return Constants.getKey();
    }

    public SharedPreferences getSharedPreferences(){
        if(prefs==null)
            return getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        else
            return prefs;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        tracker = (DriverTracker)((DriverTracker.LocalBinder) service).getService();
        tracker.connect();
        Log.d("COnnected","Yo location tracker connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        tracker = null;
    }

    public LocationTracker getLocationTracker() {
        return tracker;
    }
}
