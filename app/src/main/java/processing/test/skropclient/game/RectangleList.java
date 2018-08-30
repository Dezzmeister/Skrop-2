package processing.test.skropclient.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PConstants;

public class RectangleList implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7614719587964684947L;

    public static final int MAX_SPAWN_TRIES = 50;

    private List<Rectangle> list = new ArrayList<Rectangle>();

    private transient int maxRectangles;

    private transient float lowerSpawnBoundX = 0.05f;
    private transient float upperSpawnBoundX = 0.95f;

    private transient float lowerSpawnBoundY = 0.05f;
    private transient float upperSpawnBoundY = 0.95f;

    private transient float lowestPossibleMaxWidth = 0.15f;
    private transient float highestPossibleMaxWidth = 0.4f;

    private transient float lowestPossibleMaxHeight = 0.15f;
    private transient float highestPossibleMaxHeight = 0.4f;

    public RectangleList(int _maxRectangles) {
        maxRectangles = _maxRectangles;
    }

    public void update(float delta) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Rectangle rect = list.get(i);
            rect.update(delta);

            if (rect.cycleHasCompleted()) {
                list.remove(i);
            }
        }

        while (list.size() < maxRectangles && addRandomRectangle()) {
            ;
        }
    }

    /**
     * If a <code>Rectangle</code> was destroyed by a hit at
     * <code>(xHit,yHit)</code> returns the <code>Rectangle</code>. If not, returns
     * null.
     *
     * @param xHit
     *            x coordinate to be tested
     * @param yHit
     *            y coordinate to be tested
     * @return <code>Rectangle</code> that was hit, or null
     */
    public Rectangle successfulHit(float xHit, float yHit) {

        for (int i = list.size() - 1; i >= 0; i--) {

            if (list.get(i).wasDestroyed(xHit, yHit)) {
                return list.remove(i);
            }
        }

        return null;
    }

    private boolean addRandomRectangle() {
        float x = randomBoundedX();
        float y = randomBoundedY();

        int tries = 0;

        while (!isAcceptablePlacement(x, y) && tries < MAX_SPAWN_TRIES) {
            x = randomBoundedX();
            y = randomBoundedY();

            tries++;
        }

        if (tries == MAX_SPAWN_TRIES) {
            System.out.println("Could not spawn a rectangle after " + tries
                    + " attempts. Consider lowering the amound of rectangles or raising the max spawn tries.");
            return false;
        } else {
            list.add(new Rectangle(x, y, randomBoundedMaxWidth(), randomBoundedMaxHeight(), list.size()));
            return true;
        }
    }

    private boolean isAcceptablePlacement(float x, float y) {
        for (Rectangle r : list) {
            float halfWidth = r.width / 2.0f;
            float halfHeight = r.height / 2.0f;

            float lowerX = r.x - halfWidth;
            float upperX = r.x + halfWidth;

            float lowerY = r.y - halfHeight;
            float upperY = r.y + halfHeight;

            if (x >= lowerX && x <= upperX && y >= lowerY && y <= upperY) {
                return false;
            }
        }

        return true;
    }

    public List<Rectangle> getRectangles() {
        return list;
    }

    private float randomBoundedMaxWidth() {
        return (float) (lowestPossibleMaxWidth + (Math.random() * (highestPossibleMaxWidth - lowestPossibleMaxWidth)));
    }

    private float randomBoundedMaxHeight() {
        return (float) (lowestPossibleMaxHeight
                + (Math.random() * (highestPossibleMaxHeight - lowestPossibleMaxHeight)));
    }

    private float randomBoundedX() {
        return (float) (lowerSpawnBoundX + (Math.random() * (upperSpawnBoundX - lowerSpawnBoundX)));
    }

    private float randomBoundedY() {
        return (float) (lowerSpawnBoundY + (Math.random() * (upperSpawnBoundY - lowerSpawnBoundY)));
    }
}
