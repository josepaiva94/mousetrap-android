package pt.up.fc.dcc.mousetrap.mqtt;

import pt.up.fc.dcc.mousetrap.Constants;

/**
 * Enumeration of topics in MQTT
 */
public enum Topic {

    DOOR(Constants.TRAP_DOOR_TOPIC),
    DOOR_STATE(Constants.TRAP_DOOR_STATE_TOPIC),
    PICTURE(Constants.TRAP_PICTURE_TOPIC),
    PICTURE_REQUEST(Constants.TRAP_PICTURE_REQUEST_TOPIC),
    ALERT(Constants.TRAP_ALERT_TOPIC),
    TIMEOUT(Constants.TRAP_TIMEOUT_TOPIC),
    TIMEOUT_ACK(Constants.TRAP_TIMEOUT_ACK_TOPIC);

    private String topicString;

    Topic(String topicString) {
        this.topicString = topicString;
    }

    /**
     * Convert topic to a string with device Id
     * @param deviceId device id for topic
     * @return topic string
     */
    public String toString(String deviceId) {
        return Constants.getTopicForDevice(topicString, deviceId);
    }

    /**
     * Get topic from string
     * @param topic topic string
     * @return topic
     */
    public static Topic fromString(String topic) {

        topic = topic.replaceFirst("traps/[^/]+/", Constants.TRAP_PREFIX_TOPIC);

        switch (topic) {
            case Constants.TRAP_DOOR_TOPIC:
                return DOOR;
            case Constants.TRAP_DOOR_STATE_TOPIC:
                return DOOR_STATE;
            case Constants.TRAP_PICTURE_TOPIC:
                return PICTURE;
            case Constants.TRAP_PICTURE_REQUEST_TOPIC:
                return PICTURE_REQUEST;
            case Constants.TRAP_ALERT_TOPIC:
                return ALERT;
            case Constants.TRAP_TIMEOUT_TOPIC:
                return TIMEOUT;
            case Constants.TRAP_TIMEOUT_ACK_TOPIC:
                return TIMEOUT_ACK;
        }

        return Topic.valueOf(topic);
    }

    /**
     * Get topic from a string related with device id
     * @param topic topic string
     * @param deviceId id of the device
     * @return topic
     */
    public static Topic fromString(String topic, String deviceId) {
        String topicStr = topic.replace(deviceId, "%s");
        return fromString(topicStr);
    }
}
