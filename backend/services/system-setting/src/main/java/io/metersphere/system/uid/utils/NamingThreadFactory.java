
package io.metersphere.system.uid.utils;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Named thread in ThreadFactory. If there is no specified name for thread, it
 * will auto detect using the invoker classname instead.
 */
public class NamingThreadFactory implements ThreadFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(NamingThreadFactory.class);

    /**
     * Thread name pre
     */
    private String name;
    /**
     * Is daemon thread
     */
    private boolean daemon;
    /**
     * UncaughtExceptionHandler
     */
    private UncaughtExceptionHandler uncaughtExceptionHandler;
    /**
     * Sequences for multi thread name prefix
     */
    private final ConcurrentHashMap<String, AtomicLong> sequences;

    /**
     * Constructors
     */
    public NamingThreadFactory() {
        this(null, false, null);
    }

    public NamingThreadFactory(String name) {
        this(name, false, null);
    }

    public NamingThreadFactory(String name, boolean daemon) {
        this(name, daemon, null);
    }

    public NamingThreadFactory(String name, boolean daemon, UncaughtExceptionHandler handler) {
        this.name = name;
        this.daemon = daemon;
        this.uncaughtExceptionHandler = handler;
        this.sequences = new ConcurrentHashMap<String, AtomicLong>();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(this.daemon);

        // If there is no specified name for thread, it will auto detect using the invoker classname instead.
        // Notice that auto detect may cause some performance overhead
        String prefix = this.name;
        if (StringUtils.isBlank(prefix)) {
            prefix = getInvoker(2);
        }
        thread.setName(prefix + "-" + getSequence(prefix));

        // no specified uncaughtExceptionHandler, just do logging.
        if (this.uncaughtExceptionHandler != null) {
            thread.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
        } else {
            thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    LOGGER.error("unhandled exception in thread: " + t.getId() + ":" + t.getName(), e);
                }
            });
        }

        return thread;
    }

    /**
     * Get the method invoker's class name
     *
     * @param depth
     * @return
     */
    private String getInvoker(int depth) {
        Exception e = new Exception();
        StackTraceElement[] stes = e.getStackTrace();
        if (stes.length > depth) {
            return ClassUtils.getShortClassName(stes[depth].getClassName());
        }
        return getClass().getSimpleName();
    }

    /**
     * Get sequence for different naming prefix
     *
     * @param invoker
     * @return
     */
    private long getSequence(String invoker) {
        AtomicLong r = this.sequences.get(invoker);
        if (r == null) {
            r = new AtomicLong(0);
            AtomicLong previous = this.sequences.putIfAbsent(invoker, r);
            if (previous != null) {
                r = previous;
            }
        }

        return r.incrementAndGet();
    }

    /**
     * Getters & Setters
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        this.uncaughtExceptionHandler = handler;
    }

}
