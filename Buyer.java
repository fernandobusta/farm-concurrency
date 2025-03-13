import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Buyer implements Runnable {
    private final int buyerId;
    private final Map<String, Field> fields;
    private final Random rand;
    private final TickSystem tickSystem; // Store tick system


    public Buyer(int buyerId, Map<String, Field> fields, TickSystem tickSystem) {
        this.buyerId = buyerId;
        this.fields = fields;
        this.rand = new Random();
        this.tickSystem = tickSystem; // Assign tick system
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                tickSystem.waitForNextTick(); // Wait for tick before buying
                buyRandomAnimal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Try to buy a random animal from a field
    private void buyRandomAnimal() throws InterruptedException {
        List<String> keys = new ArrayList<>(fields.keySet());
        String animal = keys.get(rand.nextInt(keys.size()));
        Field field = fields.get(animal);

        System.out.println("⏳ Buyer " + buyerId + " waiting to buy " + field.getName());
        field.buyOne();
        System.out.println("🛒 Buyer " + buyerId + " bought 1 " + field.getName());
    }
}
