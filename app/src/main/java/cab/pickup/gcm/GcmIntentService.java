package cab.pickup.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.MyApplication;
import cab.pickup.R;
import cab.pickup.common.Constants;
import cab.pickup.common.api.Driver;
import cab.pickup.common.api.Event;
import cab.pickup.common.api.Journey;
import cab.pickup.common.api.User;
import cab.pickup.common.server.GetTask;
import cab.pickup.common.server.OnTaskCompletedListener;
import cab.pickup.common.server.Result;
import cab.pickup.ui.activity.MainActivity;
import cab.pickup.ui.activity.RideActivity;

public class GcmIntentService extends IntentService {
    SharedPreferences prefs;
    Journey journey;
    JSONArray eventList = new JSONArray();

    public static final int TYPE_USER_ADDED=10,
                            TYPE_DRIVER_ADDED=11,
                            TYPE_DRIVER_ARRIVED=12,
                            TYPE_USER_CANCELLED=13,
                            TYPE_USER_PICKED=14,
                            TYPE_USER_DROPPED=15;

    static final String TAG = "Intent Service";
    public static final String MSG_REC_INTENT_TAG = "MESSAGE_RECIEVED";
    public static final String JOURNEY_ADD_USER_INTENT_TAG = "JOURNEY_ADD_USER";
    public static final String JOURNEY_ADD_DRIVER_INTENT_TAG = "JOURNEY_ADD_DRIVER";
    public static final String JOURNEY_DRIVER_ARRIVED_INTENT_TAG = "JOURNEY_DRIVER_ARRIVED";
    public static final String JOURNEY_USER_CANCELLED_INTENT_TAG = "JOURNEY_USER_CANCELLED";
    public static final String JOURNEY_USER_PICKED_INTENT_TAG = "JOURNEY_USER_PICKED";
    public static final String JOURNEY_USER_DROPPED_INTENT_TAG = "JOURNEY_USER_DROPPED";

    NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefs=getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        try {
            journey = new Journey(new JSONObject(prefs.getString("journey","")), MyApplication.getDB());

            eventList = new JSONArray(prefs.getString("events",""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
                    int msg_type = msg.getInt("type");
                    if(msg_type==TYPE_USER_CANCELLED || msg_type==TYPE_USER_ADDED){
                        String group_id = journey.group.group_id;
                        new GetTask(this){
                            @Override
                            public void onPostExecute(Result res){
                                super.onPostExecute(res);
                                if(res.statusCode==200){
                                    try {
                                        journey.group.json=res.data.getJSONObject("group");
                                        prefs.edit()
                                                .putString("journey",journey.toString())
                                                .apply();
                                        Log.d("GROUP_UPDATE",res.data.toString());
                                    }catch (Exception E){
                                        E.printStackTrace();
                                    }

                                }
                            }
                        }.execute(Constants.getUrl("/get_group/"+group_id+"?key="+Constants.KEY));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                try {
                    msg = new JSONObject(extras.getString("message"));

                    int msg_type = msg.getInt("type");
                    final JSONObject data = msg.getJSONObject("data");
                    final long time = data.getLong("time");

                    if(!data.getString("journey_id").equals(journey.id))
                        return;

                    if(msg_type==TYPE_USER_ADDED){
                        journey.group.mates.add(0, new User(String.valueOf(data.getInt("user_id")), new OnTaskCompletedListener() {
                            @Override
                            public void onTaskCompleted(Result res) {
                                try {
                                    eventList.put(new JSONObject(new Event(Event.TYPE_USER_ADDED, journey.group.mates.get(0), time).toString()));

                                    sendJourneyUpdate(JOURNEY_ADD_USER_INTENT_TAG, "New mate added!", data.getString("user_name") + " will now ride with you");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, MyApplication.getDB()));
                    } else if(msg_type==TYPE_DRIVER_ADDED){
                        journey.group.driver = new Driver(data.getString("driver_id"), new OnTaskCompletedListener() {
                            @Override
                            public void onTaskCompleted(Result res) {
                                try {
                                    eventList.put(new JSONObject(new Event(Event.TYPE_DRIVER_ADDED, journey.group.driver,time).toString()));

                                    sendJourneyUpdate(JOURNEY_ADD_DRIVER_INTENT_TAG, "Driver allocated!", "Check to see your driver's details");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } else if(msg_type==TYPE_DRIVER_ARRIVED){
                        eventList.put(new JSONObject(new Event(Event.TYPE_DRIVER_ARRIVED, journey.group.driver, time).toString()));

                        sendJourneyUpdate(JOURNEY_DRIVER_ARRIVED_INTENT_TAG,"Your driver is arriving...",
                                "Please reach the pickup location");
                    } else if(msg_type==TYPE_USER_CANCELLED){
                        for(User u: journey.group.mates) {
                            if(u.id.equals(String.valueOf(data.getInt("user_id")))) {
                                eventList.put(new JSONObject(new Event(Event.TYPE_USER_CANCELLED, new User(new JSONObject(u.toString())), time).toString()));
                                journey.group.mates.remove(u);

                                break;
                            }
                        }
                        sendJourneyUpdate(JOURNEY_USER_CANCELLED_INTENT_TAG,"One mate left journey",
                                data.getString("user_name")+" cancelled his journey");
                    }else if(msg_type==TYPE_USER_PICKED){
                        for(User u: journey.group.mates) {
                            if(u.id.equals(String.valueOf(data.getInt("user_id")))) {
                                eventList.put(new JSONObject(new Event(Event.TYPE_USER_PICKED, u, time).toString()));
                                break;
                            }
                        }

                        sendJourneyUpdate(JOURNEY_USER_PICKED_INTENT_TAG,"One mate was picked up",data.getString("user_name")+" was picked up from his location");
                    }else if(msg_type==TYPE_USER_DROPPED){
                        for(User u: journey.group.mates) {
                            if(u.id.equals(String.valueOf(data.getInt("user_id")))) {
                                eventList.put(new JSONObject(new Event(Event.TYPE_USER_DROPPED, u, time).toString()));
                                break;
                            }
                        }
                        sendJourneyUpdate(JOURNEY_USER_DROPPED_INTENT_TAG,"One mate was dropped",data.getString("user_name")+" was dropped at his location");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                Log.i(TAG, "Received: " + extras.getString("message"));
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendJourneyUpdate(String intent_tag, String title, String message){
        prefs.edit()
            .putString("events",eventList.toString())
            .putString("journey",journey.toString())
            .commit();

        Intent i=new Intent(this, RideActivity.class);
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

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());

        Intent broadcast = new Intent(intent_tag);
        broadcast.putExtra("notif_id", 1);
        sendBroadcast(broadcast);
    }
}
