import java.util.HashMap;
import java.util.Map;

public class FarmSimulation {

    public static final long TICK_DURATION_MS = 100; // For minimal version, not too slow
    private static final long SIMULATION_RUNTIME_MS = 30_000; // simulation run time

    public static void main(String[] args) {
        // Create the enclosure (shared by delivery and farmer)
        Enclosure enclosure = new Enclosure();

        // Create the fields with initial values
        Field pigsField = new Field("pigs", 5);
        Field cowsField = new Field("cows", 5);
        Field sheepField = new Field("sheep", 5);
        Field llamasField = new Field("llamas", 5);
        Field chickensField = new Field("chicken", 5);

        // Put the fields in the map for Farmer and Buyer
        Map<String, Field> fieldsMap = new HashMap<>();
        fieldsMap.put("pigs", pigsField);
        fieldsMap.put("cows", cowsField);
        fieldsMap.put("sheep", sheepField);
        fieldsMap.put("llamas", llamasField);
        fieldsMap.put("chicken", chickensField);

        // Create the farmer
        Farmer singleFarmer = new Farmer("Farmer-01", enclosure, fieldsMap);
        Thread farmerThread = new Thread(singleFarmer, "Farmer-Thread");

        // Create the Delivery
        Delivery delivery = new Delivery(enclosure);
        Thread deliveryThread = new Thread(delivery, "Delivery-Thread");

        // Create the Buyer -> TODO: for loop here
        Buyer buyer = new Buyer(1, enclosure, fieldsMap);
        Thread buyerThread = new Thread(buyer, "Buyer-Thread");

        // Start all threads
        farmerThread.start();
        deliveryThread.start();
        buyerThread.start();
        

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
        buyerThread.interrupt();

        /** Ensure the main thread waits for te worker threads to fully shut down before
         * proceeding
         */
        try {
            farmerThread.join();
            deliveryThread.join();
            buyerThread.join();
            // And rest of the threads here...
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
