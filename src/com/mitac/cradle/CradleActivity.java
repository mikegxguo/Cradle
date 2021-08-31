package com.mitac.cradle;

import android.Manifest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.UEventObserver;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Array;

public class CradleActivity extends Activity {

    private static String TAG = "SDThread";
    private String external_sdcard_path;
    private StorageManager mStorageManager;
    private Context mContext;
/*
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
*/
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //tipAttach = (TextView) findViewById(R.id.attach);
        //tipDettach = (TextView) findViewById(R.id.dettach);
        //tipBack = (LinearLayout) findViewById(R.id.background);
        Log.d(TAG, "onCreate");
        //m_cradleObserver.startObserving("SUBSYSTEM=switch");
        //requestPermissions(
        //        new String[]{
        //                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        //                Manifest.permission.READ_EXTERNAL_STORAGE,
        //                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
        //        },
        //        0);

        Log.d(TAG, "get SD card path");
        mContext = this;
        try {
            external_sdcard_path = getSDCardPath();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return;
        }
        if (DocumentsUtils.checkWritableRootPath(CradleActivity.this, external_sdcard_path)) {
            showOpenDocumentTree();
        }
        SDThread t = new SDThread(this);
        t.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        //hRefresh.sendEmptyMessage(MSG_REFRESH);
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
        //m_cradleObserver.stopObserving();
    }

    private String getSDCardPath() throws Exception
    {
        StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);

        Class<?> storageVolumeClass = Class.forName("android.os.storage.StorageVolume");
        Method getVolumeList = sm.getClass().getMethod("getVolumeList");
        Method getPath = storageVolumeClass.getMethod("getPath");
        Method isRemovable = storageVolumeClass.getMethod("isRemovable");
        Log.d(TAG, "getMethod OK");
        Object result = getVolumeList.invoke(sm);
        Log.d(TAG, "invoke getVolumeList result:"+result);
        for(int i = 0; i < Array.getLength(result); ++i)
        {
            Object element = Array.get(result, i);
            boolean removable = (Boolean) isRemovable.invoke(element);
            Log.d(TAG, "invoke isRemovable: "+ removable);
            if(removable)
            {
                return (String) getPath.invoke(element);
            }
        }
        Log.d(TAG, "Exception: NO SD card found");

        throw new RuntimeException("no suitable SD path found");
    }

    private void showOpenDocumentTree() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mStorageManager = StorageManager.from(mContext);
            StorageVolume volume = mStorageManager.getStorageVolume(new File(external_sdcard_path));
            if (volume != null) {
                intent = volume.createAccessIntent(null);
            }
        }
        if (intent == null) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        }
        startActivityForResult(intent, DocumentsUtils.OPEN_DOCUMENT_TREE_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == DocumentsUtils.OPEN_DOCUMENT_TREE_CODE) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                DocumentsUtils.saveTreeUri(CradleActivity.this, external_sdcard_path, uri);
            }
            Log.d(TAG, "onActivityResult");
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
