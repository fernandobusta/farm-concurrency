import java.util.*;

public class Delivery implements Runnable {
    private final Enclosure enclosure;
    private final TickSystem tickSystem;
    private final Random rand;
    private int lastDeliveryTick = -100; // Ensures first delivery happens early
    private int nextDeliveryThreshold; // Randomized delivery threshold

    public Delivery(Enclosure enclosure, TickSystem tickSystem) {
        this.enclosure = enclosure;
        this.tickSystem = tickSystem;
        this.rand = new Random();
        this.nextDeliveryThreshold = 80 + rand.nextInt(40); // First threshold between 80-120 ticks
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                tickSystem.waitForNextTick(); // Wait for next tick
                
                int currentTick = tickSystem.getCurrentTick();
                boolean shouldDeliver = (rand.nextDouble() < 0.01) || (currentTick - lastDeliveryTick >= nextDeliveryThreshold);

                if (shouldDeliver) {
                    System.out.println("📦 New Delivery!");
                    Map<String, Integer> newDelivery = createRandomDelivery(10);
                    enclosure.storeFromDelivery(newDelivery);
                    
                    lastDeliveryTick = currentTick; 
                    
                    // Set new random threshold between 80-120 ticks
                    nextDeliveryThreshold = 80 + rand.nextInt(40); 
                    System.out.println("🔄 Next delivery threshold set to: " + nextDeliveryThreshold + " ticks");
                }

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
    
        return newDelivery;
    }

}
