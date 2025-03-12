import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Field {
    // Maybe inherit field class from another class (or interface) that has locks
    // or sempahores already implemented
    private final String name;
    private int animalCount;
    private final List<Buyer> buyerQueue = new ArrayList<>();

    private final Lock lock = new ReentrantLock(true);
    private final Condition notEmpty = lock.newCondition(); // condition to wait if empty

    public Field(String name, int initialAnimalCount) {
        this.name = name;
        this.animalCount = initialAnimalCount;
    }

    public String getName() {
        return name;
    }
    public int getAnimalCount() {
        return animalCount;
    }

    public void setAnimalCount(int newAnimalCount) {
        this.animalCount = newAnimalCount;
    }

    public void addBuyerToQueue(Buyer buyer) {
        this.buyerQueue.add(buyer);
    }

    public synchronized void awaitStock() throws InterruptedException {
        while (animalCount == 0) { // 🚨 Wait if no animals in the field
            System.out.println("⏳ Buyer is waiting for " + name + " to be stocked...");
            wait(); // 🚀 Waits until the Farmer calls `notifyAll()`
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
    public synchronized void signalBuyers() {
        System.out.println("🚀 Notifying Buyers that " + name + " is stocked");
        notifyAll(); // 🚀 Wake up all waiting Buyers!
    }
    
    
}
