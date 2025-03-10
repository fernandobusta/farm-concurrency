import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Delivery implements Runnable {

    private final Enclosure enclosure;
    private final Random rand;

    private static final long TICK_DURATION_MS = 100; // 1 tick = 100ms

    public Delivery (Enclosure enclosure) {
        this.enclosure = enclosure;
        this.rand = new Random(123);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Sleep for a random time between 60 and 150 ms
                long randomTicks = 60 + rand.nextInt(151);
                Thread.sleep(randomTicks * TICK_DURATION_MS);

                // Generate a random distribution of 10 animals
                Map<String, Integer> newDelivery = createRandomDelivery(10);

                // Place them in the enclosure
                enclosure.storeFromDelivery(newDelivery);

                System.out.println("Delivery: " + newDelivery + " arrived after " + randomTicks + " ticks.");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Creates a random distribution for a total number of animals.
     * @param totalAnimals
     * @return Map with animals (adding up to 10)
     */
    private Map<String, Integer> createRandomDelivery(int totalAnimals) {
        ArrayList<String> animals = new ArrayList<>();
        animals.add("pigs");
        animals.add("cows");
        animals.add("sheep");
        animals.add("llamas");
        animals.add("chickens");
        Collections.shuffle(animals);

        Map<String, Integer> newDelivery = new HashMap<>();

        int spaceLeft = totalAnimals;
        for (int i = 0; i < animals.size(); i++) {
            // If there is no space left, break
            if (spaceLeft == 0) {
                break;
            }

            int newEntrySize = rand.nextInt(spaceLeft + 1);
            spaceLeft = spaceLeft - newEntrySize;
            newDelivery.put(animals.get(0), newEntrySize);

            // Remove the animal from the list
            animals.remove(0);
            // Shuffle the list to improve randomness
            Collections.shuffle(animals);
        }

        return newDelivery;
    }


}
