import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Buyer implements Runnable {
    private final String buyerName;
    private final Map<String, Field> fields;
    private final Random rand;
    private final TickSystem tickSystem; // Store tick system
    private int tickItGotIntoQueue = -1;


    public Buyer(String buyerName, Map<String, Field> fields, TickSystem tickSystem) {
        this.buyerName = buyerName;
        this.fields = fields;
        this.rand = new Random();
        this.tickSystem = tickSystem; // Assign tick system
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int nextBuyTick = 5 + rand.nextInt(11);
                tickSystem.waitForNTicks(nextBuyTick);
                buyRandomAnimal();
    
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Try to buy a random animal from a field
    private void buyRandomAnimal() throws InterruptedException {
        List<String> keys = new ArrayList<>(fields.keySet());
        String animal = keys.get(rand.nextInt(keys.size()));
        int currentTick = tickSystem.getCurrentTick(); 
        tickItGotIntoQueue = currentTick;

        Field field = fields.get(animal);
        field.buyOne(buyerName, this.tickItGotIntoQueue);
    }

    public int getWaitTime(int currentTick) {
        int waitedTicks = currentTick - tickItGotIntoQueue;
        return waitedTicks;
    }
}
