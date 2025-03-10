import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Buyer implements Runnable {
    private final int buyerId;
    private final Map<String, Field> fields;
    private final Enclosure enclosure;
    private final Random rand;


    public Buyer(int buyerId, Enclosure enclosure, Map<String, Field> fields) {
        this.buyerId = buyerId;
        this.fields = fields;
        this.enclosure = enclosure;
        this.rand = new Random();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep((int) (Math.random() * 5000) + 2000); // Random delay before buying
                buyRandomAnimal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Try to buy a random animal from a field
    private void buyRandomAnimal() throws InterruptedException {
        // Pick a random field from map
        List<String> keys = new ArrayList<>(fields.keySet());
        String animal = keys.get(rand.nextInt(keys.size()));
        Field field = fields.get(animal);

        removeAnimalFromField(field, 1);
    }

    private void removeAnimalFromField(Field field, int numberofAnimal) throws InterruptedException {
        // Remove (buy) animal from give field
        field.lockField();
        try {
            int previousAnimalCount = field.getAnimalCount();
            if (previousAnimalCount == 0) {
                // release the lock and wait until a stock signal has been received
            }
            int newAnimalCount = previousAnimalCount - numberofAnimal;
            field.setAnimalCount(newAnimalCount);

            // Singal to Buyers or farmers ?????
            field.signalBuyers();
            System.out.println(this.buyerId + " bought " + numberofAnimal + " " + field.getName());
        } finally {
            field.unlockField();
        }
    }
}
