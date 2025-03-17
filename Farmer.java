import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Farmer implements Runnable {

    private final Enclosure enclosure;
    private final Map<String, Field> fields;
    private final String farmerName;
    private final TickSystem tickSystem;
    private Map<String, Integer> trailer;
    private final int maxCapacity = 10;
    private final int breakDuration;
    private final int breakInterval;
    
    // Colours
    public static final String ANSI_RESET = "\u001B[0m"; 
    public static final String ANSI_YELLOW = "\u001B[33m";

    public Farmer(String farmerName, Enclosure enclosure, Map<String, Field> fields, TickSystem tickSystem, int breakDuration, int breakInterval) {
        this.trailer = new HashMap<>();
        this.farmerName = farmerName;
        this.enclosure = enclosure;
        this.fields = fields;
        this.tickSystem = tickSystem;
        this.breakDuration = breakDuration;
        this.breakInterval = breakInterval;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                tickSystem.waitForNextTick(); // Wait before acting
                
                // Check if it's time for a break
                checkForBreak();

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

                goToFieldAndStock(); // Move and stock all animals

                travelBackToEnclosure(totalAnimalsInTrailer()); // Return to the enclosure
                System.out.println(ANSI_YELLOW + "🚜 " + farmerName + " returned to the enclosure" + ANSI_RESET);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void checkForBreak() throws InterruptedException {
        int currentTick = tickSystem.getCurrentTick();
        if (currentTick % breakInterval == 0) {
            takeABreak();
        }
    }

    private void takeABreak() throws InterruptedException {
            System.out.println(ANSI_YELLOW + "🚜 " + farmerName + " is taking a break" + ANSI_RESET);
            tickSystem.waitForNTicks(breakDuration);
            System.out.println(ANSI_YELLOW + "🚜 " + farmerName + " finished break" + ANSI_RESET);
    }


    private void goToFieldAndStock() throws InterruptedException {
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
            int actuallyStocked = stockAnimalsInField(field, quantity);
            System.out.println(ANSI_YELLOW + "✅ " + farmerName + " stocked " + actuallyStocked + " " + field.getName() + ANSI_RESET);
            
            int leftToStock = quantity - actuallyStocked;

            if (leftToStock > 0) {
                entry.setValue(leftToStock); // Keep the remaining animals
                visitedFields.add(animal);   // Mark field as full
            } else {
                iterator.remove(); // Remove fully stocked animals
            }
        }

    }

    private int stockAnimalsInField(Field field, int quantity) throws InterruptedException {
        field.lockField();
        try {
            boolean willNeedBreakAfter = false;
            int fieldCapacity = field.getCapacity();
            int currentCount = field.getCount();
            while (currentCount == fieldCapacity) {
                System.out.println("⏳ Field is full. Waiting for buyers to buy...");
                System.out.println("count: " + currentCount + " capacity: " + fieldCapacity);
                return 0;
            }
            // If partial stocking is necessary
            int spaceLeft = fieldCapacity - currentCount;
            int added = Math.min(spaceLeft, quantity);
            field.setCount(currentCount + added);

            System.out.println("🌾 Adding " + added + " to " + field.getName() + "field");
            for (int i=0; i<added; i++) {
                tickSystem.waitForNextTick();
                int currentTick = tickSystem.getCurrentTick();
                if (currentTick % breakInterval == 0) {
                    willNeedBreakAfter = true;
                }
            }
            System.out.println("🌾 " + field.getName() + " stocked. Count =  " + field.getCount());
            // Signal that the field is not empty anymore (buyers can proceed)
            field.signalAllNotEmpty();

            if (willNeedBreakAfter) {
                takeABreak();
            }
            return added; 
        } finally {
            field.unlockField();
        }

    }

    private void travelToField(String field, int numberOfAnimalsCarried) throws InterruptedException {
        int travelTime = 10 + numberOfAnimalsCarried; // 10 ticks + 1 per animal carried
    
        System.out.println(ANSI_YELLOW + "🚜 " + farmerName +  " carrying " 
            + numberOfAnimalsCarried + " animals " + " (Travel Time: " + travelTime + " ticks)" + ANSI_RESET);
    
        // Simulate travel time by waiting for each tick
        for (int i = 0; i < travelTime; i++) {
            tickSystem.waitForNextTick(); // Simulate travel ticks
            checkForBreak();
        }
    
    }

    private void travelBackToEnclosure(int leftoverAnimals) throws InterruptedException {
        int travelTime = 10 + leftoverAnimals;

        for (int i=0; i<travelTime; i++) {
            tickSystem.waitForNextTick();
            checkForBreak();
        }

        tickSystem.waitForNTicks(travelTime);
        System.out.println(ANSI_YELLOW  + "🚜 " + farmerName + " traveled back to enclosure" + ANSI_RESET);
    }

    private int totalAnimalsInTrailer() {
        return trailer.values().stream().mapToInt(Integer::intValue).sum();
    }
}
