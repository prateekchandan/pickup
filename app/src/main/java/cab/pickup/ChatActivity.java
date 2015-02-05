package cab.pickup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import cab.pickup.chat.GcmIntentService;
import cab.pickup.chat.RegisterTask;
import cab.pickup.chat.SendMessageTask;
import cab.pickup.util.IOUtil;


public class ChatActivity extends MyActivity {

    String SENDER_ID = "1032273645702";

    static final String TAG = "GCMDemo";

    BroadcastReceiver msgReciever;

    LinearLayout msgList;
    GoogleCloudMessaging gcm;

    String registration_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        msgList = (LinearLayout) findViewById(R.id.msg_list);

        gcm = GoogleCloudMessaging.getInstance(this);

        registration_id = getRegistrationId(getApplicationContext());

        if (registration_id.isEmpty()) {
            new RegisterTask(getApplicationContext()).execute(
                    getUrl("/add.php"),
                    device_id,
                    getKey());
        }

        msgReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView tv = new TextView(getApplicationContext());
                tv.setText(intent.getStringExtra("message"));

                msgList.addView(tv);
            }
        };

        registerReceiver(msgReciever, new IntentFilter(GcmIntentService.MSG_REC_INTENT_TAG));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getRegistrationId(Context context) {
        String registration_id = prefs.getString("registration_id", "");
        if (registration_id.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        int registeredVersion = prefs.getInt("app_version", Integer.MIN_VALUE);
        int currentVersion = IOUtil.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registration_id;
    }

    public void sendMsg(View v){
        TextView newmsg = new TextView(this);
        SendMessageTask task = new SendMessageTask();

        String msg = ((EditText)findViewById(R.id.msg_input)).getText().toString();
        task.execute(msg, getUrl("/send.php"),device_id, getKey());

        newmsg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        newmsg.setTextColor(0xffffffff);

        newmsg.setText(msg);

        msgList.addView(newmsg);

        ((EditText)findViewById(R.id.msg_input)).setText("");
    }
}
