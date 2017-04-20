package pt.up.fc.dcc.mousetrap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import pt.up.fc.dcc.mousetrap.adapter.TrapsListAdapter;
import pt.up.fc.dcc.mousetrap.models.Trap;

public class MultiTrapsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_traps);
        GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setAdapter(new TrapsListAdapter(this));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trap trap = (Trap) parent.getAdapter().getItem(position);

                Intent trapIntent = new Intent(view.getContext(), TrapActivity.class);
                trapIntent.putExtra("trapId", trap.getId());
                startActivity(trapIntent);
            }
        });
    }

}
