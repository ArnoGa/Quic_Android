package com.quicandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class Activity3 extends AppCompatActivity {

    static {
        System.loadLibrary("quic_android");
    }

    private Server[] servers;
    private final QuicRequest g = new QuicRequest();
    private ArrayList<Worker> workers;
    private Button btn;
    ArrayAdapter<Server> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);

        workers = new ArrayList<>();
        servers = new Server[]{
                new Server("https://linfo2142-grp4.info.ucl.ac.be", "Cloudflare Quiche"),
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

        ListView simpleList = (ListView) findViewById(R.id.simpleListView);
        arrayAdapter = new ArrayAdapter<Server>(Activity3.this, android.R.layout.simple_list_item_1, servers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);

                if (getItem(position).getResult() == 0) {
                    row.setBackgroundColor(Color.TRANSPARENT);
                }
                else if (getItem(position).getResult() == 1) {
                    row.setBackgroundColor(Color.GREEN);
                }
                else {
                    row.setBackgroundColor (Color.RED);
                }
                return row;
            }
        };
        simpleList.setAdapter(arrayAdapter);
    }

    public void launch(View view) {
        // Disable launch button
        btn = (Button) findViewById(R.id.launch);
        btn.setEnabled(false);

        // Reset previous results and background color
        for (Server server : servers) {
            server.setResult(0);
        }

        // Reset workers
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
            System.out.println(r);
            servers[index[0]].setResult(r.startsWith("Result: [success]") ? 1 : 2);
            return index[0] == servers.length - 1;
        }

        @Override
        protected void onPostExecute(Boolean last) {
            arrayAdapter.notifyDataSetChanged();
            if (last) btn.setEnabled(true); // Enable launch button
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