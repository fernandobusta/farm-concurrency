import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Enclosure {
    private final Map<String, Integer> animals = new HashMap<>();
    private final Random rand;
    private final Map<String, Field> fields;
    
    // Lock for all enclosure operations
    private final ReentrantLock lock = new ReentrantLock(); // fair: true
    
    // Condition used to signal farmers that animals are now available
    private final Condition notEmpty = lock.newCondition();

    public Enclosure(Map<String, Field> fields) {
        this.rand = new Random();
        this.fields = fields;
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

    // Farmer loads animals into their trailer (up to capacity
public Map<String, Integer> loadAnimalsIntoTrailer(int capacity, String farmerName) throws InterruptedException {
    lock.lock();
    try {
        System.out.println("🚜 " + farmerName + " has arrived at enclosure with capacity of: " + capacity);
        System.out.println("🏠 Enclosure has: " + animals);

        while (hasNoAnimals()) {
            System.out.println("⏳ Farmer " + farmerName + " is waiting for animals...");
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

        // Step 1: Take at most 4 from the highest-priority animal
        if (!sortedAnimals.isEmpty() && spaceLeft > 0) {
            Map.Entry<String, Integer> firstChoice = sortedAnimals.get(0);
            int numToTake = Math.min(4, Math.min(spaceLeft, firstChoice.getValue()));
            loadedAnimals.put(firstChoice.getKey(), numToTake);
            animals.put(firstChoice.getKey(), animals.get(firstChoice.getKey()) - numToTake);
            spaceLeft -= numToTake;
            sortedAnimals.remove(firstChoice);
        }

        // Step 2: Take at most 3 from the second-highest priority animal
        if (!sortedAnimals.isEmpty() && spaceLeft > 0) {
            Map.Entry<String, Integer> secondChoice = sortedAnimals.get(0);
            int numToTake = Math.min(3, Math.min(spaceLeft, secondChoice.getValue()));
            loadedAnimals.put(secondChoice.getKey(), numToTake);
            animals.put(secondChoice.getKey(), animals.get(secondChoice.getKey()) - numToTake);
            spaceLeft -= numToTake;
            sortedAnimals.remove(secondChoice);
        }

        // Step 3: Evenly distribute remaining space among other animals
        int index = 0;
        while (spaceLeft > 0 && !sortedAnimals.isEmpty()) {
            Map.Entry<String, Integer> entry = sortedAnimals.get(index % sortedAnimals.size());
            String type = entry.getKey();
            int numToTake = Math.min(1, entry.getValue()); // Take only 1 per round

            if (numToTake > 0) {
                loadedAnimals.put(type, loadedAnimals.getOrDefault(type, 0) + numToTake);
                animals.put(type, animals.get(type) - numToTake);
                spaceLeft -= numToTake;
            }

            index++; // Move to the next animal type (round-robin)
        }

        System.out.println("🚜 " + farmerName + " received: " + loadedAnimals);
        return loadedAnimals;
    } finally {
        lock.unlock();
    }
}
    

}


