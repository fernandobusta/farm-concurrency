import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Enclosure {
    private final Map<String, Integer> animals = new HashMap<>();
    private final Map<String, Field> fields;
    
    // Lock for all enclosure operations
    private final ReentrantLock lock = new ReentrantLock(); // fair: true
    
    // Condition used to signal farmers that animals are now available
    private final Condition notEmpty = lock.newCondition();
    private final TickSystem tickSystem;
    public static final String ANSI_RESET = "\u001B[0m"; 
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[36m";

    public Enclosure(Map<String, Field> fields, TickSystem tickSystem) {
        this.fields = fields;
        this.tickSystem = tickSystem; 
    }

    private boolean hasNoAnimals() {
        return animals.values().stream().allMatch(count -> count == 0);
    }

    // Store animals from a delivery
    public void storeFromDelivery(Map<String, Integer> delivery) throws InterruptedException{
        lock.lock();
        try {
            for (Map.Entry<String, Integer> entry : delivery.entrySet()) {
                String type = entry.getKey();
                int count = entry.getValue();
                animals.put(type, animals.getOrDefault(type, 0) + count);
            }
            System.out.println("     "+ANSI_BLUE+tickSystem.getCurrentTick() + " " + Thread.currentThread().getId() + " Deposit of animals from delivery : " + animals+ANSI_RESET);
            // Signal that enclosure is no longer empty
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

// Farmer loads animals into their trailer
public Map<String, Integer> loadAnimalsIntoTrailer(Map<String, Integer> existingTrailer, int capacity, String farmerName) throws InterruptedException {
    lock.lock();
    try {
        System.out.println("     "  + tickSystem.getCurrentTick() + " " + Thread.currentThread().getId() + " "  + farmerName + " has arrived at enclosure with capacity of: " + capacity);
        System.out.println("     "+tickSystem.getCurrentTick() + " " + Thread.currentThread().getId() + " "  + "Enclosure has: " + animals);

        while (hasNoAnimals()) {
            System.out.println("     " + tickSystem.getCurrentTick() + " " + Thread.currentThread().getId() + " "  +  "Farmer " + farmerName + " is waiting for animals...");
            notEmpty.await();
        }


        // Convert enclosure animals into a list and sort based on priority
        List<Map.Entry<String, Integer>> sortedAnimals = new ArrayList<>(animals.entrySet());

        sortedAnimals.sort((a, b) -> {
            Field fieldA = fields.get(a.getKey());
            Field fieldB = fields.get(b.getKey());

            // First priority: More buyers waiting
            int buyerDiff = Integer.compare(fieldB.getBuyersWaiting(), fieldA.getBuyersWaiting());
            if (buyerDiff != 0) return buyerDiff;

            // Second priority: Lower stock in the field
            return Integer.compare(fieldA.getCount(), fieldB.getCount());
        });

        Map<String, Integer> loadedAnimals = new LinkedHashMap<>();
        int spaceLeft = capacity;

        // Take at most 4 from the highest priority animal
        if (!sortedAnimals.isEmpty() && spaceLeft > 0) {
            Map.Entry<String, Integer> firstChoice = sortedAnimals.get(0);
            int numToTake = Math.min(4, Math.min(spaceLeft, firstChoice.getValue()));
            loadedAnimals.put(firstChoice.getKey(), numToTake);
            animals.put(firstChoice.getKey(), animals.get(firstChoice.getKey()) - numToTake);
            spaceLeft -= numToTake;
            sortedAnimals.remove(firstChoice);
        }

        // Take at most 3 from the second-highest priority animal
        if (!sortedAnimals.isEmpty() && spaceLeft > 0) {
            Map.Entry<String, Integer> secondChoice = sortedAnimals.get(0);
            int numToTake = Math.min(3, Math.min(spaceLeft, secondChoice.getValue()));
            loadedAnimals.put(secondChoice.getKey(), numToTake);
            animals.put(secondChoice.getKey(), animals.get(secondChoice.getKey()) - numToTake);
            spaceLeft -= numToTake;
            sortedAnimals.remove(secondChoice);
        }

        // Evenly distribute remaining space among other animals
        Iterator<Map.Entry<String, Integer>> it = sortedAnimals.iterator();
        while (spaceLeft > 0 && it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            String type = entry.getKey();
            int available = entry.getValue();
            
            if (available > 0) { // Only take if there are animals left
                int numToTake = Math.min(spaceLeft, available); // Take up to the remaining space

                loadedAnimals.put(type, loadedAnimals.getOrDefault(type, 0) + numToTake);
                animals.put(type, available - numToTake); // Subtract from enclosure
                spaceLeft -= numToTake;
            }
            
            // Remove entry if all animals have been taken
            if (animals.get(type) == 0) {
                it.remove();
            }
        }


        System.out.println("     "+ANSI_YELLOW + tickSystem.getCurrentTick() + " " + Thread.currentThread().getId() + " " +  farmerName + " collected_animals : " + loadedAnimals+ANSI_RESET);
        return loadedAnimals;
    } finally {
        lock.unlock();
    }
}
    

}


