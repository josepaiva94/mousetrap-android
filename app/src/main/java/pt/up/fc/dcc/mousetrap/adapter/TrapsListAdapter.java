package pt.up.fc.dcc.mousetrap.adapter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Comparator;

import pt.up.fc.dcc.mousetrap.App;
import pt.up.fc.dcc.mousetrap.MultiTrapsActivity;
import pt.up.fc.dcc.mousetrap.R;
import pt.up.fc.dcc.mousetrap.models.Trap;
import pt.up.fc.dcc.mousetrap.models.TrapImage;
import pt.up.fc.dcc.mousetrap.mqtt.AlertListener;
import pt.up.fc.dcc.mousetrap.mqtt.DoorStateListener;
import pt.up.fc.dcc.mousetrap.mqtt.MqttClient;
import pt.up.fc.dcc.mousetrap.mqtt.PictureListener;
import pt.up.fc.dcc.mousetrap.mqtt.TimeoutAckListener;
import pt.up.fc.dcc.mousetrap.utils.Auth;
import pt.up.fc.dcc.mousetrap.utils.TrapPicasso;

/**
 * Adapter for traps list
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class TrapsListAdapter extends ArrayAdapter<Trap> implements AlertListener,
        DoorStateListener, PictureListener, TimeoutAckListener {

    private static int notificationId = 1;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            showPushNotification(trap, url, timeout);
        }

        trap.addImage(new TrapImage(url, System.currentTimeMillis()));
        trap.setTimeout(timeout);

        notifyDataSetChanged();

        //Alerts.showMessage("Alert received");
    }

    /**
     * Show push notification
     * @param trap the trap
     * @param url image url
     * @param timeout timeout
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void showPushNotification(final Trap trap, String url, int timeout) {

        String[] devices = Auth.getInstance().getAllRegisteredDevices();

        final int fNotificationId = notificationId;

        // Sets up open trap activity
        Intent trapIntent = new Intent(App.getContext(), MultiTrapsActivity.class);
        trapIntent.putExtra("traps", devices);
        trapIntent.putExtra("notification", true);
        trapIntent.putExtra("trap", trap);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.getContext(), fNotificationId,
                trapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Sets up the CLOSE action button that will appear in the notification
        Intent closeDoorIntent = new Intent(App.getContext(), MultiTrapsActivity.class);
        closeDoorIntent.putExtra("traps", devices);
        closeDoorIntent.putExtra("notification", true);
        closeDoorIntent.putExtra("trap", trap);
        closeDoorIntent.putExtra("door", 0);
        PendingIntent closeIntent = PendingIntent.getActivity(App.getContext(), fNotificationId,
                closeDoorIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationManager notificationManager = (NotificationManager)
                App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        final Notification notification = new Notification.Builder(App.getContext())
                .setContentTitle("Mouse inside")
                .setContentText("A mouse entered in " + trap.getName())
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setAutoCancel(true)
                .setTicker("A mouse entered in " + trap.getName())
                .setVibrate(new long[] { 100, 250, 100, 500})
                .setDefaults(Notification.DEFAULT_ALL)
                .addAction (R.drawable.red,
                        App.getContext().getString(R.string.action_close), closeIntent)
                .build();

        // Hide the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(trap.getId(), fNotificationId, notification);
        notificationId++;

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            public void run() {
                notificationManager.cancel(trap.getId(), fNotificationId);
            }
        }, timeout * 1000);
    }
}
