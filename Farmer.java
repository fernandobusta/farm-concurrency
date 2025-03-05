import java.util.Map;

public class Farmer implements Runnable {

    private final Enclosure enclosure;
    private final Map<String, Field> fields;
    private final String farmerName;

    public Farmer(String farmerName, Enclosure enclosure, Map<String, Field> fields) {
        this.farmerName = farmerName;
        this.enclosure = enclosure;
        this.fields = fields;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
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
            AddAnimalToField(field, quantity);


        }
    }

    private void travelToField(String field, int numberOfAnimalsCarried) throws InterruptedException {
        // Simulate the tick cost of walking from enclosure or field to the next field
        System.out.println("Travelling to field " + field + " -> Add ticks here");
    }

    private void AddAnimalToField(Field field, int numberOfAnimal) throws InterruptedException {
        // Add animals to the given field
        field.lockField(); // Lock the field so that no buyer can access it at the same time
        try {
            int previousAnimalCount = field.getAnimalCount();
            int newAnimalCount = previousAnimalCount + numberOfAnimal;
            field.setAnimalCount(newAnimalCount);

            // Signal to waiting buyers that field is no longer empty
            field.signalBuyers();
            
            System.out.println("Added " + numberOfAnimal + " to " + field.getName());

        } finally {
            field.unlockField();
        }

    }

    private void travelBackToEnclosure(int leftoverAnimals) throws InterruptedException {
        System.out.println("Going back to enclosure -> add ticks");
    }



    
}
