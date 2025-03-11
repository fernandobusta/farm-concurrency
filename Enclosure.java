import java.util.*;

public class Enclosure {
    private final Map<String, Integer> animals = new HashMap<>();
    private final Random random = new Random();

    // Store animals from a delivery
    public synchronized void storeFromDelivery(Map<String, Integer> delivery) {
        for (Map.Entry<String, Integer> entry : delivery.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            animals.put(type, animals.getOrDefault(type, 0) + count);
        }
        System.out.println("✅ Enclosure updated: " + animals);
        notifyAll(); // 🚀 Wake up the Farmer!
    }

    // Subtract animals when taken by a farmer
    public synchronized int subtractAnimals(String type, int count) {
        int available = animals.getOrDefault(type, 0);
        if (available >= count) {
            animals.put(type, available - count);
            return count;
        } else {
            animals.put(type, 0);
            return available;
        }
    }

    // Farmer loads animals into their trailer (up to capacity)
    public synchronized Map<String, Integer> loadAnimalsIntoTrailer(int capacity) throws InterruptedException {
        while (animals.isEmpty()) { // 🚨 Wait if no animals are in the enclosure
            System.out.println("⏳ Farmer is waiting for animals...");
            wait(); // Farmer will sleep until Delivery notifies
        }
    
        Map<String, Integer> takenAnimals = new HashMap<>();
        List<String> availableTypes = new ArrayList<>(animals.keySet());
    
        int spaceLeft = capacity;
    
        while (spaceLeft > 0 && !availableTypes.isEmpty()) {
            String type = availableTypes.get(random.nextInt(availableTypes.size())); // Pick a random animal type
            int maxTake = Math.min(spaceLeft, animals.getOrDefault(type, 0)); // Max we can take
    
            if (maxTake > 0) {
                int numToTake = random.nextInt(maxTake) + 1; // Take 1 to maxTake
                takenAnimals.put(type, numToTake);
                subtractAnimals(type, numToTake);
                spaceLeft -= numToTake;
            } 
    
            availableTypes.remove(type);
        }
    
        System.out.println("🚜 Farmer received animals: " + takenAnimals);
        return takenAnimals;
    }
    

    // Get current animal counts (for debugging/logging)
    public synchronized Map<String, Integer> getAnimals() {
        return new HashMap<>(animals);
    }
}


