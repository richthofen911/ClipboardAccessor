package net.callofdroidy.clipboardaccessor;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class ServiceClipboardAccessor extends Service {
    public ServiceClipboardAccessor() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        ((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("label::textFromOutside", intent.getStringExtra("setText")));
        stopSelf();
        return START_NOT_STICKY;
    }
}
