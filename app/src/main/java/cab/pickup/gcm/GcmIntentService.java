package cab.pickup.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import cab.pickup.R;
import cab.pickup.ui.activity.MainActivity;
import cab.pickup.ui.activity.RideActivity;

public class GcmIntentService extends IntentService {

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
                    JSONObject data = msg.getJSONObject("data");

                    if(msg_type==TYPE_USER_ADDED){
                        sendJourneyUpdate(JOURNEY_ADD_USER_INTENT_TAG, data.getString("user_id"),
                                "New mate added!",
                                data.getString("user_name")+" will now ride with you");
                    } else if(msg_type==TYPE_DRIVER_ADDED){
                        sendJourneyUpdate(JOURNEY_ADD_DRIVER_INTENT_TAG, data.getString("driver_id"),
                                "Driver allocated!",
                                "Check to see your driver's details");
                    } else if(msg_type==TYPE_DRIVER_ARRIVED){
                        sendJourneyUpdate(JOURNEY_DRIVER_ARRIVED_INTENT_TAG, data.getString("driver_id"),
                                "Your driver is arriving...",
                                "Please reach the pickup location");
                    } else if(msg_type==TYPE_USER_CANCELLED){
                        sendJourneyUpdate(JOURNEY_USER_CANCELLED_INTENT_TAG, data.getString("user_id"),
                                "One mate left journey",
                                data.getString("user_name")+" cancelled his journey");
                    }else if(msg_type==TYPE_USER_PICKED){
                        sendJourneyUpdate(JOURNEY_USER_PICKED_INTENT_TAG, data.getString("user_id"),
                                "One mate was picked up",
                                data.getString("user_name")+" was picked up from his location");
                    }else if(msg_type==TYPE_USER_DROPPED){
                        sendJourneyUpdate(JOURNEY_USER_DROPPED_INTENT_TAG, data.getString("user_id"),
                                "One mate was dropped",
                                data.getString("user_name")+" was dropped at his location");
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

    private void sendJourneyUpdate(String intent_tag, String user_id, String title, String message){
        Intent i=new Intent(this, RideActivity.class);
        i.putExtra("action", intent_tag);
        i.putExtra("id", user_id);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                i, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(user_id.hashCode(), mBuilder.build());

        Intent broadcast = new Intent(intent_tag);
        broadcast.putExtra("id",user_id);
        broadcast.putExtra("notif_id",user_id.hashCode());
        sendBroadcast(broadcast);
    }
}
