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
    private static final String[] websites = { "Ingi Server",
                                               "https://quic.aiortc.org", "https://cloudflare-quic.com", "https://www.facebook.com",
                                               "https://quic.rocks:4433", "https://f5quic.com:4433", "https://www.litespeedtech.com",
                                               "https://nghttp2.org:4433", "https://test.privateoctopus.com:4433", "https://h2o.examp1e.net",
                                               "https://quic.westus.cloudapp.azure.com", "https://docs.trafficserver.apache.org/en/latest" };
    private String res = "";
    private final QuicRequest g = new QuicRequest();
    private Worker task;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String>adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, websites);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        if (position == 0) res = "ingi";
        else res = websites[position];
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