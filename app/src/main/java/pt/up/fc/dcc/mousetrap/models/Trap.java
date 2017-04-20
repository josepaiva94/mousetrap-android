package pt.up.fc.dcc.mousetrap.models;

import java.util.List;

import pt.up.fc.dcc.mousetrap.utils.SortedArrayList;

/**
 * Class that represents a Trap
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class Trap {

    private String id;
    private String name;
    private boolean doorOpen;
    private SortedArrayList<TrapImage> images;
    private boolean active;

    public Trap(String id) {
        this(id, id, true, new SortedArrayList<TrapImage>(), false);
    }

    public Trap(String id, boolean doorOpen, boolean active) {
        this(id, id, doorOpen, new SortedArrayList<TrapImage>(), active);
    }

    public Trap(String id, String name, boolean doorOpen, SortedArrayList<TrapImage> images, boolean active) {
        this.id = id;
        this.name = name;
        this.doorOpen = doorOpen;
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
}
