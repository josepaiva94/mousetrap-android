package pt.up.fc.dcc.mousetrap.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Comparator;

import pt.up.fc.dcc.mousetrap.R;
import pt.up.fc.dcc.mousetrap.models.Trap;
import pt.up.fc.dcc.mousetrap.models.TrapImage;
import pt.up.fc.dcc.mousetrap.mqtt.AlertListener;
import pt.up.fc.dcc.mousetrap.mqtt.DoorStateListener;
import pt.up.fc.dcc.mousetrap.mqtt.MqttClient;
import pt.up.fc.dcc.mousetrap.mqtt.PictureListener;
import pt.up.fc.dcc.mousetrap.mqtt.TimeoutAckListener;
import pt.up.fc.dcc.mousetrap.utils.TrapPicasso;

/**
 * Adapter for traps list
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class TrapsListAdapter extends ArrayAdapter<Trap> implements AlertListener,
        DoorStateListener, PictureListener, TimeoutAckListener {

    public TrapsListAdapter(@NonNull Context context) {
        super(context, 0);
    }

    @Override
    public void add(@Nullable Trap object) {
        int pos = getPosition(object);
        if (pos == -1) {
            super.add(object);

            MqttClient.getInstance().addAlertListener(object.getId(), this);
            MqttClient.getInstance().addPictureListener(object.getId(), this);
            MqttClient.getInstance().addDoorStateListener(object.getId(), this);
            MqttClient.getInstance().addTimeoutAckListener(object.getId(), this);
        }

        sort();
    }

    public void sort() {

        sort(new Comparator<Trap>() {
            @Override
            public int compare(Trap o1, Trap o2) {
                return -o1.compareTo(o2);
            }
        });
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup viewGroup) {
        ImageView picture, state;
        TextView name;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_traps_item, viewGroup, false);
            view.setTag(R.id.picture, view.findViewById(R.id.picture));
            view.setTag(R.id.name, view.findViewById(R.id.name));
            view.setTag(R.id.state, view.findViewById(R.id.state));
        }

        picture = (ImageView) view.getTag(R.id.picture);
        state = (ImageView) view.getTag(R.id.state);
        name = (TextView) view.getTag(R.id.name);

        Trap trap = getItem(position);
        assert trap != null;

        if (trap.getImageUrl() == null)
            picture.setImageResource(R.drawable.placeholder);
        else
            TrapPicasso.getPicasso(getContext())
                    .load(trap.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(picture);

        name.setText(trap.getName());

        if (!trap.isActive())
            state.setImageResource(R.drawable.ic_lens_grey_24dp);
        else if (trap.isDoorOpen())
            state.setImageResource(R.drawable.ic_lens_green_24dp);
        else
            state.setImageResource(R.drawable.ic_lens_red_24dp);

        return view;
    }

    @Override
    public void onPictureReceived(String deviceId, String url) {

        int pos = getPosition(new Trap(deviceId));
        if (pos < 0)
            return;

        Trap trap = getItem(pos);
        trap.addImage(new TrapImage(url, System.currentTimeMillis()));

        notifyDataSetChanged();

        //Alerts.showMessage("Image received");
    }

    @Override
    public void onDoorStateChanged(String deviceId, int state) {

        int pos = getPosition(new Trap(deviceId));
        if (pos < 0)
            return;

        Trap trap = getItem(pos);
        trap.setDoorOpen(state != 0);
        trap.setActive(true);

        notifyDataSetChanged();

        //Alerts.showMessage("Door state received");

        sort();
    }

    @Override
    public void onTimeoutAckReceived(String deviceId, int timeout) {

        int pos = getPosition(new Trap(deviceId));
        if (pos < 0)
            return;

        Trap trap = getItem(pos);
        trap.setTimeout(timeout);

        //Alerts.showMessage("Timeout ack received");
    }

    @Override
    public void onAlertReceived(String deviceId, String url, int timeout) {

        int pos = getPosition(new Trap(deviceId));
        if (pos < 0)
            return;

        Trap trap = getItem(pos);

        trap.addImage(new TrapImage(url, System.currentTimeMillis()));
        trap.setTimeout(timeout);

        notifyDataSetChanged();

        //Alerts.showMessage("Alert received");
    }
}
