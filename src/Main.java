
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Create the game window
        JFrame window = new JFrame("10 Nights - Zombie Defense");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and add the game panel
        Game game = new Game();
        window.add(game);

        // Setup window
        window.setSize(800, 600);
        window.setLocationRelativeTo(null);  // Center the window
        window.setResizable(false);
        window.setVisible(true);
    }
}
