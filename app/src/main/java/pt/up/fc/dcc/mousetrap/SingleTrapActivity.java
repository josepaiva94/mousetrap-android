package pt.up.fc.dcc.mousetrap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
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
import pt.up.fc.dcc.mousetrap.utils.Auth;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_single_trap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return true;
            case R.id.action_remove:
                // User chose the "Add" action
                popupRemoveTrap();
                return true;
            case R.id.action_timeout:
                // User chose the "Add" action
                popupTimeoutTrap();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("trap", trap);
        setResult(101, intent);
        super.finish();
    }

    /**
     * Popup to remove a trap
     */
    private void popupRemoveTrap() {

        new AlertDialog.Builder(this)
                .setTitle("Remove " + trap.getName())
                .setMessage("Do you really want to remove " + trap.getName() + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();

                        Auth.getInstance().removeDeviceTopics(trap.getId(), new Runnable() {
                            @Override
                            public void run() {
                                MqttClient.getInstance().connect();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                });
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    /**
     * Popup to set trap timeout
     */
    private void popupTimeoutTrap() {

        LayoutInflater inflater = (LayoutInflater)
                getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View npView = inflater.inflate(R.layout.number_picker_dialog_layout, null);

        String[] values = new String[] {"5", "10", "15", "20", "25", "30"};
        NumberPicker np = ((NumberPicker) npView);
        np.setMinValue(0);
        np.setMaxValue(values.length - 1);
        np.setDisplayedValues(values);
        np.setWrapSelectorWheel(true);
        np.setValue(trap.getTimeout()/5 - 1);

        new AlertDialog.Builder(this)
                .setTitle("Timeout")
                .setView(npView)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();

                                MqttClient.getInstance().publish(Topic.TIMEOUT, trap.getId(),
                                        String.valueOf((((NumberPicker) npView).getValue() + 1)*5));
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        })
                .show();
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
