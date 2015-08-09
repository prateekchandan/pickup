package cab.pickup.driver.ui.activity;

import android.os.Bundle;
import android.util.Log;

import cab.pickup.common.Constants;
import cab.pickup.driver.R;
import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.Result;


public class LoginActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            getSupportActionBar().hide();
        }
        catch (NullPointerException E){
            E.printStackTrace();
        }
        setContentView(R.layout.activity_login);

    }

}
