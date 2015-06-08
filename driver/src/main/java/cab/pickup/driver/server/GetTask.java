package cab.pickup.driver.server;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

import cab.pickup.driver.IOUtil;
import cab.pickup.driver.MainActivity;

public class GetTask extends AsyncTask<String, Integer, Result> {
    private static final String TAG = "GetTask";
    public MainActivity context;
    public String url;

    public GetTask(MainActivity context) {
        this.context=context;
    }

    @Override
    protected Result doInBackground(String... params) {
        Result ret = new Result();

        if(url==null) url = params[0];

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        HttpGet httpget = new HttpGet(url);
        try {
            HttpResponse response = httpclient.execute(httpget);

            Log.d(TAG, ret.statusCode + " : " + response.getStatusLine().getReasonPhrase());

            ret = new Result(IOUtil.buildStringFromIS(response.getEntity().getContent()));
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        httpclient.close();

        return ret;
    }

    @Override
    public void onPostExecute(Result res){
        if(res.statusCode !=200){
            Toast.makeText(context, res.statusMessage, Toast.LENGTH_LONG).show();
        }
    }
}