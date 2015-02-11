package cab.pickup;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

import cab.pickup.server.AddUserTask;


public class LoginActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Session session = Session.getActiveSession();

        if(session!=null && session.isOpened())
            finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);

        if(!prefs.contains("name")) {
            getBiodata();
        } else {
            AddUserTask a = new AddUserTask(this);
            a.execute(getUrl("add_user"), user_id, device_id, getKey()
                    ,getData(getString(R.string.profile_tag_fbid))
                    ,getData(getString(R.string.profile_tag_name))
                    ,getData(getString(R.string.profile_tag_email))
                    ,getData(getString(R.string.profile_tag_gender)));
        }
    }

    public void getBiodata(){
        Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), SettingsActivity.class);
                i.putExtra(getString(R.string.profile_tag_name), graphUser.getName());
                i.putExtra(getString(R.string.profile_tag_fbid), graphUser.getId());
                i.putExtra(getString(R.string.profile_tag_email), (String)graphUser.getProperty(getString(R.string.profile_tag_email)));
                i.putExtra(getString(R.string.profile_tag_gender), (String)graphUser.getProperty(getString(R.string.profile_tag_gender)));
                startActivityForResult(i, RESULT_OK);
            }
        });
    }
}
