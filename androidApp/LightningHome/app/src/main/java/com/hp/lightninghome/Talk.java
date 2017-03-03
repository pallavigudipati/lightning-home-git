package com.hp.lightninghome;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.skyfishjy.library.RippleBackground;

import java.io.IOException;

public class Talk extends Activity {

    boolean isMicPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        final RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.content);
        ImageView imageView = (ImageView) findViewById(R.id.centerImage);
        imageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println(event.getAction());
                System.out.println("Event no: " + MotionEvent.ACTION_UP);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    rippleBackground.startRippleAnimation();
                    isMicPressed = true;
                    recordSpell();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    rippleBackground.stopRippleAnimation();
                    isMicPressed = false;
                    return true;
                }
                // Event not handled
                return false;
            }
        });
    }

    public void recordSpell() {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setOutputFile("audio_output");
        try {
            recorder.prepare();
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        recorder.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_talk, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
