package io.metersphere.commons.utils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Weak Concurrent Hash Map Solution which stores the keys and values only for a specific amount of time, and then expires after that
 * time.
 *
 * <pre>
 *
 * // Create a Map Object
 * long expiryInMillis = 1 * 60 * 1000;	// 1 minute
 * io.metersphere.commons.utils.WeakConcurrentHashMap&lt;String, Object&gt; map = new io.metersphere.commons.utils.WeakConcurrentHashMap&lt;String, Object&gt;(expiryInMillis);
 *
 * // Use it
 * map.put(&quot;key&quot;, valueObject);
 * Object valueObject = map.get(&quot;key&quot;);
 *
 * // quit using it
 * map.quitMap();
 * </pre>
 * <p>
 * And to check if the map is alive
 *
 * <pre>
 * if (map.isAlive()) {
 * 	// Your operations on map
 * }
 * </pre>
 *
 * @param <K>
 * @param <V>
 * @author Vivekananthan M
 */
public class WeakConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    private Map<K, Long> timeMap = new ConcurrentHashMap<K, Long>();
    private WeakConcurrentHashMapListener<K, V> listener;
    private long expiryInMillis;
    private boolean mapAlive = true;

    public WeakConcurrentHashMap() {
        this.expiryInMillis = 10000;
        initialize();
    }

    public WeakConcurrentHashMap(WeakConcurrentHashMapListener<K, V> listener) {
        this.listener = listener;
        this.expiryInMillis = 10000;
        initialize();
    }

    public WeakConcurrentHashMap(long expiryInMillis) {
        this.expiryInMillis = expiryInMillis;
        initialize();
    }

    public WeakConcurrentHashMap(long expiryInMillis, WeakConcurrentHashMapListener<K, V> listener) {
        this.expiryInMillis = expiryInMillis;
        this.listener = listener;
        initialize();
    }

    void initialize() {
        new CleanerThread().start();
    }

    public void registerRemovalListener(WeakConcurrentHashMapListener<K, V> listener) {
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if trying to insert values into map after quiting
     */
    @Override
    public V put(K key, V value) {
        if (!mapAlive) {
            throw new IllegalStateException("WeakConcurrent Hashmap is no more alive.. Try creating a new one.");    // No I18N
        }
        Date date = new Date();
        timeMap.put(key, date.getTime());
        V returnVal = super.put(key, value);
        if (listener != null) {
            listener.notifyOnAdd(key, value);
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if trying to insert values into map after quiting
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (!mapAlive) {
            throw new IllegalStateException("WeakConcurrent Hashmap is no more alive.. Try creating a new one.");    // No I18N
        }
        for (K key : m.keySet()) {
            put(key, m.get(key));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if trying to insert values into map after quiting
     */
    @Override
    public V putIfAbsent(K key, V value) {
        if (!mapAlive) {
            throw new IllegalStateException("WeakConcurrent Hashmap is no more alive.. Try creating a new one.");    // No I18N
        }
        if (!containsKey(key)) {
            return put(key, value);
        } else {
            return get(key);
        }
    }

    /**
     * Should call this method when it's no longer required
     */
    public void quitMap() {
        mapAlive = false;
    }

    public boolean isAlive() {
        return mapAlive;
    }

    /**
     * This thread performs the cleaning operation on the concurrent hashmap once in a specified interval. This wait interval is half of the
     * time from the expiry time.
     */
    class CleanerThread extends Thread {

        @Override
        public void run() {
            while (mapAlive) {
                cleanMap();
                try {
                    Thread.sleep(expiryInMillis / 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void cleanMap() {
            long currentTime = new Date().getTime();
            for (K key : timeMap.keySet()) {
                if (currentTime > (timeMap.get(key) + expiryInMillis)) {
                    V value = remove(key);
                    timeMap.remove(key);
                    if (listener != null) {
                        listener.notifyOnRemoval(key, value);
                    }
                }
            }
        }
    }
}
