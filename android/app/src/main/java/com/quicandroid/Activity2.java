package com.quicandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Activity2 extends AppCompatActivity {

    static {
        System.loadLibrary("quic_android");
    }

    private final QuicRequest g = new QuicRequest();
    private Worker task;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
    }

    public void sendMessage(View view) {
        btn = (Button) findViewById(R.id.button);
        btn.setEnabled(false);
        String res = ((EditText)findViewById(R.id.input)).getText().toString();
        ((TextView) findViewById(R.id.stats)).setText("Waiting...");
        task = new Worker();
        task.execute(res);
    }

    private class Worker extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... res) {
            String r = g.sendQuicRequest(res[0]);
            return r;
        }

        @Override
        protected void onPostExecute(String result) {
            ((TextView) findViewById(R.id.stats)).setText(result);
            btn.setEnabled(true);
        }
    }

    public void previousScreen(View view) {
        if (task != null) task.cancel(true);
        startActivity(new Intent(Activity2.this, MainActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel running worker to avoid memory leaks
        if (task != null) task.cancel(true);
    }
}