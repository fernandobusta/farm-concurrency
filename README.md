# Farm Concurrency Project

## Authors

- **Fernando Bustamante**
- **Jack Keenan**

## Project Overview

This document describes the design and implementation of our Farm Simulation project, developed in Java for the Concurrent and Distributed Programming module. The assignment simulates a farm with multiple fields (pigs, cows, sheep, llamas, chicken, etc.), an enclosure for new deliveries, Farmer threads to stock animals and Buyer threads to purchase animals. We measure time in ticks, allow partial stocking and handle concurrency with Java’s locking mechanisms.

## Key Goals

1. Continuously run a simulation of deliveries arriving (on average every 100 ticks).
2. Allow farmers to pick up animals from the enclosure and stock fields.
3. Handle buyers arriving (on average every 10 ticks) to purchase an animal.
4. Enforce concurrency constraints (only one farmer stocking a field at a time).
5. Ensure fairness so that no thread starves.

Below, we outline the functionality implemented, how to compile and run the code, the concurrency patterns employed and how we address fairness/starvation.

## List of Working Functionality

| Feature                                | Status     | Notes                                                                                          |
| -------------------------------------- | ---------- | ---------------------------------------------------------------------------------------------- |
| Single and Multiple Farmers            | ✅ Working | User can specify number of farmers at startup (default to 3).                                  |
| Multiple Fields                        | ✅ Working | User can choose more fields (default to 5).                                                    |
| Enclosure animal distribution          | ✅ Working | Animals are loaded based on priority strategy.                                                 |
| Deliveries arriving ~every 100 ticks   | ✅ Working | Probability or random wait logic in Buyer.                                                     |
| Capacity limits in fields              | ✅ Working | Fields can block if they are at capacity. Partial stocking is allowed.                         |
| Breaks taken by farmers                | ✅ Working | Every 200-300 ticks the farmer will take a 150 tick break.                                     |
| Fairness / starvation prevention       | ✅ Working | Using fair `ReentrantLock(true)` and conditions.                                               |
| Configurable parameters in config file | ✅ Working | Configure tick length, number of farmers and fields, delivery probability, etc.                |
| Logging / console output               | ✅ Working | Show ticks, thread IDs, stocked animals, wait times, etc.                                      |
| Graceful shutdown                      | ✅ Working | We interrupt threads after a set runtime in FarmSimulation.                                    |
| Animal loading strategy                | ✅ Working | First based on buyers waiting, secondly on current field count, finally fills rest of trailer. |
| Storing animals from delivery          | ✅ Working | Take animals from delivery and store in enclosure.                                             |
| Buyers queue and purchase              | ✅ Working | Buyers wait if no animals available, purchase when stocked.                                    |
| Farmers stocking fields                | ✅ Working | Farmers prioritise stocking based on demand.                                                   |
| Tick system for synchronisation        | ✅ Working | Ensures actions progress over time.                                                            |
| Multi-threaded synchronisation         | ✅ Working | Locks & conditions prevent race conditions.                                                    |
| Traveling System in between fields     | ✅ Working | Traveling to fields based on highest priority animal                                           |

## Running the Code

### Requirements

- **Java SE 21 (LTS)**

### Compilation

```bash
javac *.java
```

### Execution

```bash
java FarmSimulation
```

## Configuration of Simulation Parameters

Our simulation reads key parameters (number of farmers, fields, tick duration, etc.) from a file named `farmConfig.properties`. Each property follows a simple `key=value` format.

| Parameter                 | Default   | Description                                    |
| ------------------------- | --------- | ---------------------------------------------- |
| `numFarmers`              | 3         | Number of Farmer threads.                      |
| `numFields`               | 5         | How many Fields to simulate.                   |
| `numBuyers`               | 3         | Number of Buyer threads.                       |
| `fieldCapacity`           | 10        | Max animals each Field can hold.               |
| `initialFieldCount`       | 5         | Starting animals in each Field.                |
| `tickDuration`            | 100 ms    | Real-time milliseconds per simulation tick.    |
| `lowerBoundBreakInterval` | 200 ticks | Lowest farmer-break interval.                  |
| `upperBoundBreakInterval` | 300 ticks | Highest farmer-break interval.                 |
| `breakDuration`           | 150 ticks | How long each farmer’s break lasts.            |
| `deliveryProbability`     | 0.01      | Chance a new shipment arrives each tick (0–1). |

By default, the simulation runs for **30 seconds** and then interrupts all threads to shut down.

## Task Dependencies

1. **Time Management (Ticks)**:
   - A `TickSystem` class increments a global tick counter every X ms. All threads reference this to log current ticks.
2. **Delivery → Enclosure → Farmer → Field Flow**:
   - Delivery randomly adds animals to the Enclosure.
   - Farmer picks up to 10 animals from the Enclosure and stocks them in the Field.
   - Buyer tries to buy an animal from the Field.
3. **Concurrency**:
   - Multiple farmers can run in parallel.
   - Multiple buyers also arrive concurrently.
4. **Dependencies**:
   - Farmer depends on Enclosure (for picking animals) and Field (for stocking).
   - Buyer depends on Field.
   - Delivery depends on Enclosure.
   - FarmSimulation orchestrates all threads (Farmer, Delivery, Buyer) and runs the main loop.
5. **Partial Stocking Logic (if field capacity is reached):.**
   - The Farmer tries to stock animals one field at a time. If the field is full, the farmer will proceed to stock the next animal in his trailer, while holding the animals he could not stock previously. If it’s the last field to it has to stock, the farmer will go back to the Enclosure to load more animals and try again on the previous field.

## Patterns and Strategies for Concurrency

### Farmer

Each farmer is a separate thread (**implements Runnable**) that coordinates with other threads (buyers, deliveries) through careful synchronisation. The key concurrency points are:

#### Global Tick Synchronisation:

- The farmer waits for ticks from shared **TickSystem(waitForNextTick())**, ensuring that actions (loading, travelling, stocking, breaks) proceed in discrete time units.
- This prevents race conditions based on real-time scheduling, allowing a controlled pace where all threads operate in tandem.

#### Exclusive Access to Resource:

- **Enclosure**: Only one farmer can load animals at a time, done via a thread-safe **loadAnimalsIntoTrailer(...)**.
- **Fields**: When stocking animals, the farmer acquires a **lock** on the Field to ensure exclusive access. This avoids conflicts with other farmers stocking the same field or buyers trying to buy concurrently.
- Partial stocking is supported if the field is near capacity, preventing deadlocks around finite space.

#### Thread Methods and Flow: `run()` (main loop that repeatedly executes)

- Waits for a tick (via **tickSystem.waitForNextTick()**).
- Checks for break (**if current tick % breakInterval == 0, calls takeABreak()**).
- Loads animals from enclosure if the trailer has capacity.
- Travels to each relevant field, one at a time, calling **checkForBreak()** en route.
- Stocks animals in the field via **stockAnimalsInField(...)**, which locks the field and updates its count.
- Returns to enclosure, again simulating travel and checking for breaks.

#### Break Logic

- The method **checkForBreak()** determines if the farmer’s tick count matches **breakInterval**. If so, **takeABreak()** halts the farmer for **breakDuration** ticks, enforced by **tickSystem.waitForNTicks(...)**. This is integrated into both main stocking logic and traveling segments.
- While stocking **n** animals, the farmer checks each tick to see if a break interval has been reached. If it has, the code sets a **willNeedBreakAfter** flag to true, indicating the farmer should take a break immediately after completing the current stocking operation. This is implemented to avoid interruptions mid-stock.

### Buyer

Each Buyer is a **Runnable** thread that periodically purchases animals from a random field. It waits for a random interval based on the **TickSystem**.

#### Locking the Field

To buy an animal, the Buyer must acquire the lock on that field. If the field is:

- **Empty**, the buyer waits on a **notEmpty** condition (**field.notEmpty.await()**), ensuring the buyer remains blocked until a Farmer stocks animals.
- **Currently being stocked by a Farmer**, the buyer also waits for the lock to become available (only one thread can hold it at a time).

Once locked, the buyer decrements the field’s animal count by 1 and signals the field’s **notFull** condition.

#### Preventing Starvation

By using a **fair lock** (**ReentrantLock(true)**) and waiting on proper conditions (**notEmpty**), each buyer eventually accesses the field. If the field is consistently empty, the buyer naturally blocks until a Farmer stocks it, preventing busy-wait or wasted CPU.

### Delivery

The **Delivery** thread continuously adds animals to the enclosure at semi-random intervals, simulating external deliveries.

#### Synchronisation with Enclosure

When a new shipment arrives, the Delivery thread calls **enclosure.storeAnimalsFromDelivery**.  
Inside this function:

- The **enclosure locks** its internal data structures and updates counts.
- Then, it **signals** any waiting farmers (**notEmpty** condition).

This ensures that only one thread modifies the enclosure at a time, preventing race conditions.

#### Preventing Conflicts

Because the **Delivery** does not directly interact with fields (only with the **Enclosure**), it avoids deadlock with farmers by using a simple **lock-and-update** pattern.

- Farmers can only retrieve animals once the **delivery has finished updating** and signaled that the **enclosure is no longer empty**.

## Addressing Fairness and Starvation

- **Field Locking**: Only one thread (farmer or buyer) can acquire the field lock at a time. Once a farmer is done stocking, it signals any waiting buyers.
- **Farmer-Farmer Fairness**: If multiple farmers want to stock the same field, they queue for the lock in **FIFO** order. This ensures no single farmer starves.
- **Buyer Wait**: If the field is empty, the buyer calls **notEmpty.await()** and is signaled once a farmer stocks new animals. Because of the fair lock, each buyer eventually gets a turn to buy.
- **Delivery and Enclosure**: Only one farmer can pick from the enclosure at a time. If the enclosure is empty, the farmer waits on **notEmpty.await()**. New deliveries call **signalAll()**, ensuring the next waiting farmer can pick.
- **Partial Stocking**: If a field is near or at capacity, a farmer may only deposit some of the carried animals, then either tries a different field or returns to the enclosure. This approach prevents a scenario where the farmer forever attempts to fill a single full field.

Thus, no single thread can indefinitely block others because the system is event-driven and uses **fair locks**.

## Conclusion

Our farm simulation achieves all requirements: multiple farmers, fields, buyers, concurrency constraints, partial stocking, and fairness mechanisms. We have tested it with various configurations (including different capacities, numbers of farmers/fields/buyers, and different probabilities for deliveries/buyers) to confirm that it runs smoothly. The system logs events correctly and ends all threads gracefully.
