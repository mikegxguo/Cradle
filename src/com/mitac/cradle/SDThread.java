package com.mitac.cradle;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Random;
import java.lang.reflect.Array;

public class SDThread extends Thread {
    private String TAG = "SDThread";

    SDThread(Context c)
    {
        context = c;
    }

    @Override
    public void run() {
        int i = 0;
        try {
            String baseDirectory = getSDCardPath();
            String middleDirectory = "Android/data/" + context.getApplicationContext().getPackageName();
            Random rd = new Random();
            int fileSize = 10 * 1024 * 1024;
            byte[] outputBuffer = new byte[fileSize];
            rd.nextBytes(outputBuffer);

            for(; true; ++i) {
                String writeFilename = "SD_stress_test_" + Long.toString(this.getId()) + "_" + Integer.toString(i) + ".data";

                File writeF = new File(new File(baseDirectory, middleDirectory), writeFilename);
                Log.d(TAG, "file: "+writeF);
                boolean ret = writeF.getParentFile().mkdirs();
                Log.d(TAG, "mkdirs return: "+ret);
                if(!writeF.isFile()){
                    boolean ret2 = writeF.createNewFile();
                    Log.d(TAG, "create New File, return: "+ ret2);
                }
                FileOutputStream output = new FileOutputStream(writeF);

                output.write(outputBuffer);
                output.flush();
                output.close();

            }
        } catch (Exception e) {
            error = true;
            exception = e;
            Log.e(TAG, e.toString());
        }
    }

    private String getSDCardPath() throws Exception
    {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        Class<?> storageVolumeClass = Class.forName("android.os.storage.StorageVolume");
        Method getVolumeList = sm.getClass().getMethod("getVolumeList");
        Method getPath = storageVolumeClass.getMethod("getPath");
        Method isRemovable = storageVolumeClass.getMethod("isRemovable");
        Object result = getVolumeList.invoke(sm);
        for(int i = 0; i < Array.getLength(result); ++i)
        {
            Object element = Array.get(result, i);
            boolean removable = (Boolean) isRemovable.invoke(element);
            if(removable)
            {
                return (String) getPath.invoke(element);
            }
        }

        throw new RuntimeException("no suitable SD path found");
    }

    private Context context;

    public boolean error = false;
    public Exception exception;
}
