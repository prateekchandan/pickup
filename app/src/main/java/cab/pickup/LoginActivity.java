package cab.pickup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.server.AddUserTask;
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
        a.execute(getUrl("/add_user"), me.id, getKey()
                , me.device_id
                , me.fbid
                , me.name
                , me.email
                , me.gender);

    }
    
    public void addDataToPrefs(String user_id, String gcm_id){
        SharedPreferences.Editor spe = prefs.edit();
        
        spe.putString("user_id",user_id);
        spe.putString("gcm_id", gcm_id);
        spe.putInt("app_version", getAppVersion());

        spe.commit();
    }
}
