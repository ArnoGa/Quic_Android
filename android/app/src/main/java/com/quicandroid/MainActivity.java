package com.quicandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("quic_android");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendMessage(View view) {
        String res = ((EditText)findViewById(R.id.input)).getText().toString();
        QuicRequest g = new QuicRequest();
        String r = g.sendQuicRequest(res);
        ((TextView)findViewById(R.id.stats)).setText(r);

    }
}