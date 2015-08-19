package cab.pickup.driver.ui.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.common.Constants;
import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.OnTaskCompletedListener;
import cab.pickup.common.server.Result;
import cab.pickup.driver.R;
import cab.pickup.driver.api.Group;
import cab.pickup.driver.gcm.GcmIntentService;

public class MainActivity extends MyActivity {

    BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GCM", "update Reciever called");

            NotificationManager mNotificationManager = (NotificationManager)
                   MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(intent.getIntExtra("notif_id",0));

            journeyAllocated();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        registerReceiver(mUpdateReceiver, new IntentFilter(GcmIntentService.JOURNEY_ALLOCATED_TAG));

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mUpdateReceiver);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent().hasExtra("action")){
            journeyAllocated();
        } else {
            Log.d("GCM", "No action");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        if(id == R.id.action_logout){
            prefs.edit().clear().apply();
            startActivity(new Intent(this,LoginActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void acceptRide(View V){
        startActivity(new Intent(this, RideActivity.class));
        finish();
    }

    public void journeyAllocated(){
        try {
            group=new Group(new JSONObject(prefs.getString("group","")));
        } catch (JSONException e) {
            group = null;
        }
        if(group==null){
            return;
        }
        findViewById(R.id.waiting_for_ride).setVisibility(View.GONE);
        findViewById(R.id.ride_accept).setVisibility(View.VISIBLE);
        Toast.makeText(this,"JourneyAllocated",Toast.LENGTH_LONG).show();
    }
}
