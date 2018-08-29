package processing.test.skropclient.game;

import java.io.Serializable;

/**
 * Uses NDC (Normalized Device Coordinates).
 *
 * @author Joe Desmond
 */
public final class Rectangle implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3838813427155057819L;
    public final float x;
    public final float y;

    public float height;
    public float width;

    public final float maxHeight;
    public final float maxWidth;

    public final float id;

    private boolean growing = true;
    private float widthGrowthSpeed = 0.005f;
    private float heightGrowthSpeed;
    private boolean completed = false;

    private final int color = generateRandomColor();

    public Rectangle(float _x, float _y, float _maxWidth, float _maxHeight, float _id) {
        x = _x;
        y = _y;
        maxWidth = _maxWidth;
        maxHeight = _maxHeight;
        id = _id;

        calculateHeightGrowthSpeed();
    }

    public boolean wasDestroyed(float xHit, float yHit) {
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;

        return (xHit > x - halfWidth && xHit < x + halfWidth && yHit > y - halfHeight && yHit < y + halfHeight);
    }

    public void update(float delta) {
        if (!completed) {
            if (growing) {
                if (width + (delta * widthGrowthSpeed) >= maxWidth) {
                    width = maxWidth;
                    height = maxHeight;
                    growing = false;
                } else {
                    width += (delta * widthGrowthSpeed);
                    height += (delta * heightGrowthSpeed);
                }
            } else {
                if (width - (delta * widthGrowthSpeed) <= 0) {
                    width = 0;
                    height = 0;
                    completed = true;
                } else {
                    width -= (delta * widthGrowthSpeed);
                    height -= (delta * heightGrowthSpeed);
                }
            }
        } else {
            width = 0;
            height = 0;
        }
    }

    private int generateRandomColor() {
        int red = (int) (Math.random() * 256);
        int green = (int) (Math.random() * 256);
        int blue = (int) (Math.random() * 256);

        return (red << 16) | (green << 8) | blue;
    }

    public void setWidthGrowthSpeed(float speed) {
        widthGrowthSpeed = speed;
        calculateHeightGrowthSpeed();
    }

    private void calculateHeightGrowthSpeed() {
        heightGrowthSpeed = (widthGrowthSpeed / maxWidth) * maxHeight;
    }

    public boolean cycleHasCompleted() {
        return completed;
    }

    public int color() {
        return color;
    }
}
