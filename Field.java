import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Field {
    // Maybe inherit field class from another class (or interface) that has locks
    // or sempahores already implemented
    private final String name;
    private int count;
    private final int capacity = 10;
    private final TickSystem tickSystem; // Store tick system

    private final Lock lock = new ReentrantLock(true);
    private final Condition notEmpty = lock.newCondition(); // condition to wait if empty
    private final Condition notFull = lock.newCondition();

    private final Queue<String> buyerQueue = new LinkedList<>();

    public Field(String name, int initialAnimalCount, TickSystem tickSystem) {

        this.name = name;
        this.count = initialAnimalCount;
        this.tickSystem = tickSystem;
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

    public void addBuyerToQueue(String buyerName) {
        lock.lock();
        try {
            buyerQueue.add(buyerName);
        } finally {
            lock.unlock();
        }
    }

    public void removeBuyerFromQueue() {
        lock.lock();
        try {
            if (!buyerQueue.isEmpty()) {
                buyerQueue.poll(); // 🚀 Remove the Buyer from the queue
            }
        } finally {
            lock.unlock();
        }
    }

    public int getBuyersWaiting() {
        lock.lock();
        try {
            return buyerQueue.size(); // 🚀 Return number of Buyers waiting
        } finally {
            lock.unlock();
        }
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
            tickSystem.waitForNTicks(added);
            // Signal that the field is not empty anymore (buyers can proceed)
            notEmpty.signalAll();

            return added; // The farmer cna see how many were stocked 
        } finally {
            lock.unlock();
        }
    }

    public void buyOne(String buyerName) throws InterruptedException {
        lock.lock();
        try {
            while (count == 0){
                System.out.println("⏳ " + buyerName + " is waiting for " + name + " to be stocked...");
                addBuyerToQueue(buyerName);
                notEmpty.await(); // Wait until some animals are available
            }
            count--;

            System.out.println("Tick" + tickSystem.getCurrentTick() + " 🛒 " + buyerName + " bought 1 " + name + " (Remaining: " + count + ")");
            removeBuyerFromQueue();
            tickSystem.waitForNTicks(1); // Buyer waits for 1 tick after buying

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
