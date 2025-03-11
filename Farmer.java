import java.util.Map;

public class Farmer implements Runnable {

    private final Enclosure enclosure;
    private final Map<String, Field> fields;
    private final String farmerName;
    private final TickSystem tickSystem; // ✅ Store tick system

    public Farmer(String farmerName, Enclosure enclosure, Map<String, Field> fields, TickSystem tickSystem) {
        this.farmerName = farmerName;
        this.enclosure = enclosure;
        this.fields = fields;
        this.tickSystem = tickSystem; // ✅ Assign tick system
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                tickSystem.waitForNextTick();
                // Take 10 animals from enclosure
                Map<String, Integer> currentAnimals = enclosure.loadAnimalsIntoTrailer(10);
                
                // If no animals were taken, maybe enclosure is empty: Wait
                if (currentAnimals.isEmpty()) {
                    Thread.sleep(50); // Sleep for a bit before checking
                    continue;
                }

                // Take the animals to their respective fields
                stockAnimals(currentAnimals);

                // Return to enclosure, assuming no leftover animals
                travelBackToEnclosure(0);

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
            addAnimalToField(field, quantity);


        }
    }

    private void travelToField(String field, int numberOfAnimalsCarried) throws InterruptedException {
        int travelTime = 10 + numberOfAnimalsCarried; // ✅ 10 ticks + 1 per animal carried
    
        System.out.println(tickSystem.getCurrentTick() + " Farmer-Thread 🚶 Carrying " 
            + numberOfAnimalsCarried + " " + field + " (Travel Time: " + travelTime + " ticks)");
    
        // Simulate travel time by waiting for each tick
        for (int i = 0; i < travelTime; i++) {
            tickSystem.waitForNextTick(); // ⏳ Simulate travel ticks
        }
    
        System.out.println(tickSystem.getCurrentTick() + " Farmer-Thread 🚜 Arrived at " + field);
    }
    

    private void addAnimalToField(Field field, int numberOfAnimals) throws InterruptedException {
        field.lockField();
        try {
            field.setAnimalCount(field.getAnimalCount() + numberOfAnimals);
            System.out.println("✅ Farmer stocked " + numberOfAnimals + " " + field.getName());
            field.signalBuyers(); // 🚀 Notify Buyers that stock is available
        } finally {
            field.unlockField();
        }
    }
    
    

    private void travelBackToEnclosure(int leftoverAnimals) throws InterruptedException {
        System.out.println("Going back to enclosure -> add ticks");
    }



    
}
