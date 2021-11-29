package com.quicandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static {
        System.loadLibrary("quic_android");
    }

    private Spinner spinner;
    private static final Server[] servers = {
            new Server("Ingi Server", "Cloudflare Quiche"),
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

    private String res = "";
    private final QuicRequest g = new QuicRequest();
    private Worker task;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<Server>adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, servers);

        adapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        if (position == 0) res = "ingi";
        else res = servers[position].getUrl();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Auto-generated method
    }

    public void sendMessage(View view) {
        btn = (Button) findViewById(R.id.button);
        btn.setEnabled(false);
        ((TextView) findViewById(R.id.stats)).setText("Waiting...");
        task = new Worker();
        task.execute(res);
    }

    private class Worker extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... res) {
            String r = g.sendQuicRequest(res[0]);
            System.out.println(r);
            return r;
        }

        @Override
        protected void onPostExecute(String result) {
            ((TextView) findViewById(R.id.stats)).setText(result);
            btn.setEnabled(true);
        }
    }

    public void nextScreen(View view) {
        if (task != null) task.cancel(true);
        startActivity(new Intent(MainActivity.this, Activity2.class));
    }

    public void previousScreen(View view) {
        if (task != null) task.cancel(true);
        startActivity(new Intent(MainActivity.this, Activity3.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel running worker to avoid memory leaks
        if (task != null) task.cancel(true);
    }
}