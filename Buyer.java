import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Buyer implements Runnable {
    private final int buyerId;
    private final Map<String, Field> fields;
    private final Enclosure enclosure;
    private final Random rand;
    private final TickSystem tickSystem; // ✅ Store tick system


    public Buyer(int buyerId, Enclosure enclosure, Map<String, Field> fields, TickSystem tickSystem) {
        this.buyerId = buyerId;
        this.fields = fields;
        this.enclosure = enclosure;
        this.rand = new Random();
        this.tickSystem = tickSystem; // ✅ Assign tick system
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                tickSystem.waitForNextTick(); // 🚀 Wait for tick before buying
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
    
        // ✅ Buyer just waits if the field is empty (NO LOCK)
        while (field.getAnimalCount() == 0) { 
            System.out.println("⏳ Buyer " + buyerId + " is waiting for " + field.getName() + " to be stocked...");
            field.awaitStock(); // 🚀 Wait for Farmer to notify
        }
    
        removeAnimalFromField(field, 1); // ✅ Lock only when modifying
    }
    
    private void removeAnimalFromField(Field field, int numberOfAnimals) throws InterruptedException {
        field.lockField(); // 🔒 Lock only when modifying
        try {
            int previousCount = field.getAnimalCount();
            if (previousCount == 0) return; // 🚨 Double-check to avoid race conditions
    
            field.setAnimalCount(previousCount - numberOfAnimals);
            System.out.println("🛒 Buyer " + buyerId + " bought " + numberOfAnimals + " " + field.getName());
    
        } finally {
            field.unlockField(); // 🔓 Unlock after modifying
        }
    }
    

    
}
