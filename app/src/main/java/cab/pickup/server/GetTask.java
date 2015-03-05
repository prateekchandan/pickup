package cab.pickup.server;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

import cab.pickup.MyActivity;
import cab.pickup.util.IOUtil;

public class GetTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "GetTask";
     public MyActivity context;

    public GetTask(MyActivity context) {
        this.context=context;
    }

    @Override
    protected String doInBackground(String... params) {
        String ret_value = null;

        String url = params[0];

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        HttpGet httpget = new HttpGet(url);
        int statusCode = 0;

        try {
            HttpResponse response = httpclient.execute(httpget);
            statusCode = response.getStatusLine().getStatusCode();

            Log.d(TAG, statusCode + " : " + response.getStatusLine().getReasonPhrase());

            if (statusCode == 200) {
                ret_value = IOUtil.buildStringFromIS(response.getEntity().getContent());
            } else {
                Log.e(TAG, "url : " + url + "\n" + IOUtil.buildStringFromIS(response.getEntity().getContent()));
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
    public void onPostExecute(String ret){
        if(ret!=null){
            Toast.makeText(context, "Task successfull : "+ret, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Task failed!", Toast.LENGTH_LONG).show();
        }
    }
}