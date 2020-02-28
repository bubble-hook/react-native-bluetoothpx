package xyz.xbprinter;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.List;
import java.util.Map;

import com.facebook.react.bridge.Callback;

public class CBBluetoothpxModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private BTBroadcastReceiver mReceiver;

    public CBBluetoothpxModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    @Override
    public String getName() {
        return "CBBluetoothpx";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    @ReactMethod
    public void startDetect(Callback callback) {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.mReceiver = new BTBroadcastReceiver(this.reactContext);
        registerReceiver(this.mReceiver, filter);
        callback.invoke("init success");
    }
}



private class BTBroadcastReceiver extends BroadcastReceiver {

    ReactApplicationContext reactContext;

    Map<string,string> devices = new Map<string,string>();

    public CBBluetoothpxModule(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Discovery has found a device. Get the BluetoothDevice
            // object and its info from the Intent.
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress(); // MAC address
            this.devices[deviceHardwareAddress] = deviceName;
            this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("btDetect", this.devices);
        }
    }

    
}
