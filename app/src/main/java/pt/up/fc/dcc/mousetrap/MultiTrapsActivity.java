package pt.up.fc.dcc.mousetrap;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import java.util.ArrayList;

import pt.up.fc.dcc.mousetrap.adapter.TrapsListAdapter;
import pt.up.fc.dcc.mousetrap.models.ModelStore;
import pt.up.fc.dcc.mousetrap.models.Trap;
import pt.up.fc.dcc.mousetrap.models.TrapImage;
import pt.up.fc.dcc.mousetrap.mqtt.MqttClient;
import pt.up.fc.dcc.mousetrap.mqtt.Topic;
import pt.up.fc.dcc.mousetrap.utils.Auth;
import pt.up.fc.dcc.mousetrap.utils.PhotoStorageClient;

public class MultiTrapsActivity extends AppCompatActivity {

    private TrapsListAdapter adapter;
    private ArrayList<Trap> traps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_traps);

        adapter = new TrapsListAdapter(this);

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trap trap = (Trap) parent.getAdapter().getItem(position);

                Intent trapIntent = new Intent(view.getContext(), SingleTrapActivity.class);
                trapIntent.putExtra("trap", trap);
                startActivityForResult(trapIntent, 0);

            }
        });

        // traps list
        Intent intent = getIntent();
        if (intent.getStringArrayExtra("traps") != null) {
            String[] trapsIds = intent.getStringArrayExtra("traps");
            for (String trapId: trapsIds) {
                traps.add(ModelStore.getInstance(App.getContext()).retrieve(trapId,
                        new Trap(trapId)));
            }
        }

        loadTraps();

        // check if opening from notification
        if (intent.getBooleanExtra("notification", false)) {
            Trap trap = (Trap) intent.getSerializableExtra("trap");

            if (intent.getIntExtra("door", 1) == 0)
                MqttClient.getInstance().publish(Topic.DOOR, trap.getId(), String.valueOf(0));

            Intent trapIntent = new Intent(this, SingleTrapActivity.class);
            trapIntent.putExtra("trap", trap);
            startActivityForResult(trapIntent, 0);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // check if opening from notification
        if (intent.getBooleanExtra("notification", false)) {
            Trap trap = (Trap) intent.getSerializableExtra("trap");

            if (intent.getIntExtra("door", 1) == 0)
                MqttClient.getInstance().publish(Topic.DOOR, trap.getId(), String.valueOf(0));

            Intent trapIntent = new Intent(this, SingleTrapActivity.class);
            trapIntent.putExtra("trap", trap);
            startActivityForResult(trapIntent, 0);
        }
    }

    private void loadTraps() {

        for (final Trap trap : traps) {

            PhotoStorageClient.getPhotos(trap.getId(), 10, new PhotoStorageClient.PhotosRunnable() {
                @Override
                public void run() {
                    for (TrapImage img : getPhotos())
                        trap.addImage(img);
                }
            });

            adapter.add(trap);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        for (Trap trap : traps)
            ModelStore.getInstance(App.getContext()).store(trap);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {

        for (Trap trap : traps) {
            ModelStore.getInstance(App.getContext()).store(trap);

            MqttClient.getInstance().addAlertListener(trap.getId(), adapter);
            MqttClient.getInstance().addPictureListener(trap.getId(), adapter);
            MqttClient.getInstance().addDoorStateListener(trap.getId(), adapter);
            MqttClient.getInstance().addTimeoutAckListener(trap.getId(), adapter);
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 101) {
            Trap trap = (Trap) data.getSerializableExtra("trap");
            removeTrap(trap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_multi_traps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                // User chose the "Add" action

                popupAddTrap();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Popup to add a trap
     */
    private void popupAddTrap() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add trap");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.fragment_insert_trap_dialog,
                (ViewGroup) findViewById(android.support.v7.appcompat.R.id.contentPanel), false);

        final EditText input = (EditText) viewInflated.findViewById(R.id.text_trap_id);

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                final String trapId = input.getText().toString();

                Auth.getInstance().registerDeviceTopics(trapId, new Runnable() {
                    @Override
                    public void run() {
                        addTrap(new Trap(trapId));
                        MqttClient.getInstance().connect();
                    }
                });
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Add trap to adapter
     * @param trap new trap
     */
    public void addTrap(final Trap trap){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                traps.add(trap);
                adapter.add(trap);
                findViewById(R.id.grid_view).invalidate();
            }
        });
    }

    /**
     * Remove trap from adapter
     * @param trap old trap
     */
    public void removeTrap(final Trap trap){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                traps.remove(trap);
                adapter.remove(trap);
                findViewById(R.id.grid_view).invalidate();
            }
        });
    }

}
