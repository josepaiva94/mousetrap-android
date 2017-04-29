package pt.up.fc.dcc.mousetrap.mqtt;

/**
 * Listener for picture received
 */
public interface PictureListener {

    /**
     * Handler for received picture
     *
     * @param deviceId id of the device
     * @param url received image url
     */
    void onPictureReceived(String deviceId, String url);
}
