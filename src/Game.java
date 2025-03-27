
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class Game extends JPanel implements ActionListener, MouseListener, KeyListener {

    // Game variables
    private int playerHealth = 100;
    private int coins = 0;
    private int currentWave = 1;
    private int zombiesInWave = 5;
    private int zombiesSpawned = 0;
    private boolean waveInProgress = true;

    // Player variables
    private int playerX;
    private int playerY;
    private int playerSpeed = 5;
    private boolean movingUp, movingDown, movingLeft, movingRight;

    // List to keep track of zombies
    private ArrayList<Zombie> zombies = new ArrayList<>();

    // Available weapons
    private ArrayList<Weapon> availableWeapons = new ArrayList<>();
    private int selectedWeaponIndex = 0;

    // Timer for game loop
    private Timer gameTimer;

    // Bullet visualization
    private static class ShotTrail {

        double startX, startY, endX, endY;
        int lifetime;

        ShotTrail(double startX, double startY, double endX, double endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.lifetime = 10; // Will show for 10 frames
        }
    }

    private ArrayList<ShotTrail> shotTrails = new ArrayList<>();

    public Game() {
        // Set the background color
        setBackground(Color.BLACK);
        setFocusable(true);

        // Initialize player position at center
        playerX = 400;
        playerY = 300;

        // Add listeners
        addMouseListener(this);
        addKeyListener(this);

        // Initialize weapons
        availableWeapons.add(new Weapon("Pistol", 25, 0));
        availableWeapons.add(new Weapon("Shotgun", 15, 100));  // Less damage per pellet but shoots multiple
        availableWeapons.add(new Weapon("Rifle", 60, 200));

        // Start the game loop (runs 60 times per second)
        gameTimer = new Timer(1000 / 60, this);
        gameTimer.start();

        // Start first wave
        startNewWave();
    }

    private void startNewWave() {
        currentWave++;
        zombiesInWave = 5 + (currentWave * 2);  // More zombies each wave
        zombiesSpawned = 0;
        waveInProgress = true;
    }

    // Spawn a new zombie at a random position along the edges
    private void spawnZombie() {
        int x, y;
        if (Math.random() < 0.5) {
            x = (Math.random() < 0.5) ? 0 : 800;
            y = (int) (Math.random() * 600);
        } else {
            x = (int) (Math.random() * 800);
            y = (Math.random() < 0.5) ? 0 : 600;
        }

        // Different zombie types based on wave and randomness
        String zombieType;
        double rand = Math.random();
        if (currentWave >= 5 && rand < 0.2) {
            zombieType = Zombie.TYPE_TANK;
        } else if (currentWave >= 3 && rand < 0.4) {
            zombieType = Zombie.TYPE_FAST;
        } else {
            zombieType = Zombie.TYPE_NORMAL;
        }

        zombies.add(Zombie.createZombie(zombieType, x, y));
        zombiesSpawned++;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update player position based on movement flags
        if (movingUp) {
            playerY = Math.max(playerY - playerSpeed, 0);
        }
        if (movingDown) {
            playerY = Math.min(playerY + playerSpeed, getHeight() - 50);
        }
        if (movingLeft) {
            playerX = Math.max(playerX - playerSpeed, 0);
        }
        if (movingRight) {
            playerX = Math.min(playerX + playerSpeed, getWidth() - 50);
        }

        // Move all zombies towards player instead of center
        for (Zombie zombie : zombies) {
            zombie.move(playerX + 25, playerY + 25);  // Target player center
        }

        // Spawn new zombie if wave is in progress
        if (waveInProgress && zombiesSpawned < zombiesInWave && Math.random() < 0.02) {
            spawnZombie();
        }

        // Check if wave is complete
        if (waveInProgress && zombiesSpawned >= zombiesInWave && zombies.isEmpty()) {
            waveInProgress = false;
            coins += 50;  // Bonus coins for completing wave
        }

        // Check if any zombies reached the player
        for (Zombie zombie : zombies) {
            if (Math.abs(zombie.getX() - (playerX + 25)) < 30
                    && Math.abs(zombie.getY() - (playerY + 25)) < 30) {
                playerHealth -= 1;
            }
        }

        // Remove dead zombies and give coins
        for (int i = zombies.size() - 1; i >= 0; i--) {
            if (zombies.get(i).isDead()) {
                // More coins for stronger zombies
                switch (zombies.get(i).getType()) {
                    case Zombie.TYPE_TANK:
                        coins += 30;
                        break;
                    case Zombie.TYPE_FAST:
                        coins += 20;
                        break;
                    default:
                        coins += 10;
                }
                zombies.remove(i);
            }
        }

        // Update shot trails
        for (int i = shotTrails.size() - 1; i >= 0; i--) {
            ShotTrail trail = shotTrails.get(i);
            trail.lifetime--;
            if (trail.lifetime <= 0) {
                shotTrails.remove(i);
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw game stats
        g.setColor(Color.WHITE);
        g.drawString("Health: " + playerHealth, 10, 20);
        g.drawString("Coins: " + coins, 10, 40);
        g.drawString("Wave: " + currentWave, 10, 60);
        g.drawString("Zombies: " + zombies.size() + "/" + zombiesInWave, 10, 80);
        g.drawString("Current Weapon: " + availableWeapons.get(selectedWeaponIndex).getName(), 10, 100);

        // Draw shop instructions
        g.drawString("Press 1-3 to select weapon", getWidth() - 200, 20);
        g.drawString("Press SPACE to buy selected weapon", getWidth() - 200, 40);

        // Draw available weapons
        int y = 60;
        for (int i = 0; i < availableWeapons.size(); i++) {
            Weapon weapon = availableWeapons.get(i);
            String text = (i + 1) + ": " + weapon.getName() + " (DMG: " + weapon.getDamage()
                    + ", Cost: " + weapon.getCost() + ")";
            g.setColor(i == selectedWeaponIndex ? Color.YELLOW : Color.WHITE);
            g.drawString(text, getWidth() - 200, y);
            y += 20;
        }

        // Draw player
        g.setColor(Color.BLUE);
        g.fillRect(playerX, playerY, 50, 50);

        // Draw all zombies with their respective colors
        for (Zombie zombie : zombies) {
            g.setColor(zombie.getColor());
            g.fillOval(zombie.getX() - 10, zombie.getY() - 10, 20, 20);

            // Draw health bar
            int healthBarWidth = 20;
            int healthBarHeight = 3;
            g.setColor(Color.RED);
            g.fillRect(zombie.getX() - healthBarWidth / 2, zombie.getY() - 15, healthBarWidth, healthBarHeight);
            g.setColor(Color.GREEN);
            g.fillRect(zombie.getX() - healthBarWidth / 2, zombie.getY() - 15,
                    (int) (healthBarWidth * (zombie.getHealth() / 100.0)), healthBarHeight);
        }

        // Draw shot trails
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        for (ShotTrail trail : shotTrails) {
            // Make shots fade out
            float alpha = trail.lifetime / 10.0f;
            g2d.setColor(new Color(1.0f, 1.0f, 0.0f, alpha)); // Yellow color with fade
            g2d.drawLine((int) trail.startX, (int) trail.startY,
                    (int) trail.endX, (int) trail.endY);
        }
    }

    // Mouse click handler - shoot at zombies
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        Weapon currentWeapon = availableWeapons.get(selectedWeaponIndex);

        // For shotgun, shoot multiple pellets in a spread
        if (currentWeapon.getName().equals("Shotgun")) {
            // Calculate angle to mouse
            double angle = Math.atan2(mouseY - (playerY + 25), mouseX - (playerX + 25));

            // Shoot 5 pellets in a spread
            for (int i = -2; i <= 2; i++) {
                double spreadAngle = angle + Math.toRadians(i * 10); // 10 degree spread between pellets
                double spreadDistance = 300; // Maximum range

                double startX = playerX + 25;
                double startY = playerY + 25;
                double endX = startX + Math.cos(spreadAngle) * spreadDistance;
                double endY = startY + Math.sin(spreadAngle) * spreadDistance;

                // Add visual trail
                shotTrails.add(new ShotTrail(startX, startY, endX, endY));

                // Check each zombie against this pellet's path
                for (Zombie zombie : zombies) {
                    if (isPointNearLine(startX, startY, endX, endY,
                            zombie.getX(), zombie.getY(), 20)) {
                        zombie.takeDamage(currentWeapon.getDamage());
                    }
                }
            }
        } else {
            // Regular weapon shooting
            for (Zombie zombie : zombies) {
                double distance = Math.sqrt(
                        Math.pow(mouseX - zombie.getX(), 2)
                        + Math.pow(mouseY - zombie.getY(), 2)
                );

                if (distance < 20) {
                    zombie.takeDamage(currentWeapon.getDamage());
                    // Add single shot trail for regular weapons
                    shotTrails.add(new ShotTrail(playerX + 25, playerY + 25,
                            zombie.getX(), zombie.getY()));
                    break;
                }
            }
        }
    }

    // Helper method to check if a point is near a line segment
    private boolean isPointNearLine(double x1, double y1, double x2, double y2,
            double px, double py, double tolerance) {
        double lineLength = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        if (lineLength == 0) {
            return false;
        }

        double u = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / (lineLength * lineLength);
        if (u < 0 || u > 1) {
            return false;
        }

        double x = x1 + u * (x2 - x1);
        double y = y1 + u * (y2 - y1);
        double distance = Math.sqrt(Math.pow(x - px, 2) + Math.pow(y - py, 2));

        return distance <= tolerance;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Movement controls
        switch (key) {
            case KeyEvent.VK_W:
                movingUp = true;
                break;
            case KeyEvent.VK_S:
                movingDown = true;
                break;
            case KeyEvent.VK_A:
                movingLeft = true;
                break;
            case KeyEvent.VK_D:
                movingRight = true;
                break;
        }

        // Weapon selection (1-3 keys)
        if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_3) {
            selectedWeaponIndex = key - KeyEvent.VK_1;
        }

        // Space to buy selected weapon
        if (key == KeyEvent.VK_SPACE) {
            Weapon selectedWeapon = availableWeapons.get(selectedWeaponIndex);
            if (coins >= selectedWeapon.getCost()) {
                coins -= selectedWeapon.getCost();
                availableWeapons.set(selectedWeaponIndex,
                        new Weapon(selectedWeapon.getName(), selectedWeapon.getDamage(), 0));
            }
        }

        // Enter to start new wave when previous is complete
        if (key == KeyEvent.VK_ENTER && !waveInProgress) {
            startNewWave();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        // Stop movement when keys are released
        switch (key) {
            case KeyEvent.VK_W:
                movingUp = false;
                break;
            case KeyEvent.VK_S:
                movingDown = false;
                break;
            case KeyEvent.VK_A:
                movingLeft = false;
                break;
            case KeyEvent.VK_D:
                movingRight = false;
                break;
        }
    }

    // Required key listener method (unused)
    @Override
    public void keyTyped(KeyEvent e) {
    }

    // Required mouse listener methods
    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
