package cab.pickup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import cab.pickup.util.Journey;
import cab.pickup.util.MapUtil;


public class RideActivity extends MyActivity implements View.OnLongClickListener, View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        new GetRidesTask(this).execute(getUrl("/user/"+me.id+"/all_journey?key="+getKey()));
    }

    @Override
    public boolean onLongClick(final View v) {
        final String id = ((Journey)v.getTag()).id;

        new AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Do you really want to delete journey?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        new GetTask(RideActivity.this).execute(getUrl("/delete_journey/"+id+"?key="+getKey()));

                        v.setVisibility(View.GONE);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
        return true;
    }

    @Override
    public void onClick(View v) {
        Journey j= (Journey)v.getTag();

        Intent i=new Intent();

        i.putExtra("journey_json", j.getJson());

        setResult(RESULT_OK, i);

        finish();
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
                    Journey journey = new Journey((JSONObject)arr.get(i), Journey.TYPE_SINGLE);
                    String text = "From:" + MapUtil.stringFromAddress(journey.start)+"\n"+
                            "To:" + MapUtil.stringFromAddress(journey.end)+"\n"+
                            "Time:" + journey.datetime+"\n";

                    TextView tv = new TextView(context);

                    tv.setText(text);
                    tv.setTag(journey);
                    tv.setOnLongClickListener(RideActivity.this);
                    tv.setOnClickListener(RideActivity.this);

                    Log.d(TAG, text);
                    ((LinearLayout) context.findViewById(R.id.ride_list)).addView(tv);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

}