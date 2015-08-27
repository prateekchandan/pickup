package cab.pickup.driver.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.driver.MyApplication;
import cab.pickup.driver.R;
import cab.pickup.common.Constants;
import cab.pickup.common.api.Driver;
import cab.pickup.common.api.Event;
import cab.pickup.common.api.Journey;
import cab.pickup.common.api.User;
import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.OnTaskCompletedListener;
import cab.pickup.common.server.Result;
import cab.pickup.driver.api.Group;
import cab.pickup.driver.ui.activity.MainActivity;
import cab.pickup.driver.ui.activity.MyActivity;
import cab.pickup.driver.ui.activity.RideActivity;

public class GcmIntentService extends IntentService {
    SharedPreferences prefs;
    Group group;
    JSONArray eventList = new JSONArray();

    public static final int TYPE_USER_ADDED=18,
                            TYPE_USER_CANCELLED=17,
                            ALLOCATED_GROUP=16;


    static final String TAG = "Intent Service";
    public static final String MSG_REC_INTENT_TAG = "MESSAGE_RECIEVED";
    public static final String JOURNEY_ADD_USER_INTENT_TAG = "JOURNEY_ADD_USER";
    public static final String JOURNEY_USER_CANCELLED_INTENT_TAG = "JOURNEY_USER_CANCELLED";
    public static final String JOURNEY_ALLOCATED_TAG = "JOURNEY_ALLOCATED_RIDE";

    NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefs=getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        try {
            group = new Group(new JSONObject(prefs.getString("group","")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            eventList = new JSONArray(prefs.getString("events",""));
        }catch (JSONException e) {
            e.printStackTrace();
        }

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Driver driver;
        if(MyApplication.driver==null)
            return;

        driver = MyApplication.driver;

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.e(TAG,"Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.e(TAG,"Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                Log.d(TAG, extras.toString());

                JSONObject msg = null;


                try {
                    msg = new JSONObject(extras.getString("message"));

                    final int msg_type = msg.getInt("type");
                    final JSONObject data = msg.getJSONObject("data");
                    final long time = data.getLong("time");

                    if(group!=null && !group.group_id.equals(data.getString("group_id"))){
                        return;
                    }

                    Log.i(TAG, "Received: " + extras.getString("message"));

                    if(msg_type==TYPE_USER_ADDED || msg_type == TYPE_USER_CANCELLED || msg_type == ALLOCATED_GROUP){
                            new GetTask(this){
                                @Override
                                public void onPostExecute(Result res){
                                    super.onPostExecute(res);
                                    if(res.statusCode==200){
                                        try {
                                            JSONObject groupObj = res.data.getJSONObject("final_data").getJSONObject("group_details");
                                            groupObj.put("journey_details",res.data.getJSONObject("final_data").getJSONArray("journey_details"));
                                            groupObj.put("group_id",data.getString("group_id"));
                                            group  = new Group(groupObj,new OnTaskCompletedListener(){
                                                @Override
                                                public void onTaskCompleted(Result res){
                                                    if(msg_type==ALLOCATED_GROUP)
                                                        sendJourneyUpdate(JOURNEY_ALLOCATED_TAG, "New Journey Allocated","A new journey has been allocated to you", MainActivity.class);
                                                    else if(msg_type==TYPE_USER_ADDED){
                                                        try {
                                                            sendJourneyUpdate(JOURNEY_ADD_USER_INTENT_TAG, "New User Added",data.getString("user_name")+" has been added to ride", RideActivity.class);
                                                        }catch (Exception E){
                                                            E.printStackTrace();
                                                        }
                                                    }
                                                    else{
                                                        try {
                                                            sendJourneyUpdate(JOURNEY_USER_CANCELLED_INTENT_TAG, "One User Cancelled Ride",data.getString("user_name")+" has left the ride", RideActivity.class);
                                                        }catch (Exception E){
                                                            E.printStackTrace();
                                                        }
                                                    }
                                                }
                                            });

                                        }catch (Exception E){
                                            E.printStackTrace();
                                        }
                                    }
                                }
                            }.execute(Constants.getUrl("/get_detailed_group/"+driver.driver_id+"?key="+Constants.KEY));

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendJourneyUpdate(String intent_tag, String title, String message,Class cls){
        prefs.edit()
                .putString("events",eventList.toString())
                .putString("group",group.toString())
                .apply();

        Intent i=new Intent(this,cls);
        i.putExtra("action", intent_tag);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                i, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setContentInfo(message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());

        try {
            Uri notification;
            if(intent_tag.equals(JOURNEY_ALLOCATED_TAG))
                notification= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            else
                notification= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent broadcast = new Intent(intent_tag);
        broadcast.putExtra("notif_id", 1);
        sendBroadcast(broadcast);
    }
}
