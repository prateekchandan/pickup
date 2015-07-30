package cab.pickup.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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

import cab.pickup.R;
import cab.pickup.api.Journey;
import cab.pickup.api.User;
import cab.pickup.server.GetTask;
import cab.pickup.server.PostTask;
import cab.pickup.server.Result;


public class    LoginActivity extends MyActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            getSupportActionBar().hide();
        }
        catch (NullPointerException E){E.printStackTrace();}
        setContentView(R.layout.activity_login);

        Session session = Session.getActiveSession();

        if(session!=null && session.isOpened()) {
            findViewById(R.id.fb_login).setVisibility(View.GONE);

            ((TextView)findViewById(R.id.login_message_text)).setText("Loading...");
            if (me.id == null) {
                ((TextView)findViewById(R.id.login_message_text)).setText("Loading...");
                addUser();
            } else {
                startNextActivity();
            }
        } else {
            findViewById(R.id.fb_login).setVisibility(View.VISIBLE);

            ((TextView)findViewById(R.id.login_message_text)).setText("");
        }

        ((LoginButton)findViewById(R.id.fb_login)).setReadPermissions("email");
    }



    @Override
    protected void onResume(){
        super.onResume();
        //checkGPS();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);

        if(!prefs.contains("user_json")) {
            findViewById(R.id.fb_login).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.login_message_text)).setText("Adding user..");
            checkFromFBID();
        } else if(me.id==null){
            addUser();
        }
        else{
            updateUser();
            Log.d(TAG,"Reached here");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public void getBiodata(){
        Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), SettingsActivity.class);
                i.putExtra(getString(R.string.profile_tag_name), graphUser.getName());
                i.putExtra(getString(R.string.profile_tag_fbid), graphUser.getId());

                me.fbid = graphUser.getId();

                Log.i(TAG, "fbid : " + graphUser.getId());

                i.putExtra(getString(R.string.profile_tag_email), (String) graphUser.getProperty("email"));
                i.putExtra(getString(R.string.profile_tag_gender), (String) graphUser.getProperty("gender"));
                startActivityForResult(i, 1);
            }
        });
    }

    private void addUser() {
        Log.d(TAG, "add user");
        setResult(RESULT_OK);

        if(!prefs.contains("user_json")) {
            getBiodata();
            return;
        }
        try {
            me=new User(new JSONObject(prefs.getString("user_json","")), false);
            Log.d("user_data",prefs.getString("user_json",""));
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
        spe.putString("user_json", me.getJson());
        spe.putString("gcm_id", gcm_id);
        spe.putInt("app_version", getAppVersion());
        spe.commit();
    }

    public void checkGPS(){
        Context context = this;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.gps_not_found_title);  // GPS not found
            builder.setMessage(R.string.gps_not_found_message); // Want to enable?
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                }
            });
            builder.setNegativeButton(R.string.no, null);
            builder.create().show();
            return;
        }
    }

    class AddUserTask extends PostTask {
        private static final String TAG = "AddUserTask";

        GoogleCloudMessaging gcm;

        String SENDER_ID = "1032273645702",
                gcm_id;

        public AddUserTask(MyActivity context){
            super(context);
            dialogMessage="Updating user data..";
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
            nameValuePairs.add(new BasicNameValuePair("phone", me.phone));
            nameValuePairs.add(new BasicNameValuePair("age", me.age));
            nameValuePairs.add(new BasicNameValuePair("company", me.company));
            nameValuePairs.add(new BasicNameValuePair("company_email", me.company_email));

            String mac_addr = ((WifiManager)getSystemService(WIFI_SERVICE)).getConnectionInfo().getMacAddress();
            nameValuePairs.add(new BasicNameValuePair("mac_addr", mac_addr));

            return nameValuePairs;
        }


        @Override
        public void onPostExecute(Result ret){
            super.onPostExecute(ret);
            if(ret.statusCode ==200) {
                ((LoginActivity) context).addDataToPrefs(ret.data.optString("user_id"), gcm_id);

                Toast.makeText(context, ret.statusMessage, Toast.LENGTH_LONG).show();

                startNextActivity();
            }
        }
    }

    private void startNextActivity() {
        if(prefs.contains("journey")) {
            try {
                JSONObject journey_data = new JSONObject(prefs.getString("journey", ""));
                Journey journey = new Journey(journey_data);

                if (journey.group == null) {
                    Intent i  = new Intent(this, MainActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(this, RideActivity.class);
                    i.putExtra("group_id",journey.group.toString());
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Intent i  = new Intent(this, MainActivity.class);
                startActivity(i);
            }
        }
        else{
            Intent i  = new Intent(this, MainActivity.class);
            startActivity(i);
        }
        finish();
    }

    public void updateUser(){
        GetTask getTask = new GetTask(this) {
            @Override
            public void onPostExecute(Result ret){
                super.onPostExecute(ret);
                if(ret.statusCode==200){
                    Toast.makeText(getApplicationContext(),ret.data.toString(),Toast.LENGTH_LONG).show();
                }
            }
        };
        getTask.execute(getUrl("/user/" + String.valueOf(me.id) + "?key=" + getKey())
        );
    }

    public void checkFromFBID(){
        Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                if(graphUser==null){
                    Toast.makeText(getApplicationContext(),"Unable to connect to Facebook. Please check your network conenction!!",Toast.LENGTH_LONG).show();
                    findViewById(R.id.fb_login).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.login_message_text)).setText("");
                    return;
                }
                final String fbid = graphUser.getId();
                GetTask getTask = new GetTask(LoginActivity.this,"Logging in..") {
                    @Override
                    public void onPostExecute(Result ret){
                        super.onPostExecute(ret);
                        if(ret.statusCode==200){
                            int userPresent;
                            try{
                                userPresent = ret.data.getInt("user_present");
                            }
                            catch (Exception E){
                                userPresent = 0;
                            }
                            if(userPresent==1){
                                try {
                                    JSONObject userdata= ret.data.getJSONObject("user_data");
                                    me.id = userdata.getString("id");
                                    me.phone = userdata.getString("phone");
                                    me.age = userdata.getString("age");
                                    me.company = userdata.getString("company");
                                    me.company_email = userdata.getString("company_email");
                                    me.email = userdata.getString("email");
                                    me.name = userdata.getString("first_name");
                                    me.gender=userdata.getString("gender");
                                    me.fbid = fbid;

                                    SharedPreferences.Editor spe = prefs.edit();
                                    spe.putString("user_json", me.getJson());
                                    spe.commit();
                                    startNextActivity();
                                    Toast.makeText(getApplicationContext(),getString(R.string.login_successful),Toast.LENGTH_LONG).show();
                                }catch (Exception E){
                                    Log.e(TAG,E.getMessage());
                                    getBiodata();
                                }
                            }
                            else{
                                getBiodata();
                            }

                        }
                    }
                };
                getTask.execute(getUrl("/user_exists?" + "key=" + getKey()+"&fbid="+fbid));
            }
        });

    }


}
