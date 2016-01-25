package net.callofdroidy.clipboardaccessor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {

    private RadioGroup rg_transWay;
    private RadioButton rb_ADB;
    private RadioButton rb_Internet;
    private TextView tv_IPInfo;
    private static final int SERVER_PORT = 8000;
    private MiniServerApp myServerForClipboard;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_IPInfo = (TextView) findViewById(R.id.tv_how_to_use);
        tv_IPInfo.setText(R.string.how_to_use_ADB_Mode);
        rb_ADB = (RadioButton) findViewById(R.id.rb_adb);
        rb_Internet = (RadioButton) findViewById(R.id.rb_internet);
        rg_transWay = (RadioGroup) findViewById(R.id.rg_transWay);

        rg_transWay.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rb_Internet.getId())
                    startHTTPServer(mHandler);
                else {
                    stopHTTPServer();
                    tv_IPInfo.setText(R.string.how_to_use_ADB_Mode);
                }
            }
        });

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case Constants.MESSAGE_RESULT_SUCCESS:
                        ((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("label::textForClipboard", msg.getData().getString("text")));
                        Toast.makeText(getApplicationContext(), "Text has been copied on the clipboard now", Toast.LENGTH_SHORT).show();
                        break;
                    case Constants.MESSAGE_RESULT_FAILURE:
                        Toast.makeText(getApplicationContext(), "Didn't get your text", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    private void startHTTPServer(Handler serverHandler){
        try{
            myServerForClipboard = new MiniServerApp(serverHandler);
            Log.e("server status", "started, running on PORT: " + SERVER_PORT);
            tv_IPInfo.setText("This device's IP: " + Formatter.formatIpAddress(((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo().getIpAddress()) + ":" + SERVER_PORT
                    + "\n" + getResources().getString(R.string.how_to_use_Internet_Mode));
        }catch (IOException e){
            Log.e("failed to start server", e.toString());
        }
    }

    private void stopHTTPServer(){
        if(myServerForClipboard != null){
            myServerForClipboard.stop();
            Log.e("server status", "stopped");
        }
    }

    private class MiniServerApp extends NanoHTTPD{
        private Handler resultHandler;
        public MiniServerApp(Handler handler) throws IOException{
            super(SERVER_PORT);
            resultHandler = handler;
            start();
        }

        @Override
        public Response serve(IHTTPSession session){
            Log.e("get a req", session.getQueryParameterString());
            String contentResp = "didn't get any text";
            Map<String, String> params = session.getParms();
            String contentRecv = params.get("text");
            Log.e("content recv", contentRecv + "");
            Message msg;
            if(contentRecv != null){
                Log.e("get text", contentRecv);
                // need to use a handler
                msg = resultHandler.obtainMessage(Constants.MESSAGE_RESULT_SUCCESS);
                Bundle bundle = new Bundle();
                bundle.putString("text", contentRecv);
                msg.setData(bundle);
                Log.e("set clipboard", "success, content: " + contentRecv);
                contentResp = "Text has been copied on the Clipboard on your phone";
            }else{
                msg = resultHandler.obtainMessage(Constants.MESSAGE_RESULT_FAILURE);
                Log.e("set clipboard", "failed");
            }
            mHandler.sendMessage(msg);

            return new Response(Response.Status.OK, MIME_HTML, contentResp);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(myServerForClipboard != null)
            myServerForClipboard.stop();
    }
}
