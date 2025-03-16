import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

public class Farmer implements Runnable {

    private final Enclosure enclosure;
    private final Map<String, Field> fields;
    private final String farmerName;
    private final TickSystem tickSystem;
    private Map<String, Integer> trailer;
    private final int maxCapacity = 10;
    
    // Colours
    public static final String ANSI_RESET = "\u001B[0m"; 
    public static final String ANSI_YELLOW = "\u001B[33m";

    public Farmer(String farmerName, Enclosure enclosure, Map<String, Field> fields, TickSystem tickSystem) {
        this.trailer = new HashMap<>();
        this.farmerName = farmerName;
        this.enclosure = enclosure;
        this.fields = fields;
        this.tickSystem = tickSystem;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                tickSystem.waitForNextTick(); // Wait before acting

                // Calculate available space in the trailer
                int currentLoad = totalAnimalsInTrailer();
                System.out.println(ANSI_YELLOW + "🚜 " + farmerName + " has " + currentLoad + " animals in the trailer" + ANSI_RESET);
                int availableSpace = maxCapacity - currentLoad;

                // Load only if there is space left
                if (availableSpace > 0 ) {
                    System.out.println(ANSI_YELLOW + "🚜 " + farmerName + " is loading animals into the trailer" + ANSI_RESET);
                    trailer = enclosure.loadAnimalsIntoTrailer(trailer, availableSpace, farmerName);
                }

                System.out.println(ANSI_YELLOW + "🚜 " + farmerName + " received: " + trailer + ANSI_RESET);

                stockAnimals(); // Move and stock all animals

                travelBackToEnclosure(totalAnimalsInTrailer()); // Return to the enclosure
                System.out.println(ANSI_YELLOW + "🚜 " + farmerName + " returned to the enclosure" + ANSI_RESET);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


    private void stockAnimals() throws InterruptedException {
        // Perform the entire sequence fo travel and stock in fields of the animals
        // For each animal -> Walk to the field and add them to the field
         Set<String> visitedFields = new HashSet<>();
         
        // Create an iterator to safely traverse the trailer's animal entries
        Iterator<Map.Entry<String, Integer>> iterator = trailer.entrySet().iterator();

        while (iterator.hasNext()) { // Loop through each type of animal in the trailer
            Map.Entry<String, Integer> entry = iterator.next(); // Get the current animal entry
            String animal = entry.getKey(); // Extract the animal type (e.g., "sheep", "pigs")
            int quantity = entry.getValue(); // Extract the number of animals being carried

            Field field = fields.get(animal); // Get the corresponding field where these animals go

            
             // Skip already visited fields
            if (visitedFields.contains(animal)) continue;
            
            // Travel from enclosure (or previous field)
            int totalAnimalsLeft = totalAnimalsInTrailer();
            travelToField(animal, totalAnimalsLeft);
            
            System.out.println(ANSI_YELLOW  + "🚜 " + farmerName + " arrived at " + animal + " (count " + quantity + ")" + ANSI_RESET);
            
            // Place the animals into the field
            
            int actuallyStocked = field.stock(quantity); // We won't worry about capacity atm
            System.out.println(ANSI_YELLOW + "✅ " + farmerName + " stocked " + actuallyStocked + " " + field.getName() + ANSI_RESET);
            
            int leftToStock = quantity - actuallyStocked;

            if (leftToStock > 0) {
                entry.setValue(leftToStock); // ✅ Keep the remaining animals
                visitedFields.add(animal);   // ✅ Mark field as full
            } else {
                iterator.remove(); // ✅ Remove fully stocked animals
            }
        }

    }

    private void travelToField(String field, int numberOfAnimalsCarried) throws InterruptedException {
        int travelTime = 10 + numberOfAnimalsCarried; // 10 ticks + 1 per animal carried
    
        System.out.println(ANSI_YELLOW + "🚜 " + farmerName +  " carrying " 
            + numberOfAnimalsCarried + " animals " + " (Travel Time: " + travelTime + " ticks)" + ANSI_RESET);
    
        // Simulate travel time by waiting for each tick
        for (int i = 0; i < travelTime; i++) {
            tickSystem.waitForNextTick(); // Simulate travel ticks
        }
    
    }

    private void travelBackToEnclosure(int leftoverAnimals) throws InterruptedException {
        int travelTime = 10 + leftoverAnimals;
        tickSystem.waitForNTicks(travelTime);
        System.out.println(ANSI_YELLOW  + "🚜 " + farmerName + " traveled back to enclosure" + ANSI_RESET);
    }

    private int totalAnimalsInTrailer() {
        return trailer.values().stream().mapToInt(Integer::intValue).sum();
    }
}
