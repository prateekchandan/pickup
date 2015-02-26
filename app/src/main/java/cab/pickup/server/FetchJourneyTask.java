package cab.pickup.server;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cab.pickup.MapsActivity;
import cab.pickup.util.IOUtil;
import cab.pickup.util.MapUtil;

public class FetchJourneyTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "SendMessageTask";
    MapsActivity context;

    public FetchJourneyTask(MapsActivity context){
        this.context=context;
    }

    @Override
    protected String doInBackground(String... params) {
        String url = params[0],
                access_key = params[1];

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        HttpPost httppost = new HttpPost(url);
        int statusCode = 0;

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("key", access_key));
            nameValuePairs.add(new BasicNameValuePair("id", params[2]));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httppost);

            statusCode = response.getStatusLine().getStatusCode();

            Log.d(TAG, statusCode + " : " + response.getStatusLine().getReasonPhrase());

            if(statusCode==200){
                return IOUtil.buildStringFromIS(response.getEntity().getContent());
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
        JSONObject gmapRes = MapUtil.getResult(ret);

        context.addPath(MapUtil.getPath(gmapRes));
    }
}