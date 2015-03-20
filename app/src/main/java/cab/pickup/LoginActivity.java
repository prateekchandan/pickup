package cab.pickup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cab.pickup.server.PostTask;
import cab.pickup.server.Result;
import cab.pickup.util.User;


public class LoginActivity extends MyActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Session session = Session.getActiveSession();

        if(session!=null && session.isOpened()) {
            if (me.id == null) {
                addUser();
            } else {
                finish();
            }
        } else {
            findViewById(R.id.fb_login).setVisibility(View.VISIBLE);
        }

        ((LoginButton)findViewById(R.id.fb_login)).setReadPermissions("email");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);

        if(!prefs.contains("user_json")) {
            getBiodata();
        } else {
            addUser();
        }

        super.onActivityResult(requestCode,resultCode,data);
    }

    public void getBiodata(){
        Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), SettingsActivity.class);
                i.putExtra(getString(R.string.profile_tag_name), graphUser.getName());
                i.putExtra(getString(R.string.profile_tag_fbid), graphUser.getId());

                me.fbid=graphUser.getId();

                Log.i(TAG, "fbid : " + graphUser.getId());

                i.putExtra(getString(R.string.profile_tag_email), (String) graphUser.getProperty(getString(R.string.profile_tag_email)));
                i.putExtra(getString(R.string.profile_tag_gender), (String)graphUser.getProperty(getString(R.string.profile_tag_gender)));
                startActivityForResult(i, 1);
            }
        });
    }

    private void addUser() {
        Log.d(TAG, "add user");
        setResult(RESULT_OK);

        try {
            me=new User(new JSONObject(prefs.getString("user_json","")));
        } catch (JSONException e) {
            e.printStackTrace();
            me=new User();
        }

        AddUserTask a = new AddUserTask(this);
        a.execute(getUrl("/add_user"));

    }
    
    public void addDataToPrefs(String user_id, String gcm_id){
        SharedPreferences.Editor spe = prefs.edit();

        me.id=user_id;
        spe.putString("user_json",me.getJson());
        spe.putString("gcm_id", gcm_id);
        spe.putInt("app_version", getAppVersion());

        spe.commit();
    }

    class AddUserTask extends PostTask {
        private static final String TAG = "AddUserTask";

        GoogleCloudMessaging gcm;

        String SENDER_ID = "1032273645702",
                gcm_id;

        public AddUserTask(MyActivity context){
            super(context);
        }

        @Override
        protected Result doInBackground(String... params) {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            try {
                gcm_id = gcm.register(SENDER_ID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return super.doInBackground(params);
        }

        @Override
        public List<NameValuePair> getPostData(String[] params, int i) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("user_id", me.id));
            nameValuePairs.add(new BasicNameValuePair("key", getKey()));

            nameValuePairs.add(new BasicNameValuePair("gcm_id", gcm_id));

            nameValuePairs.add(new BasicNameValuePair("device_id", me.device_id));
            nameValuePairs.add(new BasicNameValuePair("fbid", me.fbid));
            nameValuePairs.add(new BasicNameValuePair("name", me.name));
            nameValuePairs.add(new BasicNameValuePair("email", me.email));
            nameValuePairs.add(new BasicNameValuePair("gender", me.gender));

            return nameValuePairs;
        }


        @Override
        public void onPostExecute(Result ret){
            super.onPostExecute(ret);
            if(ret.statusCode ==200) {
                String toast;
                try {
                    JSONObject result = new JSONObject(ret.data);

                    toast = result.get("message").toString();

                    ((LoginActivity) context).addDataToPrefs(result.get("user_id").toString(), gcm_id);
                } catch (JSONException e) {
                    toast="JSONException: "+e.getMessage();
                }

                Toast.makeText(context, toast, Toast.LENGTH_LONG).show();

                context.finish();
            }
        }
    }
}
