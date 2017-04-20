package pt.up.fc.dcc.mousetrap.models;

import android.support.annotation.NonNull;

import pt.up.fc.dcc.mousetrap.Constants;

/**
 * Class that represents an image of a trap
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class TrapImage implements Comparable<TrapImage> {

    private String id;
    private long timestamp = Long.MAX_VALUE;

    public TrapImage(String id) {
        this.id = id;
    }

    public TrapImage(String id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get URL to retrieve image
     * @return URL to retrieve image
     */
    public String getImageUrl() {
        return Constants.getPhotoUrl(id);
    }

    @Override
    public int compareTo(@NonNull TrapImage ti) {
        if (timestamp > ti.getTimestamp())
            return 1;
        else if (timestamp < ti.getTimestamp())
            return -1;
        return 0;
    }
}
