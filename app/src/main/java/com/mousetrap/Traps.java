package com.mousetrap;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Traps extends Activity {


    LinearLayout trap1;
    ImageView trap1_status;
    ImageView trap1_img;
    LinearLayout trap2;
    ImageView trap2_status;
    ImageView trap2_img;

    Handler handler = new Handler();
    Runnable update = new Runnable () {
        @Override
        public void run () {
            //while(true) {
                update_traps();
            //}
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traps);

        trap1 = (LinearLayout) findViewById(R.id.trap1);
        trap1_status = (ImageView) findViewById(R.id.trap1_status);
        trap1_img = (ImageView) findViewById(R.id.trap1_img);
        trap2 = (LinearLayout) findViewById(R.id.trap2);
        trap2_status = (ImageView) findViewById(R.id.trap2_status);
        trap2_img = (ImageView) findViewById(R.id.trap2_img);

        trap1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go_trap(1);
            }
        });
        trap2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go_trap(2);
            }
        });


        // http://stackoverflow.com/questions/9812107/how-do-you-loop-a-thread
        update_traps();

    }

    @Override
    public void onBackPressed() {
    }


    void go_trap (int i) {
        Intent intent = new Intent(this,Trap.class);
        intent.putExtra("trap", "Trap "+i);
        startActivity(intent);
    }

    void update_traps () {

        // get status + img from broker
        trap1_status.setImageResource(R.drawable.green);
        trap2_status.setImageResource(R.drawable.red);

        handler.postDelayed(update, 5000);


    }

}


