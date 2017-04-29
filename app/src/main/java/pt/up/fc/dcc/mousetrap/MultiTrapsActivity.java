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

import pt.up.fc.dcc.mousetrap.adapter.TrapsListAdapter;
import pt.up.fc.dcc.mousetrap.models.Trap;
import pt.up.fc.dcc.mousetrap.models.TrapImage;
import pt.up.fc.dcc.mousetrap.mqtt.MqttClient;
import pt.up.fc.dcc.mousetrap.utils.Auth;
import pt.up.fc.dcc.mousetrap.utils.PhotoStorageClient;

public class MultiTrapsActivity extends AppCompatActivity {

    private TrapsListAdapter adapter;

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

                Intent trapIntent = new Intent(view.getContext(), TrapActivity.class);
                trapIntent.putExtra("trapId", trap.getId());
                startActivity(trapIntent);
            }
        });

        // traps list
        Intent intent = getIntent();
        String[] trapsIds = intent.getStringArrayExtra("traps");
        for (String trapId : trapsIds) {
            final Trap trap = new Trap(trapId);

            PhotoStorageClient.getPhotos(trapId, 10, new PhotoStorageClient.PhotosRunnable() {
                @Override
                public void run() {
                    for (TrapImage img: getPhotos())
                        trap.addImage(img);
                }
            });

            adapter.add(trap);
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

            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...


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


    public void addTrap(final Trap trap){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(trap);
                findViewById(R.id.grid_view).invalidate();
            }
        });
    }

}
