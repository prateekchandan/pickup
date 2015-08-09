package cab.pickup.driver.ui.activity;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import cab.pickup.common.Constants;
import cab.pickup.driver.R;
import cab.pickup.common.util.IOUtil;

/**
 * Created by prateek on 4/8/15.
 */
public class MyActivity extends ActionBarActivity {

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
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
