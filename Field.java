import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Field {
    // Maybe inherit field class from another class (or interface) that has locks
    // or sempahores already implemented
    private final String name;
    private int count;
    private int capacity = 99999;
    private final List<Buyer> buyerQueue = new ArrayList<>();

    private final Lock lock = new ReentrantLock(true);
    private final Condition notEmpty = lock.newCondition(); // condition to wait if empty
    private final Condition notFull = lock.newCondition();

    public Field(String name, int initialAnimalCount) {
        this.name = name;
        this.count = initialAnimalCount;
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

    public void addBuyerToQueue(Buyer buyer) {
        this.buyerQueue.add(buyer);
    }

    public int stock(int numberToAdd) throws InterruptedException {
        lock.lock();
        try {
            while (count == capacity) {
                notFull.await();
            }
            // If partial stocking is necessary
            int spaceLeft = capacity - count;
            int added = Math.min(spaceLeft, numberToAdd);
            count += added;
            System.out.println("🌾 " + name + " count after stocking: " + count);
            // Signal that the field is not empty anymore (buyers can proceed)
            notEmpty.signalAll();

            return added; // The farmer cna see how many were stocked 
        } finally {
            lock.unlock();
        }
    }

    public void buyOne(String buyerName, TickSystem tickSystem) throws InterruptedException {
        lock.lock();
        try {
            while (count == 0){
                System.out.println("⏳ " + buyerName + " is waiting for " + name + " to be stocked...");
                notEmpty.await(); // Wait until some animals are available
            }
            count--;

            System.out.println("🛒 " + buyerName + " bought 1 " + name + " (Remaining: " + count + ")");

            notFull.signalAll(); // Let the farmer know it's not full
        } finally {
            lock.unlock();
        }
    }
    // ------------------------------------------------------------------------
    // Lock/Unlock Exposed methods
    // ------------------------------------------------------------------------

    public void lockField() { 
        lock.lock();
    }
    public void unlockField() {
        lock.unlock();
    }
}
