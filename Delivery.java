import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Delivery implements Runnable {

    private final Enclosure enclosure;
    private final Random rand;
    private final TickSystem tickSystem; // ✅ Store tick system


    public Delivery (Enclosure enclosure, TickSystem tickSystem) {
        this.enclosure = enclosure;
        this.rand = new Random(123);
        this.tickSystem = tickSystem; // ✅ Assign tick system
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 1% chance per tick (1/100 probability)
                if (rand.nextDouble() < 0.01) {  
                    Map<String, Integer> newDelivery = createRandomDelivery(10);
                    enclosure.storeFromDelivery(newDelivery);
                    System.out.println(tickSystem.getCurrentTick() + " Delivery-Thread 📦 Delivery arrived: " + newDelivery);
                }
                tickSystem.waitForNextTick(); // ⏳ Wait for next tick
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Creates a random distribution for a total number of animals.
     * @param totalAnimals
     * @return Map with animals (adding up to 10)
     */
    private Map<String, Integer> createRandomDelivery(int totalAnimals) {
        ArrayList<String> animals = new ArrayList<>(List.of("pigs", "cows", "sheep", "llamas", "chicken"));
        Collections.shuffle(animals);
    
        Map<String, Integer> newDelivery = new HashMap<>();
        int spaceLeft = totalAnimals;
    
        for (int i = 0; i < animals.size(); i++) {
            if (spaceLeft == 0) break; // Stop when all animals are assigned
    
            int maxAllocation = Math.min(spaceLeft, 3); // Max 3 per type
            int newEntrySize = (i == animals.size() - 1) ? spaceLeft : 1 + rand.nextInt(maxAllocation); // Last type gets all remaining space
            newDelivery.put(animals.get(i), newEntrySize);
            spaceLeft -= newEntrySize;
        }
    
        System.out.println("🔹 Generated delivery: " + newDelivery + " (Total: " + totalAnimals + ")");
        return newDelivery;
    }



}
