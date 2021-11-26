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

    private static final String[] websites = { "ingi",
            "https://quic.aiortc.org", "https://cloudflare-quic.com", "https://www.facebook.com",
            "https://quic.rocks:4433", "https://f5quic.com:4433", "https://www.litespeedtech.com",
            "https://nghttp2.org:4433", "https://test.privateoctopus.com:4433", "https://h2o.examp1e.net",
            "https://quic.westus.cloudapp.azure.com", "https://docs.trafficserver.apache.org/en/latest"
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

        // Run one worker per website
        for (int i = 0; i < websites.length; i++) {
            Worker task = new Worker();
            workers.add(task);
            task.execute(i);
        }
    }

    private class Worker extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... index) {
            String r = g.sendQuicRequest(websites[index[0]]);
            output.append(r);
            output.append("\n\n");
            System.out.println(output.toString());
            return index[0] == websites.length - 1;
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