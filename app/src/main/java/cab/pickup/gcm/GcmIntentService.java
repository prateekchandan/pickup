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

import cab.pickup.JourneyActivity;
import cab.pickup.R;

public class GcmIntentService extends IntentService {

    public static final int TYPE_NO_JOURNEY=0,
                            TYPE_COMMON_JOURNEY=1;

    static final String TAG = "Intent Service";
    public static final String MSG_REC_INTENT_TAG = "MESSAGE_RECIEVED";

    public GcmIntentService() {
        super("GcmIntentService");
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

                    if(msg_type==TYPE_COMMON_JOURNEY){
                        sendNotification(msg.getString("name") + " sent request", msg.get("journey_id").toString());
                    } else if(msg_type==TYPE_NO_JOURNEY){
                        sendNotification("No common journey :(", msg.get("journey_id").toString());
                    }

                    Log.i(TAG, msg.getString("name"));
                    Log.i(TAG, msg.get("journey_id").toString());
                    Log.i(TAG, msg.get("type").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.i(TAG, "Received: " + extras.getString("message"));

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg, String id) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i=new Intent(this, JourneyActivity.class);
        i.putExtra("journey_id",id);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                i, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Journey matched")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void sendMsg(String msg){
        Intent i = new Intent(MSG_REC_INTENT_TAG);

        i.putExtra("message", msg);

        sendBroadcast(i);
    }
}
