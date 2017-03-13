/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package edu.cmu.pocketsphinx.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.skyfishjy.library.RippleBackground;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class PocketSphinxActivity extends Activity implements
        RecognitionListener {

    private static final String HP_HOME = "hphome";
    private static final String SPELL_COLORS_FILE = "spell_color.txt";

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private Map<String, String> spellsColors;
    private SpeechRecognizer recognizer;
//    private HueHandler hueHandler;

    public PHHueSDK phHueSDK;
    public PHBridge phBridge;
    public PHLight phLight;
    public static final String APP_NAME = "HPHomeApp";
    public static final String TAG = "HPHomeApp";
    // TODO: We should actually let the user choose one.
    // TODO: That code can be found in the demo app for Hue
    public static final String IP_ADDRESS = "192.168.1.16";
    public static final String USERNAME = "UY0J1uAWDCOtEjLwoewsTm4JUCgycrtKoUdON69j";

    public PHSDKListener listener = new PHSDKListener() {
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

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.main);
       // ((TextView) findViewById(R.id.caption_text)).setText("Preparing the recognizer");

        final RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.content);

        final TextView display = (TextView) findViewById(R.id.display);
        Typeface quickSandFont = Typeface.createFromAsset(getAssets(),
                "fonts/Quicksand-Regular.otf");
        display.setTypeface(quickSandFont);
        display.setText("drawing magic ...");


        /******
        *******/
//        public static final String IP_ADDRESS = "192.168.1.16";
//        http://192.168.1.16/debug/clip.html
//        public static final String USERNAME = "UY0J1uAWDCOtEjLwoewsTm4JUCgycrtKoUdON69j";
//        try {
//            URL url = new URL("http://192.168.1.16/api/UY0J1uAWDCOtEjLwoewsTm4JUCgycrtKoUdON69j/lights/2/state");
//            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
//            httpCon.setDoOutput(true);
//            httpCon.setRequestMethod("PUT");
//            OutputStreamWriter out = new OutputStreamWriter(
//                    httpCon.getOutputStream());
//            out.write("{\"on\":false}");
//            out.close();
//        } catch (Exception e) {
//            System.out.println("WTF");
//        }


        // hueHandler = new HueHandler();
        phHueSDK = PHHueSDK.create();

        // Set the Device Name (name of your app). This will be stored in your bridge whitelist entry.
        phHueSDK.setAppName(APP_NAME);
        // phHueSDK.setDeviceName(android.os.Build.MODEL);
        phHueSDK.setDeviceName(APP_NAME);

        // Register the PHSDKListener to receive callbacks from the bridge.
        phHueSDK.getNotificationManager().registerSDKListener(listener);

        PHAccessPoint accessPoint = new PHAccessPoint();
        accessPoint.setIpAddress(IP_ADDRESS);
        accessPoint.setUsername(USERNAME);

        if (!phHueSDK.isAccessPointConnected(accessPoint)) {
            // No need .. we already display "drawing magic ..."
            //  PHWizardAlertDialog.getInstance().showProgressDialog(R.string.connecting, PHHomeActivity.this);
            phHueSDK.connect(accessPoint);
        }

        loadColors();

        ImageView micButton = (ImageView) findViewById(R.id.micButton);

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    rippleBackground.startRippleAnimation();
                    recognizer.startListening(HP_HOME);
                    display.setText("");
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    recognizer.stop();
                    rippleBackground.stopRippleAnimation();
                    return true;
                }
                // Event not handled
                return false;
            }
        });

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        runRecognizerSetup();
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(PocketSphinxActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.display))
                            .setText("Failed to init recognizer " + result);
                } else {
                    // HueHandler might not be done
                    // TODO: better way of doing this
                    boolean flag = true;
                  //  while (flag) {
                   ///     if (hueHandler.phLight != null) {
                        //    System.out.println(hueHandler.phLight);
                            flag = false;
                      //  }
                    //}
                    ((TextView) findViewById(R.id.display)).setText("cast");
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }

        // hueHandler.wrapUp();
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr();
        System.out.println(text);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            System.out.println("onResult: " + text);
            TextView display = (TextView) findViewById(R.id.display);
            if (!spellsColors.containsKey(text)) {
                display.setText("cast again");
            } else {
                display.setText(text);
                // hueHandler.setColor(spellsColors.get(text));
                PHLightState lightState = new PHLightState();
                float[] xy = HueHandler.rgbToXy(spellsColors.get(text));
                lightState.setX(xy[0]);
                lightState.setY(xy[1]);
                // Packing listener for now.
                phBridge.updateLightState(phLight, lightState, new PHLightListener() {
                    @Override
                    public void onReceivingLightDetails(PHLight phLight) {

                    }

                    @Override
                    public void onReceivingLights(List<PHBridgeResource> list) {

                    }

                    @Override
                    public void onSearchComplete() {

                    }

                    @Override
                    public void onSuccess() {
                        System.out.println("Success");
                    }

                    @Override
                    public void onError(int i, String s) {
                        System.out.println("Error");
                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
                        System.out.println("update");
                    }
                });
                phBridge.updateLightState(phLight, lightState);
            }
        }
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "spells.dict"))
               // .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .getRecognizer();
        recognizer.addListener(this);

        // TODO: can be used?
        // Create keyword-activation search.
        // recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File spellGrammar = new File(assetsDir, "hpgrammar.gram");
        recognizer.addGrammarSearch(HP_HOME, spellGrammar);
    }

    @Override
    public void onBeginningOfSpeech() {}

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(Exception error) {}

    @Override
    public void onTimeout() {}

    // Loads spell-color mapping
    private void loadColors() {
        spellsColors = new HashMap<>();
        try {
            InputStream is = getAssets().open(SPELL_COLORS_FILE);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!(line.charAt(0) == '#')) {
                    String[] spellColor = line.split(";");
                    spellsColors.put(spellColor[0], spellColor[1]);
                }
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
