import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FarmSimulation {

    public static final long TICK_DURATION_MS = 10; // For minimal version, not too slow
    private static final long SIMULATION_RUNTIME_MS = 30_000; // simulation run time

    public static int[] showWelcomeScreen() {
        Scanner scanner = new Scanner(System.in);
        FarmBanner();
        printFarm();
        System.out.println("=======================================================================================================================================");
        int numFarmers = 3;
        int numFields = 5;

        boolean validResponse = false;
        while (!validResponse) {
            System.out.println("Press ENTER if you would like the traditional setup (3 farmers, 5 fields),");
            System.out.println("or type 'no' (then press ENTER) to manually set up variables and probabilities.");
            System.out.print("Choice: ");
            String response = scanner.nextLine().trim();
            if (response.isEmpty()) {
                System.out.println("You selected the traditional setup!");
                validResponse = true;
            } else if (response.equalsIgnoreCase("no")) {
                System.out.println("Let's begin!");
                System.out.print("Enter the number of farmers: ");
                numFarmers = readInt(scanner, 1, 10);
                System.out.print("Enter the number of fields (max 10): ");
                numFields = readInt(scanner, 1, 10);
                validResponse = true;
            } else {
                System.out.println("Unrecognised input. Please try again.");
            }
        }
        System.out.println("You can customise your farm setup here. Let's begin!");
        System.out.println();


        return new int[] { numFarmers, numFields };
    }

    private static int readInt(Scanner scanner, int min, int max) {
        while(true) {
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val < min || val > max) {
                    System.out.print("Invalid input. Please enter a number between "
                                     + min + " and " + max + ": ");
                } else {
                    return val;
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter an integer: ");
            }
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

    public static void main(String[] args) {

        int[] farmSetup = showWelcomeScreen();
        
        int numFarmers = farmSetup[0];
        int numFields = farmSetup[1];

        TickSystem tickSystem = new TickSystem(1000, 100); // 1000 ticks/day, 100ms per tick
        tickSystem.start(); // Start ticking
        
        // Create the enclosure (shared by delivery and farmer)

        // Create the fields with initial values
        ArrayList<String> fields = new ArrayList<>(List.of("pigs", "cows", "sheep", "llamas", "chicken", "bulls", "dogs", "cats", "rabbits", "horses"));
        Map<String, Field> fieldsMap = new HashMap<>();

        for (int i=1; i <= numFields; i++) {
            Field newField = new Field(fields.get(i), 0, tickSystem);
            fieldsMap.put(fields.get(i), newField);
        }

        Enclosure enclosure = new Enclosure(fieldsMap);

        // Create the farmer
        List<Thread> farmerThreads = new ArrayList<>();
        for (int i=1; i <= numFarmers; i++) {
            Farmer singleFarmer = new Farmer("Farmer-0"+i, enclosure, fieldsMap, tickSystem);
            Thread farmerThread = new Thread(singleFarmer, "Farmer-"+i);
            farmerThreads.add(farmerThread);
            farmerThread.start();
        }

        // Create the Delivery
        Delivery delivery = new Delivery(enclosure, tickSystem);
        Thread deliveryThread = new Thread(delivery, "Delivery-Thread");
        
        int numberOfBuyers = 3; // Change this number to adjust how many buyers are created
        List<Thread> buyerThreads = new ArrayList<>();

        for (int i = 1; i <= numberOfBuyers; i++) {
            Buyer buyer = new Buyer("Buyer-" + i, fieldsMap, tickSystem); // Each buyer gets a unique ID
            Thread buyerThread = new Thread(buyer, "Buyer-" + i);
            buyerThreads.add(buyerThread);
            buyerThread.start(); // Start the buyer thread
        }
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
}
