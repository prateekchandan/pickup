package cab.pickup.server;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

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

import cab.pickup.MyActivity;
import cab.pickup.util.IOUtil;

public class AddUserTask extends AsyncTask<String, Integer, String>{
    private static final String TAG = "AddUserTask";
    MyActivity context;

    GoogleCloudMessaging gcm;

    String SENDER_ID = "1032273645702",
            gcm_id;

    public AddUserTask(MyActivity context){
        this.context=context;
    }

    @Override
    protected String doInBackground(String... params) {
        String url=params[0],
                user_id=params[1],
                access_key=params[2];

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
        HttpPost httppost = new HttpPost(url);
        int statusCode=0;

        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            gcm_id = gcm.register(SENDER_ID);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
            nameValuePairs.add(new BasicNameValuePair("key", access_key));

            nameValuePairs.add(new BasicNameValuePair("gcm_id", gcm_id));

            nameValuePairs.add(new BasicNameValuePair("device_id", params[3]));
            nameValuePairs.add(new BasicNameValuePair("fbid", params[4]));
            nameValuePairs.add(new BasicNameValuePair("name", params[5]));
            nameValuePairs.add(new BasicNameValuePair("email", params[6]));
            nameValuePairs.add(new BasicNameValuePair("gender", params[7]));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httppost);

            statusCode = response.getStatusLine().getStatusCode();

            Log.d(TAG, statusCode + " : " + response.getStatusLine().getReasonPhrase());

            String json = IOUtil.buildStringFromIS(response.getEntity().getContent());

            Log.d(TAG, json);

            JSONObject result = new JSONObject(json);

            String msg = result.get("message").toString();

            Log.e(TAG, "Error: "+msg);

            if(statusCode==200){
                String usr = result.get("user_id").toString();
                Log.d(TAG, "user_id extaracted: "+usr);
                return usr;
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(String user_id){
        context.getSharedPreferences().edit().putString("user_id",user_id);
        context.getSharedPreferences().edit().putString("gcm_id", gcm_id);
        context.getSharedPreferences().edit().putInt("app_version", IOUtil.getAppVersion(context));

        context.getSharedPreferences().edit().commit();

        context.finish();
    }
}
