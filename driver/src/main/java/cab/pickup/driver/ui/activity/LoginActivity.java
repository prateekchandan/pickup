package cab.pickup.driver.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cab.pickup.common.Constants;
import cab.pickup.common.api.Driver;
import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.PostTask;
import cab.pickup.common.server.Result;
import cab.pickup.driver.R;


public class LoginActivity extends MyActivity {

    EditText usernameField , passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        usernameField = ((EditText)findViewById(R.id.username));
        passwordField = ((EditText)findViewById(R.id.password));

        if(me.driver_id!=null){
            registerGCM();
        }

    }

    public void startNextActivity(){
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    public void login(View v){
        final String username , password;
        username=usernameField.getText().toString();
        password=passwordField.getText().toString();
        int check=0;
        if(username.equals("")) {
            usernameField.setError(getString(R.string.empty_username));
            check = 1;
        }
        if(password.equals("")){
            passwordField.setError(getString(R.string.empty_password));
            check = 1;
        }
        if(check==1)
            return;

        new PostTask(this, getString(R.string.login_attempt)) {
            @Override
            public List<NameValuePair> getPostData(String[] params, int i) {
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("username", username));
                nameValuePairs.add(new BasicNameValuePair("password", password));
                nameValuePairs.add(new BasicNameValuePair("key", getKey()));
                Log.d("PostingData",nameValuePairs.toString());
                return nameValuePairs;
            }

            @Override
            public void onPostExecute(Result ret){
                super.onPostExecute(ret);
                if(ret.statusCode ==200) {
                    Toast.makeText(LoginActivity.this,getString(R.string.login_success),Toast.LENGTH_LONG).show();
                    try{
                        me = new Driver(ret.data.getJSONObject("driver"));
                        SharedPreferences.Editor spe = prefs.edit();
                        spe.putString("driver_json", me.getJson());
                        spe.apply();
                    }catch (Exception E){
                        E.printStackTrace();
                    }
                    registerGCM();
                }else{
                    passwordField.setText("");
                    Toast.makeText(LoginActivity.this,ret.statusMessage,Toast.LENGTH_LONG).show();
                }
            }
        }.execute(Constants.getUrl("/driver_login"));

    }

    public void registerGCM(){
        final GoogleCloudMessaging gcm;

        final String SENDER_ID = "1032273645702";
        gcm = GoogleCloudMessaging.getInstance(this);

        new PostTask(this){
            @Override
            protected Result doInBackground(String... params) {
                String gcm_id=null;
                try {
                    gcm_id = gcm.register(SENDER_ID);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return super.doInBackground(params[0], gcm_id);
            }

            @Override
            public List<NameValuePair> getPostData(String[] params, int i) {

                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("driver_id", me.driver_id));
                nameValuePairs.add(new BasicNameValuePair("key", getKey()));

                nameValuePairs.add(new BasicNameValuePair("reg_id", params[i]));

                return nameValuePairs;
            }
        }.execute(Constants.getUrl("/driver_register_gcm"));
        startNextActivity();
    }

}
