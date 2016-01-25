package net.callofdroidy.clipboardaccessor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

public class ServiceClipboardAccessor extends Service{
    String ACTION_GET = "clipper.get";
    String ACTION_SET = "clipper.set";
    String EXTRA_TEXT = "text";

    public ServiceClipboardAccessor() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        // register receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("clipper.get");
        intentFilter.addAction("clipper.set");
        registerReceiver(myReceiver, intentFilter);

        return START_NOT_STICKY;
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver(){
        public boolean isActionGet(final String action) {
            return ACTION_GET.equals(action);
        }
        public boolean isActionSet(final String action) {
            return ACTION_SET.equals(action);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ClipboardManager cb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (isActionSet(intent.getAction())) {
                String text = intent.getStringExtra(EXTRA_TEXT);
                if (text != null) {
                    ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("label::textForClipboard", text));
                    setResultData("Text is copied into clipboard.");
                } else {
                    setResultData("No text is provided. Use -e text \"text to be pasted\"");
                }
            } else if (isActionGet(intent.getAction())) {
                String content;
                content = cb.getPrimaryClip().getItemAt(0).getText().toString();
                Log.e("from phone", content);
                setResultData(content);
            }
            stopSelf();
        }
    };

    @Override
    public void onDestroy(){
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }
}
