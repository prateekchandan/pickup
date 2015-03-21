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

import cab.pickup.ui.activity.MyActivity;
import cab.pickup.util.IOUtil;

public class GetTask extends AsyncTask<String, Integer, Result> {
    private static final String TAG = "GetTask";
    public MyActivity context;
    public String url;

    public GetTask(MyActivity context) {
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
            ret.statusCode = response.getStatusLine().getStatusCode();
            ret.statusMessage = response.getStatusLine().getReasonPhrase();

            Log.d(TAG, ret.statusCode + " : " + response.getStatusLine().getReasonPhrase());

            ret.data = IOUtil.buildStringFromIS(response.getEntity().getContent());
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
            String toast;
            try {
                JSONObject result = new JSONObject(res.data);

                toast=result.get("message").toString();
            } catch (JSONException e){
                toast=res.statusMessage;
            }

            Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
        }
    }
}