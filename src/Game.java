
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

    // List to keep track of zombies
    private ArrayList<Zombie> zombies = new ArrayList<>();

    // Available weapons
    private ArrayList<Weapon> availableWeapons = new ArrayList<>();
    private int selectedWeaponIndex = 0;

    // Timer for game loop
    private Timer gameTimer;

    public Game() {
        // Set the background color
        setBackground(Color.BLACK);
        setFocusable(true);

        // Add listeners
        addMouseListener(this);
        addKeyListener(this);

        // Initialize weapons
        availableWeapons.add(new Weapon("Pistol", 25, 0));
        availableWeapons.add(new Weapon("Shotgun", 40, 100));
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
        // Move all zombies
        for (Zombie zombie : zombies) {
            zombie.move(getWidth() / 2, getHeight() / 2);
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

        // Check if any zombies reached the house
        for (Zombie zombie : zombies) {
            if (Math.abs(zombie.getX() - getWidth() / 2) < 30
                    && Math.abs(zombie.getY() - getHeight() / 2) < 30) {
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

        // Draw player's home
        g.setColor(Color.BLUE);
        g.fillRect(getWidth() / 2 - 25, getHeight() / 2 - 25, 50, 50);

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
    }

    // Mouse click handler - shoot at zombies
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        Weapon currentWeapon = availableWeapons.get(selectedWeaponIndex);

        for (Zombie zombie : zombies) {
            double distance = Math.sqrt(
                    Math.pow(mouseX - zombie.getX(), 2)
                    + Math.pow(mouseY - zombie.getY(), 2)
            );

            if (distance < 20) {
                zombie.takeDamage(currentWeapon.getDamage());
                break;
            }
        }
    }

    // Key handler for weapon selection and purchase
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Number keys 1-3 for weapon selection
        if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_3) {
            selectedWeaponIndex = key - KeyEvent.VK_1;
        }

        // Space to buy selected weapon
        if (key == KeyEvent.VK_SPACE) {
            Weapon selectedWeapon = availableWeapons.get(selectedWeaponIndex);
            if (coins >= selectedWeapon.getCost()) {
                coins -= selectedWeapon.getCost();
                // Set cost to 0 once purchased
                availableWeapons.set(selectedWeaponIndex,
                        new Weapon(selectedWeapon.getName(), selectedWeapon.getDamage(), 0));
            }
        }

        // Enter to start new wave when previous is complete
        if (key == KeyEvent.VK_ENTER && !waveInProgress) {
            startNewWave();
        }
    }

    // Required key listener methods
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
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
