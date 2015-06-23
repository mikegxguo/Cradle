package com.mitac.cradle;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.UEventObserver;
import android.util.Log;

public class CradleActivity extends Activity {

    private static String TAG = "CradleActivity";

    TextView tipAttach;
    TextView tipDettach;
    LinearLayout tipBack;
    private static int cntAttach = 0;
    private static int cntDettach = 0;

    private static final int MSG_REFRESH = 0x1245;

    private Handler hRefresh = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REFRESH:
                if (tipAttach != null) {
                    tipAttach.setText("Attach: " + cntAttach);
                }
                if (tipDettach != null) {
                    tipDettach.setText("Dettach: " + cntDettach);
                }
                if (tipBack != null && cntDettach>0) {
                    tipBack.setBackgroundColor(Color.RED);
                }
                break;
            default:
                break;
            }
        }
    };

    private UEventObserver m_cradleObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEvent event) {
            Log.d(TAG, "Event: " + event);
            boolean bCradle = "dock".equals(event.get("SWITCH_NAME")) ? true
                    : false;
            if (bCradle) {
                String status = event.get("SWITCH_STATE");
                if ("1".equals(status)) {
                    Log.d(TAG, "Cradle is attached.");
                    cntAttach += 1;
                } else if ("0".equals(status)) {
                    Log.d(TAG, "Cradle is dettached.");
                    cntDettach += 1;
                }
                hRefresh.sendEmptyMessage(MSG_REFRESH);
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tipAttach = (TextView) findViewById(R.id.attach);
        tipDettach = (TextView) findViewById(R.id.dettach);
        tipBack = (LinearLayout) findViewById(R.id.background);
        Log.d(TAG, "onCreate");
        m_cradleObserver.startObserving("SUBSYSTEM=switch");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        hRefresh.sendEmptyMessage(MSG_REFRESH);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        m_cradleObserver.stopObserving();
    }
}
