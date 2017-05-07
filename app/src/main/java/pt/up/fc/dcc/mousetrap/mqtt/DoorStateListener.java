package pt.up.fc.dcc.mousetrap.mqtt;

/**
 * Listener for door state update
 */
public interface DoorStateListener {

    /**
     * Handler for changes in door state
     *
     * @param deviceId id of the device
     * @param state state of the door
     */
    void onDoorStateChanged(String deviceId, int state);
}
