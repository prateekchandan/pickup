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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cab.pickup.util.IOUtil;

public class AddUserTask extends AsyncTask<String, Integer, String>{
    private static final String TAG = "SendMessageTask";

    @Override
    protected String doInBackground(String... params) {
        String url=params[0],
                user_id=params[1],
                device_id=params[2],
                access_key=params[3];

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        HttpPost httppost = new HttpPost(url);
        int statusCode=0;

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
            nameValuePairs.add(new BasicNameValuePair("device_id", device_id));
            nameValuePairs.add(new BasicNameValuePair("key", access_key));

            nameValuePairs.add(new BasicNameValuePair("fbid", params[4]));
            nameValuePairs.add(new BasicNameValuePair("name", params[5]));
            nameValuePairs.add(new BasicNameValuePair("email", params[6]));
            nameValuePairs.add(new BasicNameValuePair("gender", params[7]));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httppost);

            statusCode = response.getStatusLine().getStatusCode();

            Log.d(TAG, statusCode + " : " + response.getStatusLine().getReasonPhrase());

            if(statusCode==200){
                JSONObject result = new JSONObject(IOUtil.buildStringFromIS(response.getEntity().getContent()));

                String msg = result.get("message").toString();
                if(msg.equals("user added")){
                    return result.get("user_id").toString();
                }
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
