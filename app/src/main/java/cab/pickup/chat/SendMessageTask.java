package cab.pickup.chat;


import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cab.pickup.util.IOUtil;

public class SendMessageTask extends AsyncTask<String,Integer,Integer> {
    private static final String TAG = "SendMessageTask";
    String url, device_id, access_key;

    public SendMessageTask(String url, String dev_id, String key){
        this.url=url;
        device_id=dev_id;
        access_key=key;
    }

    @Override
    protected Integer doInBackground(String... params) {
        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        HttpPost httppost = new HttpPost(url);
        int statusCode=0;

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("message", params[0]));
            nameValuePairs.add(new BasicNameValuePair("device_id", device_id));
            nameValuePairs.add(new BasicNameValuePair("key", access_key));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httppost);

            statusCode = response.getStatusLine().getStatusCode();

            Log.d(TAG, statusCode + " : " + response.getStatusLine().getReasonPhrase());
            Log.d(TAG, "Send response : " + IOUtil.buildStringFromIS(response.getEntity().getContent()));
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return statusCode;
    }
}
