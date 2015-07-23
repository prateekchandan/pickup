package cab.pickup.ui.activity;

import android.os.Bundle;
import android.view.View;

import cab.pickup.R;


public class RideActivity extends MapsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
    }

    public void cancel(View v){

    }
}
