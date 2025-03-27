
public class Weapon {

    private String name;
    private int damage;
    private int cost;

    public Weapon(String name, int damage, int cost) {
        this.name = name;
        this.damage = damage;
        this.cost = cost;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getCost() {
        return cost;
    }
}
