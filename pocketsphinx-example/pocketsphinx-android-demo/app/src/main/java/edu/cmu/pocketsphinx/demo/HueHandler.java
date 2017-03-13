package edu.cmu.pocketsphinx.demo;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;

/**
 * Created by pallavigudipati on 05/03/17.
 */

public class HueHandler {

    public PHHueSDK phHueSDK;
    public PHBridge phBridge;
    public PHLight phLight;

    private static final String APP_NAME = "HPHomeApp";

    // TODO: We should actually let the user choose one.
    // TODO: That code can be found in the demo app for Hue
    private static final String IP_ADDRESS = "192.168.1.16";
    private static final String USERNAME = "UY0J1uAWDCOtEjLwoewsTm4JUCgycrtKoUdON69j";
    private static final String TAG = "HPHome";

    // private HueSharedPreferences prefs;
    // private AccessPointListAdapter adapter;

    private boolean lastSearchWasIPScan = false;

    public static void main(String[] args) {
        HueHandler handler = new HueHandler();
       // handler.setColor("#636161");
    }

    // Local SDK Listener
    private PHSDKListener listener = new PHSDKListener() {
        @Override
        public void onAccessPointsFound(List<PHAccessPoint> accessPoint) {
            if (accessPoint != null && accessPoint.size() > 0) {
                phHueSDK.getAccessPointsFound().clear();
                phHueSDK.getAccessPointsFound().addAll(accessPoint);
            }
        }

        @Override
        public void onCacheUpdated(List<Integer> arg0, PHBridge bridge) {}

        @Override
        public void onBridgeConnected(PHBridge b, String username) {
            phHueSDK.setSelectedBridge(b);
            phHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
            phHueSDK.getLastHeartbeat().put(b.getResourceCache().getBridgeConfiguration().getIpAddress(),
                    System.currentTimeMillis());

            phBridge = b;
            List<PHLight> allLights = phBridge.getResourceCache().getAllLights();
            // TODO: Generalize for all lights. Currently have money only for one :P
            // TODO: MainActivity should checkup on phLight before displaying "cast"
            phLight = allLights.get(0);
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
            System.out.println("Authentication required");
            phHueSDK.startPushlinkAuthentication(accessPoint);
        }

        @Override
        public void onConnectionResumed(PHBridge bridge) {
            phHueSDK.getLastHeartbeat().put(bridge.getResourceCache().getBridgeConfiguration().getIpAddress(),
                    System.currentTimeMillis());
            for (int i = 0; i < phHueSDK.getDisconnectedAccessPoint().size(); i++) {
                if (phHueSDK.getDisconnectedAccessPoint().get(i).getIpAddress()
                        .equals(bridge.getResourceCache().getBridgeConfiguration().getIpAddress())) {
                    phHueSDK.getDisconnectedAccessPoint().remove(i);
                }
            }

        }

        @Override
        public void onConnectionLost(PHAccessPoint accessPoint) {
            if (!phHueSDK.getDisconnectedAccessPoint().contains(accessPoint)) {
                phHueSDK.getDisconnectedAccessPoint().add(accessPoint);
            }
        }

        @Override
        public void onError(int code, final String message) {
            Log.e(TAG, "on Error Called : " + code + ":" + message);

            if (code == PHHueError.NO_CONNECTION) {
                Log.w(TAG, "On No Connection");
            } else if (code == PHHueError.AUTHENTICATION_FAILED ||
                    code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
                Log.w(TAG, "Authentication failed");
            } else if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
                Log.w(TAG, "Bridge Not Responding . . . ");
            }
            else if (code == PHMessageType.BRIDGE_NOT_FOUND) {
                Log.w(TAG, "Bridge not found");
            } else {
                Log.w(TAG, "Unknown error: " + message);
            }
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> parsingErrorsList) {
            for (PHHueParsingError parsingError: parsingErrorsList) {
                Log.e(TAG, "ParsingError : " + parsingError.getMessage());
            }
        }
    };

    HueHandler() {
        // Gets an instance of the Hue SDK.
        phHueSDK = PHHueSDK.create();

        // Set the Device Name (name of your app). This will be stored in your bridge whitelist entry.
        phHueSDK.setAppName(APP_NAME);
        // phHueSDK.setDeviceName(android.os.Build.MODEL);
        phHueSDK.setDeviceName(APP_NAME);

        // Register the PHSDKListener to receive callbacks from the bridge.
        phHueSDK.getNotificationManager().registerSDKListener(listener);

       // adapter = new AccessPointListAdapter(getApplicationContext(), phHueSDK.getAccessPointsFound());

       // ListView accessPointList = (ListView) findViewById(R.id.bridge_list);
       //// accessPointList.setOnItemClickListener(this);
        // accessPointList.setAdapter(adapter);

        // Try to automatically connect to the last known bridge.  For first time use this will be empty so a bridge search is automatically started.
        // prefs = HueSharedPreferences.getInstance(getApplicationContext());
       // String lastIpAddress = prefs.getLastConnectedIPAddress();
        // TODO: What to do? I think we can get this in the getting started thing
        // String lastUsername = prefs.getUsername();

        // Automatically try to connect to the last connected IP Address.  For multiple bridge support a different implementation is required.
       // if (lastIpAddress != null && !lastIpAddress.equals("")) {

        PHAccessPoint accessPoint = new PHAccessPoint();
        accessPoint.setIpAddress(IP_ADDRESS);
        accessPoint.setUsername(USERNAME);

        if (!phHueSDK.isAccessPointConnected(accessPoint)) {
            // No need .. we already display "drawing magic ..."
            //  PHWizardAlertDialog.getInstance().showProgressDialog(R.string.connecting, PHHomeActivity.this);
            phHueSDK.connect(accessPoint);
        }

        // TODO: this goes to the callback function
        // phBridge = phHueSDK.getSelectedBridge();
        // List<PHLight> allLights = phBridge.getResourceCache().getAllLights();

        // TODO: Generalize for all lights. Currently have money only for one :P
        // phLight = allLights.get(0);
    }

    public void setColor(String hexColor) {
        PHLightState lightState = new PHLightState();
        float[] xy = rgbToXy(hexColor);
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        // Packing listener for now.
        phBridge.updateLightState(phLight, lightState);
    }

    public static float[] rgbToXy(String hexColor) {
        System.out.println(hexColor);
        int rgbColor = Color.parseColor(hexColor);
        // int rgbColor = 0xFFFF0000;
        System.out.println(rgbColor);
        return PHUtilities.calculateXY(rgbColor, Build.MODEL);
    }

    public void wrapUp() {
        if (phBridge != null) {
            if (phHueSDK.isHeartbeatEnabled(phBridge)) {
                phHueSDK.disableHeartbeat(phBridge);
            }
            phHueSDK.disconnect(phBridge);
        }
    }
}
