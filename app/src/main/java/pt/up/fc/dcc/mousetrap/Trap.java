package pt.up.fc.dcc.mousetrap;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Trap extends Activity {

    TextView trap_name;
    ImageView trap_status_color;
    TextView trap_status_text;
    ImageView trap_img;
    Button btn_door;
    Button btn_ss;

    Handler handler = new Handler();
    Runnable update = new Runnable () {
        @Override
        public void run () {
            update_traps();
        }
    };

    int state = 0; // 0=offline   1=open   2=closed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trap);

        trap_name = (TextView) findViewById(R.id.trap_name);
        trap_status_color = (ImageView) findViewById(R.id.trap_status_color);
        trap_status_text = (TextView) findViewById(R.id.trap_status_text);
        trap_img = (ImageView) findViewById(R.id.trap_img);
        btn_door = (Button) findViewById(R.id.btn_door);
        btn_ss = (Button) findViewById(R.id.btn_ss);

        // trap name
        Intent intent = getIntent();
        String name = intent.getStringExtra("trap");
        trap_name.setText(name);

        // update trap status + img + btn_door
        update_traps();


        // open/close door if not offline
        if (state!=0) {
            btn_door.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    open_close_door(state);
                }
            });
        }

        btn_ss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenshot();
            }
        });

    }




    void update_traps () {

        // get status + img from broker
        state = 1;

        // TODO switch state=0,1,2
        trap_status_color.setImageResource(R.drawable.green);
        trap_status_text.setText("DOOR OPEN");
        btn_door.setText("CLOSE DOOR");

        //handler.postDelayed(update, 10000);  //necessario isto?
    }

    void open_close_door (int i) {
        switch(i) { // update_traps aqui? se proximo state for igual ao retornado pelo broker, continuar
            case 0: break;
            case 1: close_door(); break;
            case 2: open_door(); break;

        }

    }

    void close_door () {
        //if notify_broker("close_door") == OK
        state = 2;
        trap_status_color.setImageResource(R.drawable.red);
        trap_status_text.setText("DOOR CLOSED");
        btn_door.setText("OPEN DOOR");
    }

    void open_door () {
        //if notify_broker("open_door") == OK
        state = 1;
        trap_status_color.setImageResource(R.drawable.green);
        trap_status_text.setText("DOOR OPEN");
        btn_door.setText("CLOSE DOOR");
    }


    void screenshot() {
        // request screenshot from broker
        // save it to local folder
        // update trap_img
    }


}
