import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class FarmSimulation {

    public static long TICK_DURATION_MS = 10; // For minimal version, not too slow
    private static final long SIMULATION_RUNTIME_MS = 300_000; // simulation run time


    public static void main(String[] args) {

        showWelcomeScreen();

        Properties configProps = loadConfig("farmConfig.properties"); // Load properties file

        Random rand = new Random();
        int numFarmers = getIntProperty(configProps, "numFarmers", 3);
        int numFields = getIntProperty(configProps, "numFields", 5);
        int numBuyers = getIntProperty(configProps, "numBuyers", 3);
        int fieldCapacity = getIntProperty(configProps, "fieldCapacity", 10);
        int initialFieldCount = getIntProperty(configProps, "initialFieldCount", 5);
        int tickDuration = getIntProperty(configProps, "tickDuration", 100);
        int breakDuration = getIntProperty(configProps, "breakDuration", 150);
        int lowerBoundBreakInterval = getIntProperty(configProps, "lowerBoundBreakInterval", 300);
        int upperBoundBreakInterval = getIntProperty(configProps, "upperBoundBreakInterval", 300);
        double deliveryProbability = getDoubleProperty(configProps, "deliveryProbability", 100);

        if (initialFieldCount > fieldCapacity) {
            System.err.println("Initial field count can't be bigger than capacity. Using defaults 5 and 10 respectively.");
            fieldCapacity = 10;
            initialFieldCount = 5;
        }
        if (lowerBoundBreakInterval > upperBoundBreakInterval) {
            System.err.println("Lower bound break interval can't be bigger than upper bound. Using defaults 300 and 300 respectively.");
            lowerBoundBreakInterval = 100;
            upperBoundBreakInterval = 200;
        }
        if (deliveryProbability < 0 || deliveryProbability > 1) {
            System.err.println("Delivery probability must be between 0 and 1. Using default 0.01.");
            deliveryProbability = 0.01;
        }
        int breakRange = (upperBoundBreakInterval - lowerBoundBreakInterval) + 1;

        // Showing loaded config
        System.out.println("============== Loaded Config ==============");
        System.out.println("  Farmers: " + numFarmers);
        System.out.println("  Fields : " + numFields);
        System.out.println("  Buyers: " + numBuyers);
        System.out.println("  Field Capacity: " + fieldCapacity);
        System.out.println("  Initial Field Count: " + initialFieldCount);
        System.out.println("  Tick Duration: " + tickDuration + " ms");
        System.out.println("  Break Duration: " + breakDuration + " ticks");
        System.out.println("  Break Interval lower bound: " + lowerBoundBreakInterval + " ticks");
        System.out.println("  Break Interval upper bound: " + upperBoundBreakInterval + " ticks");
        System.out.println("  Delivery Probability: " + deliveryProbability + " ticks");


        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Press ENTER to start the simulation!");
            scanner.nextLine(); 
        } catch (Exception e) {
            System.err.println("Error occurred while reading input. Exiting simulation. Please try again.");
        }

        TickSystem tickSystem = new TickSystem(1000, tickDuration); // 1000 ticks/day, 100ms per tick
        tickSystem.start(); // Start ticking
        
        // =========================== Fields ===========================
        // Create the fields with initial values
        ArrayList<String> fields = new ArrayList<>(List.of("pigs", "cows", "sheep", "llamas", "chicken", "bulls", "dogs", "cats", "rabbits", "horses"));
        Map<String, Field> fieldsMap = new HashMap<>();
        
        // Cut the list
        if (numFields < fields.size()) {
            fields = new ArrayList<>(fields.subList(0, numFields));
        }
        
        for (int i=0; i < numFields; i++) {
            Field newField = new Field(fields.get(i), initialFieldCount, tickSystem, fieldCapacity);
            fieldsMap.put(fields.get(i), newField);
        }
        
        // =========================== Enclosure ===========================
        Enclosure enclosure = new Enclosure(fieldsMap);

        // =========================== Delivery ===========================
        Delivery delivery = new Delivery(enclosure, tickSystem, fields, deliveryProbability);
        Thread deliveryThread = new Thread(delivery, "Delivery-Thread");
        deliveryThread.start();
        
        // =========================== Farmers ===========================
        List<Thread> farmerThreads = new ArrayList<>();
        for (int i=1; i <= numFarmers; i++) {
            int randomBreakInterval = lowerBoundBreakInterval + rand.nextInt(breakRange);
            Farmer singleFarmer = new Farmer("Farmer-"+i, enclosure, fieldsMap, tickSystem, breakDuration, randomBreakInterval);
            Thread farmerThread = new Thread(singleFarmer, "Farmer-"+i);
            farmerThreads.add(farmerThread);
            farmerThread.start();
        }

        // =========================== Buyers ===========================
        List<Thread> buyerThreads = new ArrayList<>();

        for (int i = 1; i <= numBuyers; i++) {
            Buyer buyer = new Buyer("Buyer-" + i, fieldsMap, tickSystem); // Each buyer gets a unique ID
            Thread buyerThread = new Thread(buyer, "Buyer-" + i);
            buyerThreads.add(buyerThread);
            buyerThread.start(); // Start the buyer thread
        }
        

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
        for (Thread farmerThread : farmerThreads) {
            farmerThread.interrupt();
        }
        deliveryThread.interrupt();
        for (Thread buyerThread : buyerThreads) { // Interrupt all buyer threads
            buyerThread.interrupt();
        }

        /** Ensure the main thread waits for te worker threads to fully shut down before
         * proceeding
         */
        try {
            for (Thread farmerThread : farmerThreads) { // Join all buyer threads
                farmerThread.join();
            }
            deliveryThread.join();
            for (Thread buyerThread : buyerThreads) { // Join all buyer threads
                buyerThread.join();
            }
            // And rest of the threads here...
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void showWelcomeScreen() {
        FarmBanner();
        printFarm();
        System.out.println("=======================================================================================================================================");
    }

    private static Properties loadConfig(String filename) {
        Properties props = new Properties();
        try (FileInputStream ff = new FileInputStream(filename)) {
            props.load(ff);
        } catch (IOException e) {
            System.err.println("Could not load config file: " + filename + ". Using defaults instead.");
        }
        return props;
    }

    private static int getIntProperty(Properties props, String key, int defaultValue) {
        // If key not present or invalid, return default
        String val = props.getProperty(key);
        if (val == null) {
            System.err.println("val was null");
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for " + key + ": " + val + ". Using default " + defaultValue);
            return defaultValue;
        }
    }

    private static float getDoubleProperty(Properties props, String key, float defaultValue) {
        // If key not present or invalid, return default
        String val = props.getProperty(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(val.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for " + key + ": " + val + ". Using default " + defaultValue);
            return defaultValue;
        }
    }

    public static void FarmBanner() {
        String banner = """
 __      _____________.__                                  __             __  .__               _____                      
/  \\    /  \\_   _____/|  |   ____  ____   _____   ____   _/  |_  ____   _/  |_|  |__   ____   _/ ____\\____ _______  _____  
\\   \\/\\/   /|    __)_ |  | _/ ___\\/  _ \\ /     \\_/ __ \\  \\   __\\/  _ \\  \\   __\\  |  \\_/ __ \\  \\   __\\\\__  \\\\_  __ \\/     \\ 
 \\        / |        \\|  |_\\  \\__(  <_> )  Y Y  \\  ___/   |  | (  <_> )  |  | |   Y  \\  ___/   |  |   / __ \\|  | \\/  Y Y  \\
  \\__/\\  / /_______  /|____/\\___  >____/|__|_|  /\\___  >  |__|  \\____/   |__| |___|  /\\___  >  |__|  (____  /__|  |__|_|  /
       \\/          \\/           \\/            \\/     \\/                            \\/     \\/              \\/            \\/ 
""";

        System.out.println(banner);
    }
    public static void printFarm() {
        // Copied from: https://www.angelfire.com/ca/mathcool/farm.html
        String farm = """
                                                         +&-                             _ (.".) _
                                                       _.-^-._    .--.                  '-'/. .\\'-'
                                                    .-'   _   '-. |__|                    /_   _\\     _...._
                                                   /     |_|     \\|  |                   (` o o \")---`   .::'.
                                                  /               \\  |                    /"---"` .::'    '   \\
                                                 /|               |\\ |                    |:  .::.     /  .::;|
                                                  |    _______    |  |                    |'  ::'   .:|    ':||
                                                  |    |--|--|    |  |                     \\   \\  \\ '\\     /\\\\
                              |---|---|---|---|---|    |==|==|    |  |                      \\`;-'| |-.-'-,  \\ |)
                              |---|---|---|---|---|    |==|==|    |  |                      ( | ( | `-uu ( |
                              |---|---|---|---|---|    |==|==|    |  |                       ||  ||    || ||
                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^                     /_( /_(   /_(/_(
                   """;

        System.out.println(farm);
    }


}
