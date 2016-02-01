package org.genas;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.bluecreation.melodysmart.BLEError;
import com.bluecreation.melodysmart.BondingListener;
import com.bluecreation.melodysmart.DataService;
import com.bluecreation.melodysmart.DeviceDatabase;
import com.bluecreation.melodysmart.DeviceInfoService;
import com.bluecreation.melodysmart.MelodySmartDevice;
import com.bluecreation.melodysmart.MelodySmartListener;
import com.bluecreation.melodysmart.RemoteCommandsService;import java.lang.Override;import java.lang.String;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class BC127CommService extends Service implements MelodySmartListener {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_START = "org.genas.servicetest.action.START";
    public static final String ACTION_DIAL = "org.genas.servicetest.action.DIAL";
    public static final String ACTION_END = "org.genas.servicetest.action.END";
    public static final String ACTION_REJECT = "org.genas.servicetest.action.REJECT";
    public static final String ACTION_PLAY = "org.genas.servicetest.action.PLAY";

    public static final String EXTRA_PARAM1 = "org.genas.servicetest.extra.PARAM1";
    public static final String EXTRA_PARAM2 = "org.genas.servicetest.extra.PARAM2";

    private MelodySmartDevice device;
    private RemoteCommandsService remoteCommandsService;

    private RemoteCommandsService.Listener remotecommandListener = new RemoteCommandsService.Listener() {
        @Override
        public void handleReply(byte[] bytes) {
            Log.i("BC127CommService", "Reply from RemoteCommandService: " + bytes.toString());
        }

        @Override
        public void onConnected(boolean b) {
            Log.i("BC127CommService", "RemoteCommandService connected: " + b);
        }
    };

    private BondingListener bondingListener = new BondingListener() {
        @Override
        public void onBondingStarted() {
            Log.i("BC127CommService", "Bonding started");
        }

        @Override
        public void onBondingFinished(boolean b) {
            Log.i("BC127CommService", "Bonding finished: " + b);
        }
    };

    private DataService.Listener dataServiceListener = new DataService.Listener() {
        @Override
        public void onReceived(byte[] bytes) {
            Log.i("BC127CommService", "DataService received data: " + bytes.toString());
        }

        @Override
        public void onConnected(boolean b) {
            Log.i("BC127CommService", "DataService connected: " + b);
        }
    };

    private DeviceInfoService.Listener deviceInfoServiceListener = new DeviceInfoService.Listener() {
        @Override
        public void onInfoRead(DeviceInfoService.INFO_TYPE info_type, String s) {
            Log.i("BC127CommService", "DeviceInfoService received: " + s);
        }

        @Override
        public void onReadError(int i) {
            Log.i("BC127CommService", "DeviceInfoService read error: " + i);
        }

        @Override
        public void onConnected(boolean b) {
            Log.i("BC127CommService", "DeviceInfoService connected: " + b);
        }
    };

    public static void startBC127(Context context) {
        startAction(context, ACTION_START, null, null);
    }

    /**
     * Starts this service to perform action Dial with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDIAL(Context context, String param1, String param2) {
        startAction(context, ACTION_DIAL, param1, param2);
    }

    public static void startActionEND(Context context, String param1, String param2) {
        startAction(context, ACTION_END, param1, param2);
    }

    public static void startActionREJECT(Context context, String param1, String param2) {
        startAction(context, ACTION_REJECT, param1, param2);
    }

    public static void startActionPLAY(Context context, String param1, String param2) {
        startAction(context, ACTION_PLAY, param1, param2);
    }

    private static void startAction(Context context, final String ACTION, String param1, String param2) {
        Intent intent = new Intent(context, BC127CommService.class);
        intent.setAction(ACTION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    public void onCreate() {

        super.onCreate();
        device = MelodySmartDevice.getInstance();
        device.init(this);
//        device.connect("00:1A:7D:DA:71:09"); // Odroid-U3
        device.registerListener(this);
        device.registerListener(bondingListener);
        device.getDataService().registerListener(dataServiceListener);
        device.getDeviceInfoService().registerListener(deviceInfoServiceListener);
        device.connect("20:FA:BB:02:1B:9B"); // Jamboree Purpletooth
        remoteCommandsService = device.getRemoteCommandsService();
        remoteCommandsService.registerListener(remotecommandListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        remoteCommandsService.unregisterListener(remotecommandListener);
        device.getDeviceInfoService().unregisterListener(deviceInfoServiceListener);
        device.getDataService().unregisterListener(dataServiceListener);
        device.unregisterListener(bondingListener);
        device.unregisterListener(this);
        device.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            final String action = intent.getAction();
            final String param1 = intent.getStringExtra(EXTRA_PARAM1);
            final String param2 = intent.getStringExtra(EXTRA_PARAM2);
            switch (action) {
                case ACTION_DIAL:
                    remoteCommandsService.send("CALL " + param1);
                    break;
                case ACTION_END:
                    remoteCommandsService.send("END");
                    break;
                case ACTION_START:
                    Log.d("BC127CommService", "Started service");
                    break;
            }
        }
        return result;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDeviceConnected() {
        Log.i(this.getClass().getName(), "Device connected");
    }

    @Override
    public void onDeviceDisconnected(BLEError bleError) {
        Log.i(this.getClass().getName(), "Device disconnected: " + bleError.toString());
    }

    @Override
    public void onOtauAvailable() {

    }

    @Override
    public void onOtauRecovery(DeviceDatabase.DeviceData deviceData) {

    }
}


