import java.util.Map;

public class Farmer implements Runnable {

    private final Enclosure enclosure;
    private final Map<String, Field> fields;
    private final String farmerName;
    private final TickSystem tickSystem; // Store tick system

    public Farmer(String farmerName, Enclosure enclosure, Map<String, Field> fields, TickSystem tickSystem) {
        this.farmerName = farmerName;
        this.enclosure = enclosure;
        this.fields = fields;
        this.tickSystem = tickSystem; // Assign tick system
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                tickSystem.waitForNextTick(); // ⏳ Wait before acting
    
                // Enclosure handles waiting, no need for extra check in Farmer
                Map<String, Integer> trailerAnimals = enclosure.loadAnimalsIntoTrailer(10, farmerName);

    
                System.out.println(tickSystem.getCurrentTick() + " Farmer-Thread 🚜 Received animals: " + trailerAnimals);
    
                stockAnimals(trailerAnimals); // Move and stock all animals
    
                travelBackToEnclosure(0); // Return to the enclosure
    
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    

    private void stockAnimals(Map<String, Integer> animalsToField) throws InterruptedException {
        // Perform the entire sequence fo travel and stock in fields of the animals
        System.out.println("Stocking the animals");
        // For each animal -> Walk to the field and add them to the field
        for (Map.Entry<String, Integer> entry : animalsToField.entrySet()) {
            String animal = entry.getKey();
            int quantity = entry.getValue();

            // Travel from enclosure (or previous field)
            travelToField(animal, quantity);

            // Place the animals into the field
            Field field = fields.get(animal); // get the field from map
            
            int actuallyStocked = field.stock(quantity); //We won't worry about capacity atm
            System.out.println("✅ Farmer " + farmerName + " stocked " + actuallyStocked + " " + field.getName());

        }
    }

    private void travelToField(String field, int numberOfAnimalsCarried) throws InterruptedException {
        int travelTime = 10 + numberOfAnimalsCarried; // 10 ticks + 1 per animal carried
    
        System.out.println(tickSystem.getCurrentTick() + " Farmer-Thread 🚶 Carrying " 
            + numberOfAnimalsCarried + " " + field + " (Travel Time: " + travelTime + " ticks)");
    
        // Simulate travel time by waiting for each tick
        for (int i = 0; i < travelTime; i++) {
            tickSystem.waitForNextTick(); // Simulate travel ticks
        }
    
        System.out.println(tickSystem.getCurrentTick() + " Farmer-Thread 🚜 Arrived at " + field);
    }

    private void travelBackToEnclosure(int leftoverAnimals) throws InterruptedException {
        System.out.println("Going back to enclosure -> add ticks");
    }



    
}
