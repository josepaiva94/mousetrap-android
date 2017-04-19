package pt.up.fc.dcc.mousetrap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Main extends Activity {

    ImageView conn_img;
    TextView conn_msg;
    Button btn_retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        conn_img = (ImageView) findViewById(R.id.connecting_img);
        conn_msg = (TextView) findViewById(R.id.connecting_msg);
        btn_retry = (Button) findViewById(R.id.btn_retry);

        connect_try();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connect_try();
    }

    @Override
    public void onBackPressed() {
    }

    void connect_try () {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connect_fail();
            }
        }, 5000);
    }

    void connect_fail () {
        conn_img.setVisibility(View.INVISIBLE);
        conn_msg.setText("Failed to connect!");
        btn_retry.setVisibility(View.VISIBLE);

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry();
            }
        });
    }

    void retry () {
        conn_img.setVisibility(View.VISIBLE);
        conn_msg.setText("Connecting to server...");
        btn_retry.setVisibility(View.INVISIBLE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connect_success();
            }
        }, 5000);
    }

    void connect_success () {
        Intent intent = new Intent(this,Traps.class);
        startActivity(intent);
    }

}


