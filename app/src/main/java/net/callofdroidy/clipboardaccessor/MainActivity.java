package net.callofdroidy.clipboardaccessor;

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
    private static final int SERVER_PORT = 6000;

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
                Log.e("checked changed", "checked id " + checkedId);
                Log.e("rb_internet id", rb_Internet.getId() + "");
                if (checkedId == rb_Internet.getId()) {
                    tv_IPInfo.setText("This device's IP: " + Formatter.formatIpAddress(((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo().getIpAddress()) + ":6000");
                    startHTTPServer();
                }
            }
        });
    }

    private void startHTTPServer(){
        try{
            new MiniServerApp();
            Log.e("server is running", "PORT: 6000");
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
            Log.e("session info", session.getQueryParameterString());
            String respContent = "did get any text";
            Map<String, String> params = session.getParms();
            String recvContent = params.get("text");
            if(recvContent != null){
                Log.e("receive text", recvContent);
                respContent = "successfully get content " + recvContent;
            }
            return new Response(Response.Status.OK, MIME_PLAINTEXT, respContent);
        }
    }
}
