package cab.pickup.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cab.pickup.ui.activity.MyActivity;

public class PeerDetector implements WifiP2pManager.PeerListListener {
    private static final String TAG = "PeerDetector";
    private final MyActivity context;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    ScheduledExecutorService scheduler;
    ScheduledFuture<?> scheduledTask;


    public PeerDetector(MyActivity context) {
        this.context = context;

        mManager = (WifiP2pManager) context.getSystemService(MyActivity.WIFI_P2P_SERVICE);

        mChannel = mManager.initialize(context, context.getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        for(WifiP2pDevice device : peers.getDeviceList()){
            Toast.makeText(context, "Found:"+device.deviceName, Toast.LENGTH_LONG).show();

            Log.d(TAG, "Device added : " + device.deviceName);
        }
    }

    public void discover(){
        Log.d(TAG, "Discovering");

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //do nothing
            }

            @Override
            public void onFailure(int reasonCode) {
                String toast="";
                switch (reasonCode){
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        toast="Peer-2-Peer not supported in your phone";
                        break;
                    case WifiP2pManager.ERROR:
                        toast="Error in discovering devices";
                        break;
                    case WifiP2pManager.BUSY:
                        toast="WiFi channel is busy, Try again later";
                        break;
                }

                Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void start(){
        Log.d(TAG, "Peer discovery started");

        context.registerReceiver(mReceiver, mIntentFilter);

        scheduledTask = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                discover();
            }
        }, 0, 2, TimeUnit.MINUTES);
    }

    public void stop(){
        Log.d(TAG, "Peer discovery stopped");

        scheduledTask.cancel(false);

        context.unregisterReceiver(mReceiver);
    }

    public class WifiBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager manager;
        private WifiP2pManager.Channel channel;

        public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel) {
            super();
            this.manager = manager;
            this.channel = channel;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d("Receiver", action);
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // Wifi P2P is enabled
                } else {
                    // Wi-Fi P2P is not enabled
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if (manager != null) {
                    manager.requestPeers(channel, PeerDetector.this);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }
        }
    }
}
