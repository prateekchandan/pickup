package cab.pickup;

import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Date;

import cab.pickup.server.AddJourneyTask;


public class BookActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
    }

    public void addJourney(View v){
        Address start = getIntent().getParcelableExtra("address_start");
        Address end = getIntent().getParcelableExtra("address_end");

        TimePicker journey_time = (TimePicker)findViewById(R.id.journey_time);
        String time = journey_time.getCurrentHour()+":"+journey_time.getCurrentMinute()+":00";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
        String currentDate = sdf.format(new Date());

        new AddJourneyTask(this).execute(getUrl("/add_journey"), user_id, getKey()
                ,start.getLatitude()+""
                ,start.getLongitude()+""
                ,end.getLatitude()+""
                ,end.getLongitude()+""
                ,currentDate+time
                ,"30"
                ,"30"
                ,"1");
    }


}
