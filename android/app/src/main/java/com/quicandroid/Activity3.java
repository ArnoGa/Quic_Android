package com.quicandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class Activity3 extends AppCompatActivity {

    static {
        System.loadLibrary("quic_android");
    }

    private static final Server[] servers = {
            new Server("ingi", "Cloudflare Quiche"),
            new Server("https://quic.aiortc.org", "aioquic"), new Server("https://pgjones.dev", "aioquic"),
            new Server("https://cloudflare-quic.com", "Cloudflare Quiche"), new Server("https://quic.tech:8443", "Cloudflare Quiche"),
            new Server("https://www.facebook.com", "mvfst"), new Server("https://fb.mvfst.net:4433", "mvfst"),
            new Server("https://quic.rocks:4433", "Google quiche"),
            new Server("https://f5quic.com:4433", "F5"),
            new Server("https://www.litespeedtech.com", "lsquic"),
            new Server("https://nghttp2.org:4433", "ngtcp2"),
            new Server("https://test.privateoctopus.com:4433", "picoquic"),
            new Server("https://h2o.examp1e.net", "h2o/quicly"),
            new Server("https://quic.westus.cloudapp.azure.com", "msquic"),
            new Server("https://docs.trafficserver.apache.org", "Apache Traffic Server")
    };
    private StringBuilder output = new StringBuilder();
    private final QuicRequest g = new QuicRequest();
    private ArrayList<Worker> workers;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
        workers = new ArrayList<>();
    }

    public void launch(View view) {
        // Reset output and workers
        btn = (Button) findViewById(R.id.launch);
        btn.setEnabled(false);
        output.setLength(0);
        ((TextView) findViewById(R.id.stats)).setText("Waiting...");
        workers = new ArrayList<>();

        // Run one worker per server
        for (int i = 0; i < servers.length; i++) {
            Worker task = new Worker();
            workers.add(task);
            task.execute(i);
        }
    }

    private class Worker extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... index) {
            String r = g.sendQuicRequest(servers[index[0]].getUrl());
            if (r.startsWith("Result: [success]")) {
                output.append(String.format("%s -> succeeded", servers[index[0]].toString()));
            }
            else {
                output.append(String.format("%s -> failed", servers[index[0]].toString()));
            }
            output.append("\n\n");
            System.out.println(output.toString());
            return index[0] == servers.length - 1;
        }

        @Override
        protected void onPostExecute(Boolean last) {
            ((TextView) findViewById(R.id.stats)).setText(output.toString());
            if (last) btn.setEnabled(true);
        }
    }

    public void previousScreen(View view) {
        for (Worker worker: workers) {
            worker.cancel(true);
        }
        startActivity(new Intent(Activity3.this, MainActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel running worker(s) to avoid memory leaks
        for (Worker worker: workers) {
            worker.cancel(true);
        }
    }
}