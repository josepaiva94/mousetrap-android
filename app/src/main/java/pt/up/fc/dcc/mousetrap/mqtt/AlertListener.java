package pt.up.fc.dcc.mousetrap.mqtt;

/**
 * Listener for received alert messages
 */
public interface AlertListener {

    /**
     * Handler for alerts
     *
     * @param deviceId id of the device
     * @param url image URL
     * @param timeout timeout for user to take action
     */
    void onAlertReceived(String deviceId, String url, int timeout);
}
