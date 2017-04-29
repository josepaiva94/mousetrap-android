package pt.up.fc.dcc.mousetrap.models;

import android.support.annotation.NonNull;

import java.util.List;

import pt.up.fc.dcc.mousetrap.utils.SortedArrayList;

/**
 * Class that represents a Trap
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class Trap implements Comparable<Trap> {

    private String id;
    private String name;
    private boolean doorOpen;
    private int timeout;
    private SortedArrayList<TrapImage> images;
    private boolean active;

    public Trap(String id) {
        this(id, id, true, 5, new SortedArrayList<TrapImage>(), false);
    }

    public Trap(String id, boolean doorOpen, int timeout, boolean active) {
        this(id, id, doorOpen, timeout, new SortedArrayList<TrapImage>(), active);
    }

    public Trap(String id, String name, boolean doorOpen, int timeout,
                SortedArrayList<TrapImage> images, boolean active) {
        this.id = id;
        this.name = name;
        this.doorOpen = doorOpen;
        this.timeout = timeout;
        this.images = images;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDoorOpen() {
        return doorOpen;
    }

    public void setDoorOpen(boolean doorOpen) {
        this.doorOpen = doorOpen;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public SortedArrayList<TrapImage> getImages() {
        return images;
    }

    public void setImages(SortedArrayList<TrapImage> images) {
        this.images = images;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Add image to the trap
     * @param image image to add
     */
    public void addImage(TrapImage image) {
        images.insertSorted(image);
    }

    /**
     * Get URL of the last image taken
     */
    public String getImageUrl() {
        if (images == null || images.size() == 0)
            return null;
        return getImageUrl(images.size() - 1);
    }

    /**
     * Get URL of ith image
     * @param i position of the image
     */
    public String getImageUrl(int i) {
        if (images == null || i >= images.size())
            return null;
        return images.get(i).getImageUrl();
    }

    @Override
    public int compareTo(@NonNull Trap o) {
        if (id.equals(o.getId()))
            return 0;
        if (active && !o.isActive())
            return 1;
        else if (active && o.isActive())
            return 0;
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trap trap = (Trap) o;

        if (!id.equals(trap.id)) return false;
        return true;

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
