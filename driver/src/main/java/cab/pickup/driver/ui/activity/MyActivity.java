package cab.pickup.driver.ui.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.common.Constants;
import cab.pickup.common.api.Driver;
import cab.pickup.common.api.User;
import cab.pickup.driver.R;
import cab.pickup.common.util.IOUtil;
import cab.pickup.driver.util.DriverTracker;

/**
 * Created by prateek on 4/8/15.
 */
public class MyActivity extends ActionBarActivity {

    SharedPreferences prefs;
    public Driver me;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        prefs=getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        try {
            me=new Driver(new JSONObject(prefs.getString("driver_json","")));
            Log.d("MyAct", me.getJson());
        } catch (JSONException e) {
            e.printStackTrace();
            me = new Driver();
        }

        if(DriverTracker.instance==null){
            Log.d("Service","here");
            startService(new Intent(this,DriverTracker.class));
        }else{
            Log.d("Service","already initiated");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public int getAppVersion(){
        return IOUtil.getAppVersion(getApplicationContext());
    }

    public String getKey(){
        return Constants.KEY;
    }

    public SharedPreferences getSharedPreferences(){
        if(prefs==null)
            return getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        else
            return prefs;
    }
}
