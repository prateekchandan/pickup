package cab.pickup.ui.activity;

import android.os.Bundle;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;

import cab.pickup.R;
import cab.pickup.server.GetTask;
import cab.pickup.server.Result;


public class RideActivity extends MapsActivity {
    int group_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        group_id = getIntent().getIntExtra("group_id",-1);

        GetTask commonJourneyTask = new GetTask(this){
            @Override
            public void onPostExecute(Result res) {
                super.onPostExecute(res);
                if(res.statusCode==200){

                    try {
                        JSONArray start_pts = res.data.getJSONArray("start_waypoints");
                        JSONArray end_pts = res.data.getJSONArray("end_waypoints");

                        double start_lat = start_pts.getJSONArray(0).getDouble(0);
                        double start_lng = start_pts.getJSONArray(0).getDouble(1);

                        double end_lat = end_pts.getJSONArray(end_pts.length()-1).getDouble(0);
                        double end_lng = end_pts.getJSONArray(end_pts.length()-1).getDouble(1);


                        String waypoints="";
                        for(int i=1; i<start_pts.length(); i++){
                            waypoints+=start_pts.getJSONArray(i).getDouble(0)+",";
                        }

                        String url="http://maps.googleapis.com/maps/api/directions/json?origin="
                                + start_lat + "," + start_lng + "&destination="
                                + end_lat + "," + end_lng;
                        new MapDirectionsTask().execute(url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
    }

    public void cancel(View v){

    }
}
