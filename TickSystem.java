import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TickSystem {
    private int currentTick = 0;
    private final int totalTicks;
    private final int tickDuration; // Time in milliseconds per tick
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TickSystem(int totalTicks, int tickDuration) {
        this.totalTicks = totalTicks;
        this.tickDuration = tickDuration;
    }

    public synchronized int getCurrentTick() {
        return currentTick;
    }

    public synchronized void waitForNextTick() throws InterruptedException {
        int lastTick = currentTick;
        while (currentTick == lastTick) {
            wait(); // Wait until next tick occurs
        }
    }

    // Wait for a specific number of ticks
    public synchronized void waitForNTicks(int ticks) throws InterruptedException {
        for (int i=0; i < ticks; i++) {
            waitForNextTick();
        }
    }

    private synchronized void nextTick() {
        currentTick = (currentTick + 1) % totalTicks; // Reset to 0 after a full day
        System.out.println("⏳ Tick " + currentTick);
        notifyAll(); // Wake up all waiting threads
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::nextTick, 0, tickDuration, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
