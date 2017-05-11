package pt.up.fc.dcc.mousetrap.mqtt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.up.fc.dcc.mousetrap.App;
import pt.up.fc.dcc.mousetrap.Constants;
import pt.up.fc.dcc.mousetrap.MultiTrapsActivity;
import pt.up.fc.dcc.mousetrap.R;
import pt.up.fc.dcc.mousetrap.models.ModelStore;
import pt.up.fc.dcc.mousetrap.models.Trap;
import pt.up.fc.dcc.mousetrap.models.TrapImage;
import pt.up.fc.dcc.mousetrap.utils.Alerts;
import pt.up.fc.dcc.mousetrap.utils.Auth;

/**
 * MQTT Android Client
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class MqttClient implements MqttCallbackExtended {
    private static final String INTEGER_REGEX = "^[0-9]+$";
    private static final String BINARY_REGEX = "^0|1$";
    private static final Pattern TOPIC_DEVICE_PATTERN = Pattern.compile("^traps/([^\\/]+)/");

    private static MqttClient client = null;

    private static int notificationId = 1;

    private MqttAndroidClient mqttAndroidClient = null;

    private Map<String, ArrayList<DoorStateListener>> doorStateListeners = new HashMap<>();
    private Map<String, ArrayList<PictureListener>> pictureListeners = new HashMap<>();
    private Map<String, ArrayList<AlertListener>> alertListeners = new HashMap<>();
    private Map<String, ArrayList<TimeoutAckListener>> timeoutAckListeners = new HashMap<>();


    private MqttClient() {
    }

    public static MqttClient getInstance() {
        if (client == null)
            client = new MqttClient();
        return client;
    }

    /**
     * Initializes Android MQTT client
     */
    public void initClient() {

        if (mqttAndroidClient != null && mqttAndroidClient.isConnected())
            try {
                mqttAndroidClient.disconnect();
            } catch (MqttException e) {
                Alerts.showError("Disconnecting from broker ...");
            }

        connect();
    }

    /**
     * Connect to broker
     */
    public void connect() {

        try {

            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
            }
        } catch (Exception e){
            // ignore exception
        }

        mqttAndroidClient = new MqttAndroidClient(App.getContext(),
                Constants.BROKER_PROTOCOL + "://" + Constants.BROKER_HOSTNAME + ":" + Constants.BROKER_PORT,
                Auth.getAccessToken() + System.currentTimeMillis());
        mqttAndroidClient.setCallback(this);

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(Constants.BROKER_USERNAME_JWT);
        mqttConnectOptions.setPassword(Auth.getIdToken().toCharArray());
        // disabled SSL
        /*mqttConnectOptions.setSocketFactory(SslUtils.getSocketFactory(R.raw.mousetrapcert,
                R.raw.mousetrapcert, R.raw.mousetrapts, null, null));*/

        try {
            mqttAndroidClient.connect(mqttConnectOptions, App.getContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToRegisteredTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, final Throwable exception) {
                    Alerts.showError(exception.getLocalizedMessage());
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

        if (reconnect)
            Alerts.showMessage("Reconnected to MQTT Broker");
        else {
            Alerts.showMessage("Connected to MQTT Broker");
            //subscribeToRegisteredTopics();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Alerts.showWarning("Lost connection");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        onMessage(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    /**
     * Subscribes to all topics in user metadata
     */
    public void subscribeToRegisteredTopics() {

        String[] topics = Auth.getInstance().getRegisteredTopics();

        if (topics.length <= 1)
            return;

        String[] validTopics = new String[topics.length];
        int[] validQos = new int[topics.length];
        int count = 0;
        for (String topic : topics) {

            try {

                switch (Topic.fromString(topic)) {
                    case DOOR_STATE:
                    case PICTURE:
                    case ALERT:
                    case TIMEOUT_ACK:

                        validTopics[count] = topic;
                        validQos[count] = 1;
                        count++;
                        break;
                }
            } catch (Exception e) {
                continue;
            }
        }

        try {
            mqttAndroidClient.subscribe(Arrays.copyOfRange(validTopics, 0, count),
                    Arrays.copyOfRange(validQos, 0, count), App.getContext(), new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Alerts.showMessage("Successfully subscribed topics");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                            Alerts.showWarning("Could not subscribe to topics: " + e.getMessage());
                        }
                    });
        } catch (MqttException e) {
            Alerts.showWarning("Could not subscribe to topics");
        }
    }

    /**
     * Add listener for door state updates
     *
     * @param deviceId id of the device to listen to
     * @param listener listener to add
     */
    public void addDoorStateListener(String deviceId, DoorStateListener listener) {

        ArrayList<DoorStateListener> listeners = doorStateListeners.get(deviceId);

        if (listeners == null)
            listeners = new ArrayList<>();

        if (!listeners.contains(listener))
            listeners.add(listener);

        doorStateListeners.put(deviceId, listeners);
    }

    /**
     * Remove listener for door state updates
     *
     * @param deviceId id of the device to listen to
     * @param listener listener to remove
     */
    public void removeDoorStateListener(String deviceId, DoorStateListener listener) {

        ArrayList<DoorStateListener> listeners = doorStateListeners.get(deviceId);

        if (listeners == null)
            return;
        listeners.remove(listener);
    }

    /**
     * Add listener for alert messages
     *
     * @param deviceId id of the device to listen to
     * @param listener listener to add
     */
    public void addAlertListener(String deviceId, AlertListener listener) {

        ArrayList<AlertListener> listeners = alertListeners.get(deviceId);

        if (listeners == null)
            listeners = new ArrayList<>();

        if (!listeners.contains(listener))
            listeners.add(listener);

        alertListeners.put(deviceId, listeners);
    }

    /**
     * Remove listener for alert messages
     *
     * @param deviceId id of the device to listen to
     * @param listener listener to remove
     */
    public void removeAlertListener(String deviceId, AlertListener listener) {

        ArrayList<AlertListener> listeners = alertListeners.get(deviceId);

        if (listeners == null)
            return;
        listeners.remove(listener);
    }

    /**
     * Add listener for picture messages
     *
     * @param deviceId id of the device to listen to
     * @param listener listener to add
     */
    public void addPictureListener(String deviceId, PictureListener listener) {

        ArrayList<PictureListener> listeners = pictureListeners.get(deviceId);

        if (listeners == null)
            listeners = new ArrayList<>();

        if (!listeners.contains(listener))
            listeners.add(listener);

        pictureListeners.put(deviceId, listeners);
    }

    /**
     * Remove listener for picture messages
     *
     * @param deviceId id of the device to listen to
     * @param listener listener to remove
     */
    public void removePictureListener(String deviceId, PictureListener listener) {

        ArrayList<PictureListener> listeners = pictureListeners.get(deviceId);

        if (listeners == null)
            return;
        listeners.remove(listener);
    }

    /**
     * Add listener for timeout ack messages
     *
     * @param deviceId id of the device to listen to
     * @param listener listener to add
     */
    public void addTimeoutAckListener(String deviceId, TimeoutAckListener listener) {

        ArrayList<TimeoutAckListener> listeners = timeoutAckListeners.get(deviceId);

        if (listeners == null)
            listeners = new ArrayList<>();

        if (!listeners.contains(listener))
            listeners.add(listener);

        timeoutAckListeners.put(deviceId, listeners);
    }

    /**
     * Remove listener for timeout ack messages
     *
     * @param deviceId id of the device to listen to
     * @param listener listener to remove
     */
    public void removeTimeoutAckListener(String deviceId, TimeoutAckListener listener) {

        ArrayList<TimeoutAckListener> listeners = timeoutAckListeners.get(deviceId);

        if (listeners == null)
            return;
        listeners.remove(listener);
    }

    /**
     * Handler called when a message is received
     *
     * @param topicStr topic of the message
     * @param message  actual message
     */
    public void onMessage(String topicStr, MqttMessage message) {

        String deviceId = getTopicDevice(topicStr);
        Topic topic = Topic.fromString(topicStr, deviceId);
        String payload = null;
        try {
            payload = new String(message.getPayload(), Constants.CHARSET);
        } catch (UnsupportedEncodingException e) {
            Alerts.showError(e.getMessage());
            return;
        }

        String[] args = payload.split(" ");

        switch (topic) {

            case DOOR_STATE:

                if (args.length != 1 || !args[0].matches(BINARY_REGEX)) {
                    Alerts.showError("Invalid payload");
                    return;
                }

                if (doorStateListeners.get(deviceId) == null) // if nobody cares
                    return;
                for (DoorStateListener doorStateListener : doorStateListeners.get(deviceId)) {
                    doorStateListener.onDoorStateChanged(deviceId, Integer.parseInt(args[0]));
                }

                break;
            case PICTURE:

                if (args.length != 1) {
                    Alerts.showError("Invalid payload");
                    return;
                }

                if (pictureListeners.get(deviceId) == null) // if nobody cares
                    return;
                for (PictureListener pictureListener : pictureListeners.get(deviceId)) {
                    pictureListener.onPictureReceived(deviceId, args[0]);
                }

                break;
            case ALERT:

                if (args.length != 2 || !args[1].matches(INTEGER_REGEX)) {
                    Alerts.showError("Invalid payload");
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    showPushNotification(deviceId, args[0], Integer.parseInt(args[1]));
                }

                if (alertListeners.get(deviceId) == null) // if nobody cares
                    return;
                for (AlertListener alertListener : alertListeners.get(deviceId)) {
                    alertListener.onAlertReceived(deviceId, args[0], Integer.parseInt(args[1]));
                }

                break;
            case TIMEOUT_ACK:

                if (args.length != 1 || !args[0].matches(INTEGER_REGEX)) {
                    Alerts.showError("Invalid payload");
                    return;
                }

                if (timeoutAckListeners.get(deviceId) == null) // if nobody cares
                    return;
                for (TimeoutAckListener timeoutAckListener : timeoutAckListeners.get(deviceId)) {
                    timeoutAckListener.onTimeoutAckReceived(deviceId, Integer.parseInt(args[0]));
                }

                break;
        }
    }

    /**
     * Show push notification
     * @param deviceId the id of the device
     * @param url image url
     * @param timeout timeout
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void showPushNotification(String deviceId, String url, int timeout) {

        final Trap trap = ModelStore.getInstance(App.getContext()).retrieve(deviceId,
                new Trap(deviceId));

        trap.addImage(new TrapImage(url, System.currentTimeMillis()));
        trap.setTimeout(timeout);

        String[] devices = Auth.getInstance().getAllRegisteredDevices();

        final int fNotificationId = notificationId;

        // Sets up open trap activity
        Intent trapIntent = new Intent(App.getContext(), MultiTrapsActivity.class);
        trapIntent.putExtra("traps", devices);
        trapIntent.putExtra("notification", true);
        trapIntent.putExtra("trap", trap);
        trapIntent.putExtra("door", 1);
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
                .addAction(R.drawable.ic_lens_red_24dp,
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

    /**
     * Publish a message in a topic of a device
     *
     * @param topic    topic to publish on
     * @param deviceId device that should listen to the message
     * @param args     payload parts (arguments of the message)
     */
    public void publish(Topic topic, String deviceId, String... args) {

        switch (topic) {

            case DOOR:
                if (args.length != 1 || !args[0].matches(BINARY_REGEX)) {
                    Alerts.showWarning("Invalid arguments while publishing to topic");
                    return;
                }

                try {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(args[0].getBytes(Constants.CHARSET));
                    mqttAndroidClient.publish(topic.toString(deviceId), message);
                } catch (MqttException | UnsupportedEncodingException e) {
                    Alerts.showError(e.getMessage());
                }

                break;
            case PICTURE_REQUEST:
                if (args != null && args.length != 0) {
                    Alerts.showWarning("Invalid arguments while publishing to topic");
                    return;
                }

                try {
                    MqttMessage message = new MqttMessage();
                    mqttAndroidClient.publish(topic.toString(deviceId), message);
                } catch (MqttException e) {
                    Alerts.showError(e.getMessage());
                }

                break;
            case TIMEOUT:
                if (args.length != 1 || !args[0].matches(INTEGER_REGEX)) {
                    Alerts.showWarning("Invalid arguments while publishing to topic");
                    return;
                }

                try {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(args[0].getBytes(Constants.CHARSET));
                    mqttAndroidClient.publish(topic.toString(deviceId), message);
                } catch (MqttException | UnsupportedEncodingException e) {
                    Alerts.showError(e.getMessage());
                }

                break;
            default:
                Alerts.showWarning("No such topic");
        }
    }

    /**
     * Get device for topic of the form traps/%s/XX
     *
     * @param topic topic string
     * @return device id
     */
    public static String getTopicDevice(String topic) {

        Matcher m = TOPIC_DEVICE_PATTERN.matcher(topic);

        if (!m.find())
            throw new InvalidParameterException();

        return m.group(1);
    }
}
