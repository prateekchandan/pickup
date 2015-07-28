package cab.pickup.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.R;
import cab.pickup.api.Journey;
import cab.pickup.server.GetTask;
import cab.pickup.server.Result;


public class RideActivity extends MapsActivity {
    Journey journey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("JourneyCheck", "Contains journey: "+prefs.contains("journey"));
        try {
            journey=new Journey(new JSONObject(prefs.getString("journey","")));

            JSONArray start_pts = journey.group.getJSONObject("path_waypoints").getJSONArray("startwaypoints");
            JSONArray end_pts = journey.group.getJSONObject("path_waypoints").getJSONArray("endwaypoints");

            double start_lat = start_pts.getJSONArray(0).getDouble(0);
            double start_lng = start_pts.getJSONArray(0).getDouble(1);

            double end_lat = end_pts.getJSONArray(end_pts.length()-1).getDouble(0);
            double end_lng = end_pts.getJSONArray(end_pts.length()-1).getDouble(1);


            String waypoints="";
            for(int i=1; i<start_pts.length(); i++){
                waypoints+=start_pts.getJSONArray(i).getDouble(0)+","+start_pts.getJSONArray(i).getDouble(1)+"|";
            }

            for(int i=0; i<end_pts.length()-1; i++){
                waypoints+=end_pts.getJSONArray(i).getDouble(0)+","+end_pts.getJSONArray(i).getDouble(1)+"|";
            }
            if(waypoints.length()==0)
                waypoints="|";

            Log.d("WAYPTT",waypoints);
            String url="http://maps.googleapis.com/maps/api/directions/json?origin="
                    + start_lat + "," + start_lng + "&destination="
                    + end_lat + "," + end_lng+"&waypoints="+waypoints.substring(0,waypoints.length()-1);
            Log.d("WAYPTT",url);
            new MapDirectionsTask().execute(url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void cancel(View v){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Are you sure to cancel this journey ?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        prefs.edit().remove("journey").apply();

                        new GetTask(RideActivity.this){
                            @Override
                            public void onPostExecute(Result res) {
                                super.onPostExecute(res);
                                if(res.statusCode==200)
                                    Toast.makeText(RideActivity.this,res.statusMessage,Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RideActivity.this, MainActivity.class));
                                finish();
                            }
                        }.execute(getUrl("/cancel_journey/" + journey.id + "?key=" + getKey()));


                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


    }
}
