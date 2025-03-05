import java.util.List;

public class Buyer implements Runnable {
    private final int buyerId;
    private final List<Field> fields; // List of all fields
    private final Enclosure enclosure; // Reference to the enclosure

    public Buyer(int buyerId, List<Field> fields, Enclosure enclosure) {
        this.buyerId = buyerId;
        this.fields = fields;
        this.enclosure = enclosure;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep((int) (Math.random() * 5000) + 2000); // Random delay before buying
                buyRandomAnimal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Try to buy a random animal from a field
    private void buyRandomAnimal() {
        Field field = fields.get((int) (Math.random() * fields.size())); // Pick a random field
        String animalType = field.getFieldType();

        synchronized (field) {
            while (field.getAnimalCount() == 0) {
                System.out.println("Buyer " + buyerId + " waiting for " + animalType);
                notifyFarmerForPriority(animalType); // Tell farmer we need this animal
                try {
                    field.wait(); // Wait for a farmer to stock the field
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Buy the animal
            field.removeAnimal(1);
            System.out.println("Buyer " + buyerId + " bought 1 " + animalType);
        }
    }

    // Notify farmers that this animal is needed (so they prioritize it)
    private void notifyFarmerForPriority(String animalType) {
        synchronized (enclosure) {
            enclosure.storeFromDelivery(List.of(animalType)); // Make sure this animal is stocked next
        }
    }
}
