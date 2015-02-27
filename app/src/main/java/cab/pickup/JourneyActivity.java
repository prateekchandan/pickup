package cab.pickup;

import android.os.Bundle;

import cab.pickup.server.FetchJourneyTask;


public class JourneyActivity extends MapsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        new FetchJourneyTask(this).execute(getUrl("/journey"), getKey(), getIntent().getStringExtra("id"));
    }


}
