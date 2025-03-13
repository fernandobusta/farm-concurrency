import java.util.HashMap;
import java.util.Map;

public class Farmer implements Runnable {

    private final Enclosure enclosure;
    private final Map<String, Field> fields;
    private final String farmerName;
    private final TickSystem tickSystem; // Store tick system
    private Map<String, Integer> trailer;
    
    // Colours
    public static final String ANSI_RESET = "\u001B[0m"; 
    public static final String ANSI_YELLOW = "\u001B[33m";

    public Farmer(String farmerName, Enclosure enclosure, Map<String, Field> fields, TickSystem tickSystem) {
        this.trailer = new HashMap<>();
        this.farmerName = farmerName;
        this.enclosure = enclosure;
        this.fields = fields;
        this.tickSystem = tickSystem; // Assign tick system
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                tickSystem.waitForNextTick(); // Wait before acting

                // Enclosure handles waiting, no need for extra check in Farmer
                trailer = enclosure.loadAnimalsIntoTrailer(10, farmerName);

                System.out.println(ANSI_YELLOW + "🚜 " + farmerName + " received: " + trailer + ANSI_RESET);

                stockAnimals(); // Move and stock all animals

                travelBackToEnclosure(totalAnimalsInTrailer()); // Return to the enclosure

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


    private void stockAnimals() throws InterruptedException {
        // Perform the entire sequence fo travel and stock in fields of the animals
        // For each animal -> Walk to the field and add them to the field
        for (Map.Entry<String, Integer> entry : trailer.entrySet()) {
            String animal = entry.getKey();
            int quantity = entry.getValue();
            
            
            // Travel from enclosure (or previous field)
            int totalAnimalsLeft = totalAnimalsInTrailer();
            travelToField(animal, totalAnimalsLeft);
            
            System.out.println(ANSI_YELLOW  + "🚜 " + farmerName + " arrived at " + animal + " (count " + quantity + ")" + ANSI_RESET);
            
            // Place the animals into the field
            Field field = fields.get(animal); // get the field from map
            
            int actuallyStocked = field.stock(quantity); // We won't worry about capacity atm
            System.out.println(ANSI_YELLOW + "✅ " + farmerName + " stocked " + actuallyStocked + " " + field.getName() + ANSI_RESET);
            
            // Eliminate animals from map
            entry.setValue(0); // Assuming we stocked all of them (no capacity)
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
