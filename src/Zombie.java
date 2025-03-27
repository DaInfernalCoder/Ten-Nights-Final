
import java.awt.Color;

public class Zombie {

    // Zombie types
    public static final String TYPE_NORMAL = "Normal";
    public static final String TYPE_FAST = "Fast";
    public static final String TYPE_TANK = "Tank";

    // Zombie position
    private int x;
    private int y;

    // Zombie properties
    private int health;
    private int speed;
    private String type;
    private Color color;

    public static Zombie createZombie(String type, int startX, int startY) {
        switch (type) {
            case TYPE_FAST:
                return new Zombie(startX, startY, 50, 4, TYPE_FAST, Color.RED);
            case TYPE_TANK:
                return new Zombie(startX, startY, 200, 1, TYPE_TANK, Color.GRAY);
            default:
                return new Zombie(startX, startY, 100, 2, TYPE_NORMAL, Color.GREEN);
        }
    }

    private Zombie(int startX, int startY, int health, int speed, String type, Color color) {
        this.x = startX;
        this.y = startY;
        this.health = health;
        this.speed = speed;
        this.type = type;
        this.color = color;
    }

    // Move zombie towards the center (where the home is)
    public void move(int targetX, int targetY) {
        // Simple movement: move directly towards target
        if (x < targetX) {
            x += speed;
        }
        if (x > targetX) {
            x -= speed;
        }
        if (y < targetY) {
            y += speed;
        }
        if (y > targetY) {
            y -= speed;
        }
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHealth() {
        return health;
    }

    public Color getColor() {
        return color;
    }

    public String getType() {
        return type;
    }

    // Take damage
    public void takeDamage(int damage) {
        health -= damage;
    }

    // Check if zombie is dead
    public boolean isDead() {
        return health <= 0;
    }
}
