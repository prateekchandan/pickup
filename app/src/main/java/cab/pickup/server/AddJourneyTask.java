package cab.pickup.server;

import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cab.pickup.MyActivity;

public class AddJourneyTask extends PostTask {
    private static final String TAG = "AddJourneyTask";

    public AddJourneyTask(MyActivity context){
        super(context);
    }

    @Override
    public List<NameValuePair> getPostData(String[] params, int i) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

        nameValuePairs.add(new BasicNameValuePair("user_id", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("key", params[i++]));

        nameValuePairs.add(new BasicNameValuePair("start_lat", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("start_long", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("end_lat", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("end_long", params[i++]));

        nameValuePairs.add(new BasicNameValuePair("journey_time", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("margin_before", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("margin_after", params[i++]));
        nameValuePairs.add(new BasicNameValuePair("preference",params[i++]));

        nameValuePairs.add(new BasicNameValuePair("start_text",params[i++]));
        nameValuePairs.add(new BasicNameValuePair("end_text",params[i++]));

        return nameValuePairs;
    }

    @Override
    protected void onPostExecute(String ret){
        String toast="";
        if(ret == null){
            toast="There was an error in adding journey";
        } else {
            try {
                JSONObject result = new JSONObject(ret);
                toast = result.get("message").toString();

                Log.d(TAG, toast);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
    }
}
