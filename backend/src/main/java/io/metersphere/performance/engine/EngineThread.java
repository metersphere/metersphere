package io.metersphere.performance.engine;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class EngineThread implements Runnable {
    private Thread thread;
    protected volatile boolean stopped = false;
    protected boolean isDaemon = false;

    private final AtomicBoolean started = new AtomicBoolean(false);

    public abstract String getEngineName();

    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        stopped = false;
        this.thread = new Thread(this, getEngineName());
        this.thread.setDaemon(isDaemon);
        this.thread.start();
    }

    public void stop() {
        this.stop(false);
    }

    public void stop(final boolean interrupt) {
        if (!started.get()) {
            return;
        }
        this.stopped = true;

        if (interrupt) {
            this.thread.interrupt();
        }
    }


    public boolean isStopped() {
        return stopped;
    }

    public boolean isDaemon() {
        return isDaemon;
    }

    public void setDaemon(boolean daemon) {
        isDaemon = daemon;
    }
}