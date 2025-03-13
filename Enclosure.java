import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Enclosure {
    private final Map<String, Integer> animals = new HashMap<>();
    private final Random rand;
    
    // Lock for all enclosure operations
    private final ReentrantLock lock = new ReentrantLock(); // fair: true
    
    // Condition used to signal farmers that animals are now available
    private final Condition notEmpty = lock.newCondition();

    public Enclosure() {
        this.rand = new Random();
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
            System.out.println("✅ Enclosure updated: " + animals);
            // Signal that enclosure is no longer empty
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // Subtract animals when taken by a farmer
    private void subtractAnimals(Map<String, Integer> loadedAnimals) {
        for (Map.Entry<String, Integer> entry: loadedAnimals.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            // Subtract animal from the enclosure
            animals.put(type, animals.get(type) - count);
        }
    }

    // Farmer loads animals into their trailer (up to capacity)
    public Map<String, Integer> loadAnimalsIntoTrailer(int capacity, String farmerName) throws InterruptedException {
        lock.lock();
        try {
            System.out.println("🚜 Farmer " + farmerName + " has arrived at enclosure");
            System.out.println("🚜 Enclosure has: " + animals);
            while (hasNoAnimals()) { // Wait if all animal counts are 0
                System.out.println("⏳ Farmer " + farmerName + " is waiting for animals...");
                notEmpty.await();
            }
        
            Map<String, Integer> loadedAnimals = new HashMap<>();
            List<String> availableTypes = new ArrayList<>(animals.keySet());
        
            int spaceLeft = capacity;
            
            while (spaceLeft > 0 && !availableTypes.isEmpty()) {
                String type = availableTypes.get(rand.nextInt(availableTypes.size())); // Pick a random animal type
                int maxTake = Math.min(spaceLeft, animals.getOrDefault(type, 0)); // Max we can take
                // TODO: Improve randomisation (Always take capacity)
                if (maxTake > 0) {
                    int numToTake = rand.nextInt(maxTake) + 1; // Take 1 to maxTake
                    loadedAnimals.put(type, numToTake);
                    spaceLeft -= numToTake;
                } 
                availableTypes.remove(type);
            }
        
            System.out.println("🚜 Farmer received animals: " + loadedAnimals);
            subtractAnimals(loadedAnimals); // Subtract loaded animals from enclosure
            return loadedAnimals;
        } finally {
            lock.unlock();
        }
    }
}


