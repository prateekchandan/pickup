package cab.pickup;

import android.os.Bundle;

import cab.pickup.server.GetRidesTask;


public class RideActivity extends MyActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        new GetRidesTask(this).execute(getUrl("/user"),  getKey() , user_id);
    }


}
