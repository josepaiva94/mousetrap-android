package pt.up.fc.dcc.mousetrap;

/**
 * Constants used in MouseTrap
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public final class Constants {

    /* Auth0 */
    public static final String AUTH_SCOPE = "openid profile offline_access";
    public static final String AUTH_CONNECTION = "Traps";
    public static final String GOOGLE_AUTH_CONNECTION = "google-oauth2";
    public static final String ID_TOKEN_FIELD = "id_token";
    public static final String ACCESS_TOKEN_FIELD = "access_token";
    public static final String REFRESH_TOKEN_FIELD = "refresh_token";
    public static final String TOKEN_TYPE_FIELD = "token_type";
    public static final String SCOPE_FIELD = "scope";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String USER_METADATA_TOPICS_FIELD = "topics";

    /* Photo Storage API */
    public static final String PHOTO_STORAGE_API_URL = "http://10.0.2.2:3001/api/";
    public static final String PHOTO_STORAGE_API_PHOTOS_ENDPOINT = PHOTO_STORAGE_API_URL + "photos/%s";
    public static final String PHOTO_STORAGE_API_DEVICE_PHOTOS_ENDPOINT = PHOTO_STORAGE_API_URL + "devices/%s/photos";

    /* MQTT Broker */
    public static final String BROKER_PROTOCOL = "tcp";
    public static final String BROKER_HOSTNAME = "10.0.2.2";
    public static final String BROKER_PORT = "8443";
    public static final String BROKER_USERNAME_JWT = "JWT";

    public static final String TRAP_PREFIX_TOPIC = "traps/%s/";
    // open/close door
    public static final String TRAP_DOOR_TOPIC = TRAP_PREFIX_TOPIC + "door";
    // door IS open/closed
    public static final String TRAP_DOOR_STATE_TOPIC = TRAP_PREFIX_TOPIC + "door/state";
    // answer to a picture request
    public static final String TRAP_PICTURE_TOPIC = TRAP_PREFIX_TOPIC + "picture";
    // t// request a picture
    public static final String TRAP_PICTURE_REQUEST_TOPIC = TRAP_PREFIX_TOPIC + "picture/request";
    // alert of mouse inside
    public static final String TRAP_ALERT_TOPIC = TRAP_PREFIX_TOPIC + "alert";
    // time for user to take action
    public static final String TRAP_TIMEOUT_TOPIC = TRAP_PREFIX_TOPIC + "timeout";
    // inform that time for user to take action has changed
    public static final String TRAP_TIMEOUT_ACK_TOPIC = TRAP_PREFIX_TOPIC + "timeout/ack";

    // certificates
    public static final String DEFAULT_ALIAS = "MouseTrap Cert";
    public static final String DEFAULT_PASSWORD = "mousetrap";

    /* Globals */
    public static final String CHARSET = "UTF-8";


    /**
     * Get URL for retrieving a photo
     *
     * @param id photo id
     * @return URL for retrieving a photo
     */
    public static String getPhotoUrl(String id) {
        return String.format(PHOTO_STORAGE_API_PHOTOS_ENDPOINT, id);
    }

    /**
     * Get URL for retrieving all photos of a device
     *
     * @param id device id
     * @return URL for retrieving all photos of a device
     */
    public static String getDevicePhotosUrl(String id) {
        return String.format(PHOTO_STORAGE_API_DEVICE_PHOTOS_ENDPOINT, id);
    }

    /**
     * Get specific topic string for device
     *
     * @param topic global topic string
     * @param id device id
     * @return specific topic string for device
     */
    public static String getTopicForDevice(String topic, String id) {
        return String.format(topic, id);
    }
}
