package cab.pickup.ui.activity;

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

import cab.pickup.R;
import cab.pickup.common.Constants;
import cab.pickup.gcm.GcmIntentService;
import cab.pickup.common.server.SendMessageTask;
import cab.pickup.common.util.IOUtil;


public class ChatActivity extends MyActivity {

    String SENDER_ID = "1032273645702";

    static final String TAG = "GCMDemo";

    BroadcastReceiver msgReceiver;

    LinearLayout msgList;

    LinearLayout.LayoutParams sentMsgLP, rcdMsgLP;
    GoogleCloudMessaging gcm;

    String registration_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        msgList = (LinearLayout) findViewById(R.id.msg_list);

        gcm = GoogleCloudMessaging.getInstance(this);

        registration_id = getRegistrationId(getApplicationContext());

        msgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LinearLayout rcdMsg = (LinearLayout)getLayoutInflater().inflate(R.layout.received_msg, msgList);

                ((TextView)rcdMsg.findViewById(R.id.message_body)).setText(intent.getStringExtra("message"));
            }
        };

        registerReceiver(msgReceiver, new IntentFilter(GcmIntentService.MSG_REC_INTENT_TAG));


    }

    @Override
    public void onDestroy(){
        unregisterReceiver(msgReceiver);

        super.onDestroy();
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
        SendMessageTask task = new SendMessageTask();

        String msg = ((EditText)findViewById(R.id.msg_input)).getText().toString();
        task.execute(msg, Constants.getUrl("/send.php"), me.device_id, getKey());


        LinearLayout sentMsg = (LinearLayout)getLayoutInflater().inflate(R.layout.sent_msg, msgList);

        ((TextView)sentMsg.findViewById(R.id.message_body)).setText(msg);


        ((EditText)findViewById(R.id.msg_input)).setText("");
    }
}
