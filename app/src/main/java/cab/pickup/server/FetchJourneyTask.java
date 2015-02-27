package cab.pickup.server;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
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
                return IOUtil.buildStringFromIS(response.getEntity().getContent());
            } else {
                Log.e(TAG, "url : "+url+"\n"+IOUtil.buildStringFromIS(response.getEntity().getContent()));
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(String ret){
        Log.d(TAG, ret);
        JSONObject gmapRes = MapUtil.getResult(ret);

        context.addPath(MapUtil.getPath(gmapRes));
    }
}