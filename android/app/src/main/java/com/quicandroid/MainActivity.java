package com.quicandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
        QuicRequest g = new QuicRequest();
        String r = g.sendQuicRequest(res);
        ((TextView)findViewById(R.id.stats)).setText(r);
    }

    public void nextScreen(View view) {
        startActivity(new Intent(MainActivity.this, Activity2.class));
    }
}