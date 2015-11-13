package net.callofdroidy.clipboardaccessor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.webkit.WebHistoryItem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_IPInfo = (TextView) findViewById(R.id.tv_IPInfo);
        rb_ADB = (RadioButton) findViewById(R.id.rb_adb);
        rb_Internet = (RadioButton) findViewById(R.id.rb_internet);
        rg_transWay = (RadioGroup) findViewById(R.id.rg_transWay);

        rg_transWay.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rb_Internet.getId())
                    startHTTPServer();
            }
        });
    }

    private void startHTTPServer(){
        try{
            myServerForClipboard = new MiniServerApp();
            Log.e("server started", "running on PORT: " + SERVER_PORT);
            tv_IPInfo.setText("This device's IP: " + Formatter.formatIpAddress(((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo().getIpAddress()) + ":" + SERVER_PORT
                    + "\n" + getResources().getString(R.string.connect));
        }catch (IOException e){
            Log.e("failed to start server", e.toString());
        }
    }

    private class MiniServerApp extends NanoHTTPD{
        public MiniServerApp() throws IOException{
            super(SERVER_PORT);
            start();
        }

        @Override
        public Response serve(IHTTPSession session){
            String respContent = "did get any text";
            Map<String, String> params = session.getParms();
            String recvContent = params.get("text");
            if(recvContent != null){
                Log.e("get text", recvContent);
                // need to use a handler
                //((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("label::textForClipboard", recvContent));
                Log.e("set clipboard", recvContent);
                respContent = "Text has been copied on the Clipboard on your phone";
                //Toast.makeText(getApplicationContext(), "Text has been copied on Clipboard now", Toast.LENGTH_SHORT).show();
            }//else
                //Toast.makeText(getApplicationContext(), "Didn't get your text", Toast.LENGTH_SHORT).show();
            return new Response(Response.Status.OK, MIME_PLAINTEXT, respContent);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(myServerForClipboard != null)
            myServerForClipboard.stop();
    }
}
