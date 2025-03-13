import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class FarmSimulation {

    public static final long TICK_DURATION_MS = 500; // For minimal version, not too slow
    private static final long SIMULATION_RUNTIME_MS = 30_000; // simulation run time

    public static void main(String[] args) {

        TickSystem tickSystem = new TickSystem(1000, 100); // 1000 ticks/day, 100ms per tick
        tickSystem.start(); // Start ticking
        
        // Create the enclosure (shared by delivery and farmer)
        Enclosure enclosure = new Enclosure();

        // Create the fields with initial values
        Field pigsField = new Field("pigs", 0);
        Field cowsField = new Field("cows", 0);
        Field sheepField = new Field("sheep", 0);
        Field llamasField = new Field("llamas", 0);
        Field chickensField = new Field("chicken", 0);

        // Put the fields in the map for Farmer and Buyer
        Map<String, Field> fieldsMap = new HashMap<>();
        fieldsMap.put("pigs", pigsField);
        fieldsMap.put("cows", cowsField);
        fieldsMap.put("sheep", sheepField);
        fieldsMap.put("llamas", llamasField);
        fieldsMap.put("chicken", chickensField);

        // Create the farmer
        Farmer singleFarmer = new Farmer("Farmer-01", enclosure, fieldsMap, tickSystem);
        Thread farmerThread = new Thread(singleFarmer, "Farmer-Thread");

        // Create the Delivery
        Delivery delivery = new Delivery(enclosure, tickSystem);
        Thread deliveryThread = new Thread(delivery, "Delivery-Thread");

        // Create the Buyer -> TODO: for loop here
        
        int numberOfBuyers = 3; // Change this number to adjust how many buyers are created
        List<Thread> buyerThreads = new ArrayList<>();

        for (int i = 1; i <= numberOfBuyers; i++) {
            Buyer buyer = new Buyer("Buyer-" + i, fieldsMap, tickSystem); // Each buyer gets a unique ID
            Thread buyerThread = new Thread(buyer, "Buyer-" + i);
            buyerThreads.add(buyerThread);
            buyerThread.start(); // Start the buyer thread
        }

        

        // Start all threads
        farmerThread.start();
        deliveryThread.start();
        

        /** The current thread (FarmSimualtion main) will sleep while the other threads
         * keep running in the background. The main() thread will be sleeping for SIMULATION_RUNTIME_MS
         * milliseconds.
         */
        try {
            Thread.sleep(SIMULATION_RUNTIME_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop all threads
        farmerThread.interrupt();
        deliveryThread.interrupt();
        for (Thread buyerThread : buyerThreads) { // Interrupt all buyer threads
            buyerThread.interrupt();
        }

        /** Ensure the main thread waits for te worker threads to fully shut down before
         * proceeding
         */
        try {
            farmerThread.join();
            deliveryThread.join();
            for (Thread buyerThread : buyerThreads) { // Join all buyer threads
                buyerThread.join();
            }
            // And rest of the threads here...
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
