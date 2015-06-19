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
    int cntAttach;
    int cntDettach;

    private static final int MSG_ATTACH = 0x1245;
    private static final int MSG_DETTACH = 0x1246;

    private Handler hRefresh = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_ATTACH:
                if (tipAttach != null) {
                    tipAttach.setText("Attach: " + cntAttach);
                }
                break;
            case MSG_DETTACH:
                if (tipDettach != null) {
                    tipDettach.setText("Dettach: " + cntDettach);
                }
                if (tipBack != null) {
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
            boolean bCradle = "cradle".equals(event.get("external")) ? true
                    : false;
            if (bCradle) {
                String status = event.get("status");
                if ("attached".equals(status)) {
                    Log.i(TAG, "Cradle is attached.");
                    cntAttach += 1;
                    hRefresh.sendEmptyMessage(MSG_ATTACH);
                } else if ("dettached".equals(status)) {
                    Log.i(TAG, "Cradle is dettached.");
                    cntDettach += 1;
                    hRefresh.sendEmptyMessage(MSG_DETTACH);
                }
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
        cntAttach = 0;
        cntDettach = 0;

        m_cradleObserver.startObserving("SUBSYSTEM=switch");
    }

}
