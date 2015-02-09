package cab.pickup;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.provider.Settings;

import cab.pickup.util.IOUtil;
import cab.pickup.widget.LocationSearchBar;

public class MyActivity extends Activity {
    String device_id;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle icicle){
        super.onCreate(icicle);

        prefs=getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public int getAppVersion(){
        return IOUtil.getAppVersion(getApplicationContext());
    }

    public String getUrl(String... path){
        return getString(R.string.base_url)+path[0];
    }

    public String getKey(){
        return getString(R.string.key);
    }

    public SharedPreferences getSharedPreferences(){
        return getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
    }

    public void returnLocationSearchValue(Address address, int id){
        ((LocationSearchBar)findViewById(id)).setAddress(address);
    }
}
