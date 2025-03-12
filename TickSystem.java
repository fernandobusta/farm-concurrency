public class TickSystem {
    private int currentTick = 0;
    private final int totalTicks;
    private final int tickDuration; // Time in milliseconds per tick

    public TickSystem(int totalTicks, int tickDuration) {
        this.totalTicks = totalTicks;
        this.tickDuration = tickDuration;
    }

    public synchronized int getCurrentTick() {
        return currentTick;
    }

    public synchronized void waitForNextTick() throws InterruptedException {
        wait(); // 🚀 Threads will wait until the next tick
    }

    public synchronized void nextTick() {
        currentTick++;
        System.out.println("⏳ Tick " + currentTick);
        notifyAll(); // 🚀 Wake up all waiting threads
    }

    public void start() {
        new Thread(() -> {
            while (currentTick < totalTicks) {
                try {
                    Thread.sleep(tickDuration); // Control tick speed
                    nextTick();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}
