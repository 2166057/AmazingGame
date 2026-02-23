package net.wattpadpremium.server;

import lombok.Getter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ExtendableTimer {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong remainingSeconds = new AtomicLong(0);

    private ScheduledFuture<?> task;
    private final Runnable onFinish;
    private final Consumer<Long> onTick;
    @Getter
    private volatile boolean running = false;

    private final long maximumSeconds;

    public ExtendableTimer(long initialSeconds, Runnable onFinish, Consumer<Long> onTick) {
        this.maximumSeconds = initialSeconds;
        this.remainingSeconds.set(initialSeconds);
        this.onFinish = onFinish;
        this.onTick = onTick;
    }

    public synchronized void start() {
        if (running) return;

        running = true;

        task = scheduler.scheduleAtFixedRate(() -> {
            long timeLeft = remainingSeconds.decrementAndGet();
            onTick.accept(timeLeft);
            if (timeLeft <= 0) {
                stopInternal();
                onFinish.run();
            }

        }, 1, 1, TimeUnit.SECONDS);
    }

    public synchronized void addSeconds(long seconds) {
        if (!running) return;
        if (remainingSeconds.get() + seconds > maximumSeconds) {
            remainingSeconds.set(maximumSeconds);
            return;
        }
        remainingSeconds.addAndGet(seconds);
        return;
    }

    public synchronized void stop() {
        stopInternal();
    }

    private void stopInternal() {
        running = false;
        if (task != null) {
            task.cancel(false);
        }
    }

    public long getRemainingSeconds() {
        return remainingSeconds.get();
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}