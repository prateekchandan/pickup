package cab.pickup.server;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.util.List;

import cab.pickup.MyActivity;
import cab.pickup.util.IOUtil;

public abstract class PostTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "ServerTask";
    MyActivity context;

    public PostTask(MyActivity context){
        this.context=context;
    }
    @Override
    protected String doInBackground(String... params) {
        String url = params[0], ret_value=null;

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        HttpPost httppost = new HttpPost(url);
        int statusCode = 0;

        try {
            List<NameValuePair> nameValuePairs = getPostData(params, 1);

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httppost);

            statusCode = response.getStatusLine().getStatusCode();

            Log.d(TAG, statusCode + " : " + response.getStatusLine().getReasonPhrase());

            if(statusCode==200){
                ret_value=IOUtil.buildStringFromIS(response.getEntity().getContent());
            } else {
                ret_value=null;
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        httpclient.close();

        return ret_value;
    }

    public abstract List<NameValuePair> getPostData(String[] params, int i);
}
