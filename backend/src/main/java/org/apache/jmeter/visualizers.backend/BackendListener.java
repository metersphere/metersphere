//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.jmeter.visualizers.backend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 当前上下文类加载为 NewDriver 的类加载器，这里使用系统类加载器加载 APIBackendListenerClient

public class BackendListener extends AbstractTestElement implements Backend, Serializable, SampleListener, TestStateListener, NoThreadClone, Remoteable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(BackendListener.class);
    public static final String CLASSNAME = "classname";
    public static final String QUEUE_SIZE = "QUEUE_SIZE";
    private static final Object LOCK = new Object();
    public static final String ARGUMENTS = "arguments";
    private Class<?> clientClass;
    public static final String DEFAULT_QUEUE_SIZE = "5000";
    private static final transient SampleResult FINAL_SAMPLE_RESULT = new SampleResult();
    private static final Map<String, BackendListener.ListenerClientData> queuesByTestElementName = new ConcurrentHashMap();
    private transient String myName;
    private transient BackendListener.ListenerClientData listenerClientData;

    public BackendListener() {
        this.setArguments(new Arguments());
    }

    public Object clone() {
        BackendListener clone = (BackendListener)super.clone();
        clone.clientClass = this.clientClass;
        return clone;
    }

    private Class<?> initClass() {
        String name = this.getClassname().trim();

        try {
            // 当前上下文类加载为 NewDriver 的类加载器，这里使用系统类加载器加载 APIBackendListenerClient
            return Class.forName(name, false, this.getClass().getClassLoader());
        } catch (Exception var3) {
            log.error("{}\tException initialising: {}", new Object[]{this.whoAmI(), name, var3});
            return null;
        }
    }

    private String whoAmI() {
        return Thread.currentThread().getName() + "@" + Integer.toHexString(this.hashCode()) + "-" + this.getName();
    }

    public void sampleOccurred(SampleEvent event) {
        Arguments args = this.getArguments();
        BackendListenerContext context = new BackendListenerContext(args);
        SampleResult sr = this.listenerClientData.client.createSampleResult(context, event.getResult());
        if (sr == null) {
            if (log.isDebugEnabled()) {
                log.debug("{} => Dropping SampleResult: {}", this.getName(), event.getResult());
            }

        } else {
            try {
                if (!this.listenerClientData.queue.offer(sr)) {
                    this.listenerClientData.queueWaits.add(1L);
                    long t1 = System.nanoTime();
                    this.listenerClientData.queue.put(sr);
                    long t2 = System.nanoTime();
                    this.listenerClientData.queueWaitTime.add(t2 - t1);
                }
            } catch (Exception var9) {
                log.error("sampleOccurred, failed to queue the sample", var9);
            }

        }
    }

    private static void sendToListener(BackendListenerClient backendListenerClient, BackendListenerContext context, List<SampleResult> sampleResults) {
        if (!sampleResults.isEmpty()) {
            backendListenerClient.handleSampleResults(sampleResults, context);
            sampleResults.clear();
        }

    }

    private static BackendListenerClient createBackendListenerClientImpl(Class<?> clientClass) {
        if (clientClass == null) {
            return new BackendListener.ErrorBackendListenerClient();
        } else {
            try {
                return (BackendListenerClient)clientClass.getDeclaredConstructor().newInstance();
            } catch (Exception var2) {
                log.error("Exception creating: {}", clientClass, var2);
                return new BackendListener.ErrorBackendListenerClient();
            }
        }
    }

    public void testStarted() {
        this.testStarted("local");
    }

    public void testStarted(String host) {
        if (log.isDebugEnabled()) {
            log.debug("{}\ttestStarted({})", this.whoAmI(), host);
        }

        String size = this.getQueueSize();

        int queueSize;
        try {
            queueSize = Integer.parseInt(size);
        } catch (NumberFormatException var12) {
            log.warn("Invalid queue size '{}' defaulting to {}", size, "5000");
            queueSize = Integer.parseInt("5000");
        }

        Object var4 = LOCK;
        synchronized(LOCK) {
            this.myName = this.getName();
            this.listenerClientData = (BackendListener.ListenerClientData)queuesByTestElementName.get(this.myName);
            if (this.listenerClientData == null) {
                this.clientClass = this.initClass();
                BackendListenerClient backendListenerClient = createBackendListenerClientImpl(this.clientClass);
                BackendListenerContext context = new BackendListenerContext((Arguments)this.getArguments().clone());
                this.listenerClientData = new BackendListener.ListenerClientData();
                this.listenerClientData.queue = new ArrayBlockingQueue(queueSize);
                this.listenerClientData.queueWaits = new LongAdder();
                this.listenerClientData.queueWaitTime = new LongAdder();
                this.listenerClientData.latch = new CountDownLatch(1);
                this.listenerClientData.client = backendListenerClient;
                if (log.isInfoEnabled()) {
                    log.info("{}: Starting worker with class: {} and queue capacity: {}", new Object[]{this.getName(), this.clientClass, this.getQueueSize()});
                }

                BackendListener.Worker worker = new BackendListener.Worker(backendListenerClient, (Arguments)this.getArguments().clone(), this.listenerClientData);
                worker.setDaemon(true);
                worker.start();
                if (log.isInfoEnabled()) {
                    log.info("{}: Started  worker with class: {}", this.getName(), this.clientClass);
                }

                try {
                    backendListenerClient.setupTest(context);
                } catch (Exception var10) {
                    throw new IllegalStateException("Failed calling setupTest", var10);
                }

                queuesByTestElementName.put(this.myName, this.listenerClientData);
            }

            this.listenerClientData.instanceCount++;
        }
    }

    public void testEnded(String host) {
        Object var2 = LOCK;
        synchronized(LOCK) {
            BackendListener.ListenerClientData listenerClientDataForName = (BackendListener.ListenerClientData)queuesByTestElementName.get(this.myName);
            if (log.isDebugEnabled()) {
                log.debug("testEnded called on instance {}#{}", this.myName, listenerClientDataForName.instanceCount);
            }

            if (listenerClientDataForName != null) {
                listenerClientDataForName.instanceCount--;
                if (listenerClientDataForName.instanceCount > 0) {
                    return;
                }

                queuesByTestElementName.remove(this.myName);
            } else {
                log.error("No listener client data found for BackendListener {}", this.myName);
            }
        }

        try {
            this.listenerClientData.queue.put(FINAL_SAMPLE_RESULT);
        } catch (Exception var6) {
            log.warn("testEnded() with exception: {}", var6.getMessage(), var6);
        }

        if (this.listenerClientData.queueWaits.longValue() > 0L) {
            log.warn("QueueWaits: {}; QueueWaitTime: {} (nanoseconds), you may need to increase queue capacity, see property 'backend_queue_capacity'", this.listenerClientData.queueWaits, this.listenerClientData.queueWaitTime);
        }

        try {
            this.listenerClientData.latch.await();
            BackendListenerContext context = new BackendListenerContext(this.getArguments());
            this.listenerClientData.client.teardownTest(context);
        } catch (Exception var5) {
            throw new IllegalStateException("Failed calling teardownTest", var5);
        }
    }

    public void testEnded() {
        this.testEnded("local");
    }

    public void sampleStarted(SampleEvent e) {
    }

    public void sampleStopped(SampleEvent e) {
    }

    public void setArguments(Arguments args) {
        args.removeArgument("useRegexpForSamplersList", "false");
        this.setProperty(new TestElementProperty("arguments", args));
    }

    public Arguments getArguments() {
        return (Arguments)this.getProperty("arguments").getObjectValue();
    }

    public void setClassname(String classname) {
        this.setProperty("classname", classname);
    }

    public String getClassname() {
        return this.getPropertyAsString("classname");
    }

    public void setQueueSize(String queueSize) {
        this.setProperty("QUEUE_SIZE", queueSize, "5000");
    }

    public String getQueueSize() {
        return this.getPropertyAsString("QUEUE_SIZE", "5000");
    }

    static class ErrorBackendListenerClient extends AbstractBackendListenerClient {
        ErrorBackendListenerClient() {
        }

        public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
            BackendListener.log.warn("ErrorBackendListenerClient#handleSampleResult called, noop");
            Thread.yield();
        }
    }

    private static final class Worker extends Thread {
        private final BackendListener.ListenerClientData listenerClientData;
        private final BackendListenerContext context;
        private final BackendListenerClient backendListenerClient;

        private Worker(BackendListenerClient backendListenerClient, Arguments arguments, BackendListener.ListenerClientData listenerClientData) {
            this.listenerClientData = listenerClientData;
            arguments.addArgument("TestElement.name", this.getName());
            this.context = new BackendListenerContext(arguments);
            this.backendListenerClient = backendListenerClient;
        }

        public void run() {
            boolean isDebugEnabled = BackendListener.log.isDebugEnabled();
            ArrayList sampleResults = new ArrayList(this.listenerClientData.queue.size());

            try {
                try {
                    boolean endOfLoop = false;

                    while(!endOfLoop) {
                        if (isDebugEnabled) {
                            BackendListener.log.debug("Thread: {} taking SampleResult from queue: {}", Thread.currentThread().getName(), this.listenerClientData.queue.size());
                        }

                        SampleResult sampleResult = (SampleResult)this.listenerClientData.queue.take();
                        if (isDebugEnabled) {
                            BackendListener.log.debug("Thread: {} took SampleResult: {}, isFinal: {}", new Object[]{Thread.currentThread().getName(), sampleResult, sampleResult == BackendListener.FINAL_SAMPLE_RESULT});
                        }

                        while(!(endOfLoop = sampleResult == BackendListener.FINAL_SAMPLE_RESULT) && sampleResult != null) {
                            sampleResults.add(sampleResult);
                            if (isDebugEnabled) {
                                BackendListener.log.debug("Thread: {} polling from queue: {}", Thread.currentThread().getName(), this.listenerClientData.queue.size());
                            }

                            sampleResult = (SampleResult)this.listenerClientData.queue.poll();
                            if (isDebugEnabled) {
                                BackendListener.log.debug("Thread: {} took from queue: {}, isFinal: {}", new Object[]{Thread.currentThread().getName(), sampleResult, sampleResult == BackendListener.FINAL_SAMPLE_RESULT});
                            }
                        }

                        if (isDebugEnabled) {
                            BackendListener.log.debug("Thread: {} exiting with FINAL EVENT: {}, null: {}", new Object[]{Thread.currentThread().getName(), sampleResult == BackendListener.FINAL_SAMPLE_RESULT, sampleResult == null});
                        }

                        BackendListener.sendToListener(this.backendListenerClient, this.context, sampleResults);
                        if (!endOfLoop) {
                            LockSupport.parkNanos(100L);
                        }
                    }
                } catch (InterruptedException var8) {
                    Thread.currentThread().interrupt();
                }

                BackendListener.sendToListener(this.backendListenerClient, this.context, sampleResults);
                BackendListener.log.info("Worker ended");
            } finally {
                this.listenerClientData.latch.countDown();
            }

        }
    }

    private static final class ListenerClientData {
        private BackendListenerClient client;
        private BlockingQueue<SampleResult> queue;
        private LongAdder queueWaits;
        private LongAdder queueWaitTime;
        private int instanceCount;
        private CountDownLatch latch;

        private ListenerClientData() {
        }
    }
}
