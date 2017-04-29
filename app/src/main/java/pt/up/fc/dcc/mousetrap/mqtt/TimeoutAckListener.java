package pt.up.fc.dcc.mousetrap.mqtt;

/**
 * Listener for timeout ack messages
 */
public interface TimeoutAckListener {

    /**
     * Handler for received timeout acks
     *
     * @param deviceId id of the device
     * @param timeout timeout for user to take action
     */
    void onTimeoutAckReceived(String deviceId, int timeout);
}
