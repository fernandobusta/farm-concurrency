import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Field {
    private final String name;
    private int count;
    private final int capacity;
    private final TickSystem tickSystem; // Store tick system

    private final Lock lock = new ReentrantLock(true);
    private final Condition notEmpty = lock.newCondition(); // condition to wait if empty
    private final Condition notFull = lock.newCondition();

    private final AtomicInteger buyersWaiting = new AtomicInteger(0);

    public Field(String name, int initialAnimalCount, TickSystem tickSystem, int capacity) {

        this.name = name;
        this.count = initialAnimalCount;
        this.tickSystem = tickSystem;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }
    public int getCount() {
        return count;
    }

    public void setCount(int newAnimalCount) {
        this.count = newAnimalCount;
    }

    public int getCapacity() {
        return capacity;
    }
    
    public int getBuyersWaiting() {
        return buyersWaiting.get(); // No lock needed
    }

    public void addBuyerToQueue() {
        buyersWaiting.incrementAndGet();
    }

    public void removeBuyerFromQueue() {
        buyersWaiting.decrementAndGet();
    }
    public void buyOne(String buyerName, int tickItGotIntoQueue) throws InterruptedException {
        lock.lock();
        try {
            while (count == 0){
                System.out.println("⏳ " + buyerName + " is waiting for " + name + " to be stocked...");
                addBuyerToQueue();
                notEmpty.await(); // Wait until some animals are available
            }
            count--;

            int waitedTicks = tickSystem.getCurrentTick() - tickItGotIntoQueue;
            System.out.println("🛒 " + tickSystem.getCurrentTick() + " | " + buyerName + " collected 1: " + name + " from field after waiting " + waitedTicks + " ticks." + "(Remaining " + name + ":" + count + ")");

            System.out.println("Tick" + tickSystem.getCurrentTick() + " 🛒 " + buyerName + " bought 1 " + name + " (Remaining: " + count + ")");
            removeBuyerFromQueue();
            tickSystem.waitForNTicks(1); // Buyer waits for 1 tick after buying

            notFull.signalAll(); // Let the farmer know it's not full
        } finally {
            lock.unlock();
        }
    }
    // Available methods for Farmers to stock animals in field (The purpose of this is to accurately assign breaks to farmers)
    public void lockField() {
        lock.lock();
    }
    public void unlockField() {
        lock.unlock();
    }
    public void signalAllNotEmpty() {
        notEmpty.signalAll();
    }

}