package cab.pickup.server;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cab.pickup.MapsActivity;
import cab.pickup.util.IOUtil;
import cab.pickup.util.MapUtil;

public class FetchJourneyTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "FetchJourney";
    MapsActivity context;

    public FetchJourneyTask(MapsActivity context){
        this.context=context;
    }

    @Override
    protected String doInBackground(String... params) {
        String ret_value=null;

        String url = params[0],
                access_key = params[1];

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        url+="/"+params[2]+"?key="+access_key;
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
            Toast.makeText(context, "Error while getting path from server!", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, ret);
        JSONObject gmapRes = MapUtil.getResult(ret);

        try {
            context.addPath(MapUtil.getPath(gmapRes));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}