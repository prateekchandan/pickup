package cab.pickup.chat;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cab.pickup.R;
import cab.pickup.util.IOUtil;

public class RegisterTask extends AsyncTask<String,Integer,String> {
    Context context;
    GoogleCloudMessaging gcm;

    String SENDER_ID = "1032273645702",
            registration_id;

    static final String TAG = "RegisterTask",
                        PROPERTY_REG_ID = "reg_id",
                        PROPERTY_APP_VERSION = "app_version";

    public RegisterTask(Context c){
        context=c;
    }

    @Override
    protected String doInBackground(String... params) {
        String msg = "",
                url=params[0],
                device_id=params[1],
                access_key=params[2];
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            registration_id = gcm.register(SENDER_ID);
            msg = "Device registered, registration ID=" + registration_id;

            Log.d(TAG, msg);

            AndroidHttpClient httpclient = AndroidHttpClient.newInstance(TAG);
            HttpPost httppost = new HttpPost(url);

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("reg_id", registration_id));
                nameValuePairs.add(new BasicNameValuePair("device_id", device_id));
                nameValuePairs.add(new BasicNameValuePair("key", access_key));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                Log.d(TAG, response.getStatusLine().getStatusCode() +" : "+response.getStatusLine().getReasonPhrase());

            } catch (ClientProtocolException e) {
                Log.e(TAG,e.getMessage());
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
            }

            SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);

            int appVersion = IOUtil.getAppVersion(context);
            Log.i(TAG, "Saving regId on app version " + appVersion);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(PROPERTY_REG_ID, registration_id);
            editor.putInt(PROPERTY_APP_VERSION, appVersion);
            editor.commit();
        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
        }
        return msg;
    }
}
