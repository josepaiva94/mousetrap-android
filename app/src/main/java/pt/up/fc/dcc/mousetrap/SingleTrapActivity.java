package pt.up.fc.dcc.mousetrap;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import pt.up.fc.dcc.mousetrap.models.Trap;
import pt.up.fc.dcc.mousetrap.models.TrapImage;
import pt.up.fc.dcc.mousetrap.mqtt.AlertListener;
import pt.up.fc.dcc.mousetrap.mqtt.DoorStateListener;
import pt.up.fc.dcc.mousetrap.mqtt.MqttClient;
import pt.up.fc.dcc.mousetrap.mqtt.PictureListener;
import pt.up.fc.dcc.mousetrap.mqtt.TimeoutAckListener;
import pt.up.fc.dcc.mousetrap.mqtt.Topic;
import pt.up.fc.dcc.mousetrap.utils.Alerts;
import pt.up.fc.dcc.mousetrap.utils.TrapPicasso;

public class SingleTrapActivity extends AppCompatActivity implements AlertListener,
        PictureListener, DoorStateListener, TimeoutAckListener {

    private Trap trap;

    private TextView single_trap_id;
    private ToggleButton single_trap_door_action;
    private ImageView single_trap_state;
    private ImageView single_trap_picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_trap);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton picture_request = (FloatingActionButton) findViewById(R.id.picture_request);
        picture_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MqttClient.getInstance().publish(Topic.PICTURE_REQUEST, trap.getId());
                Alerts.showMessage("Picture requested");
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        trap = (Trap) intent.getSerializableExtra("trap");

        single_trap_id = ((TextView) findViewById(R.id.single_trap_id));
        single_trap_door_action = ((ToggleButton) findViewById(R.id.single_trap_door_action));
        single_trap_state = ((ImageView) findViewById(R.id.single_trap_state));
        single_trap_picture = ((ImageView) findViewById(R.id.single_trap_picture));

        single_trap_id.setText(trap.getName());
        single_trap_door_action.setChecked(trap.isDoorOpen());

        if (!trap.isActive())
            single_trap_state.setImageResource(R.drawable.ic_lens_grey_24dp);
        else if (trap.isDoorOpen())
            single_trap_state.setImageResource(R.drawable.ic_lens_green_24dp);
        else
            single_trap_state.setImageResource(R.drawable.ic_lens_red_24dp);

        if (trap.getImageUrl() == null)
            single_trap_picture.setImageResource(R.drawable.placeholder);
        else
            TrapPicasso.getPicasso(getApplicationContext())
                    .load(trap.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(single_trap_picture);

        single_trap_door_action.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MqttClient.getInstance().publish(Topic.DOOR, trap.getId(), String.valueOf(isChecked ? 1 : 0));
                Alerts.showMessage("Sent order to " + (isChecked ? "OPEN" : "CLOSE") + " door");
            }
        });

        MqttClient.getInstance().addDoorStateListener(trap.getId(), this);
        MqttClient.getInstance().addPictureListener(trap.getId(), this);
        MqttClient.getInstance().addAlertListener(trap.getId(), this);
        MqttClient.getInstance().addTimeoutAckListener(trap.getId(), this);
    }

    @Override
    protected void onDestroy() {

        MqttClient.getInstance().removeDoorStateListener(trap.getId(), this);
        MqttClient.getInstance().removePictureListener(trap.getId(), this);
        MqttClient.getInstance().removeAlertListener(trap.getId(), this);
        MqttClient.getInstance().removeTimeoutAckListener(trap.getId(), this);

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPictureReceived(String deviceId, String url) {

        if (!trap.getId().equals(deviceId))
            return;

        trap.addImage(new TrapImage(url, System.currentTimeMillis()));

        TrapPicasso.getPicasso(this)
                .load(trap.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(single_trap_picture);

        //Alerts.showMessage("Image received");
    }

    @Override
    public void onDoorStateChanged(String deviceId, int state) {

        if (!trap.getId().equals(deviceId))
            return;

        if (state != 0) {
            single_trap_state.setImageResource(R.drawable.ic_lens_green_24dp);
            single_trap_door_action.setChecked(true);
        } else {
            single_trap_state.setImageResource(R.drawable.ic_lens_red_24dp);
            single_trap_door_action.setChecked(false);
        }

        trap.setDoorOpen(state != 0);
        trap.setActive(true);

        //Alerts.showMessage("Door state received");
    }

    @Override
    public void onTimeoutAckReceived(String deviceId, int timeout) {

        if (!trap.getId().equals(deviceId))
            return;

        trap.setTimeout(timeout);

        //Alerts.showMessage("Timeout ack received");
    }

    @Override
    public void onAlertReceived(String deviceId, String url, int timeout) {

        if (!trap.getId().equals(deviceId))
            return;

        trap.addImage(new TrapImage(url, System.currentTimeMillis()));
        trap.setTimeout(timeout);

        TrapPicasso.getPicasso(this)
                .load(trap.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(single_trap_picture);

        //Alerts.showMessage("Alert received");
    }
}
