package cab.pickup.driver.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

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
            startNextActivity();
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
                }else{
                    passwordField.setText("");
                    passwordField.setError(ret.statusMessage);
                }
            }
        }.execute(Constants.getUrl("/driver_login"));

    }

}
