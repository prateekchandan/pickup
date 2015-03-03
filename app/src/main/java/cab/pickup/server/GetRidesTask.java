package cab.pickup.server;


import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cab.pickup.MyActivity;
import cab.pickup.R;
import cab.pickup.util.IOUtil;

public class GetRidesTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "GetRides";
    MyActivity context;

    public GetRidesTask(MyActivity context){
        this.context=context;
    }

    @Override
    protected String doInBackground(String... params) {
        String ret_value=null;

        String url = params[0],
                access_key = params[1],
                user_id=params[2];

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        url+="/"+user_id+"/all_journey?key="+access_key;
        HttpGet httpget= new HttpGet(url);
        int statusCode = 0;

        try {
            HttpResponse response = httpclient.execute(httpget);
            statusCode = response.getStatusLine().getStatusCode();

            Log.d(TAG, statusCode + " : " + response.getStatusLine().getReasonPhrase());

            if(statusCode==200){
                ret_value = IOUtil.buildStringFromIS(response.getEntity().getContent());
            } else {
                Log.e(TAG, "url : "+url+"\n"+IOUtil.buildStringFromIS(response.getEntity().getContent()));
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        httpclient.close();

        return ret_value;
    }

    @Override
    protected void onPostExecute(String ret){
        if(ret==null) {
            Toast.makeText(context, "Error while getting rides from server!", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, ret);
        try {
            JSONArray arr = new JSONArray(ret);

            for(int i=0 ; i<arr.length();i++) {
                JSONObject result = (JSONObject)arr.get(i);
                String text = "From:" + result.get("start_lat").toString() + "," + result.get("start_long").toString() +"\n"+
                        "To:" + result.get("end_lat").toString() + "," + result.get("end_long").toString() +"\n"+
                        "Time:" + result.get("journey_time").toString();

                TextView tv = new TextView(context);

                tv.setText(text);

                Log.d(TAG, text);
                ((LinearLayout) context.findViewById(R.id.ride_list)).addView(tv);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
