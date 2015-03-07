package cab.pickup;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.server.GetTask;


public class RideActivity extends MyActivity implements View.OnLongClickListener{
    View.OnLongClickListener longCL = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        new GetRidesTask(this).execute(getUrl("/user/"+user_id+"/all_journey?key="+getKey()));
    }

    @Override
    public boolean onLongClick(View v) {
        String id = (String)v.getTag();

        new GetTask(this).execute(getUrl("/delete_journey/"+id+"?key="+getKey()));

        v.setVisibility(View.GONE);

        return true;
    }

    class GetRidesTask extends GetTask {
        private static final String TAG = "GetRides";

        public GetRidesTask(MyActivity context){
            super(context);
        }

        @Override
        public void onPostExecute(String ret){
            if(ret==null) {
                Toast.makeText(context, "Error while getting rides from server!", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, ret);
            try {
                JSONArray arr = new JSONArray(ret);

                for(int i=0 ; i<arr.length();i++) {
                    JSONObject result = (JSONObject)arr.get(i);
                    String text = "From:" + result.get("start_text").toString()+"\n"+
                            "To:" + result.get("end_text").toString()+"\n"+
                            "Time:" + result.get("journey_time").toString()+"\n";

                    TextView tv = new TextView(context);

                    tv.setText(text);
                    tv.setTag(result.get("journey_id"));
                    tv.setOnLongClickListener(longCL);

                    Log.d(TAG, text);
                    ((LinearLayout) context.findViewById(R.id.ride_list)).addView(tv);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

}
