/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.threads;

import io.metersphere.api.jmeter.MsResultCollector;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.IteratingController;
import org.apache.jmeter.control.TransactionSampler;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.*;
import org.apache.jmeter.threads.JMeterContext.TestLogicalAction;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.timers.TimerService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.util.JMeterError;
import org.apache.jorphan.util.JMeterStopTestException;
import org.apache.jorphan.util.JMeterStopTestNowException;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * The JMeter interface to the sampling process, allowing JMeter to see the
 * timing, add listeners for sampling events and to stop the sampling process.
 */
public class JMeterThread implements Runnable, Interruptible {
    private static final Logger log = LoggerFactory.getLogger(JMeterThread.class);

    public static final String PACKAGE_OBJECT = "JMeterThread.pack"; // $NON-NLS-1$

    public static final String LAST_SAMPLE_OK = "JMeterThread.last_sample_ok"; // $NON-NLS-1$

    private static final String TRUE = Boolean.toString(true); // i.e. "true"

    /**
     * How often to check for shutdown during ramp-up, default 1000ms
     */
    private static final int RAMPUP_GRANULARITY =
            JMeterUtils.getPropDefault("jmeterthread.rampup.granularity", 1000); // $NON-NLS-1$

    private static final float TIMER_FACTOR = JMeterUtils.getPropDefault("timer.factor", 1.0f);

    private static final TimerService TIMER_SERVICE = TimerService.getInstance();

    private static final float ONE_AS_FLOAT = 1.0f;

    private static final boolean APPLY_TIMER_FACTOR = Float.compare(TIMER_FACTOR, ONE_AS_FLOAT) != 0;

    private final Controller threadGroupLoopController;

    private final HashTree testTree;

    private final TestCompiler compiler;

    private final JMeterThreadMonitor monitor;

    private final JMeterVariables threadVars;

    // Note: this is only used to implement TestIterationListener#testIterationStart
    // Since this is a frequent event, it makes sense to create the list once rather than scanning each time
    // The memory used will be released when the thread finishes
    private final Collection<TestIterationListener> testIterationStartListeners;

    private final Collection<SampleMonitor> sampleMonitors;

    private final ListenerNotifier notifier;

    /*
     * The following variables are set by StandardJMeterEngine.
     * This is done before start() is called, so the values will be published to the thread safely
     * TODO - consider passing them to the constructor, so that they can be made final
     * (to avoid adding lots of parameters, perhaps have a parameter wrapper object.
     */
    private String threadName;

    private int initialDelay = 0;

    private int threadNum = 0;

    private long startTime = 0;

    private long endTime = 0;

    private final boolean isSameUserOnNextIteration;

    // based on this scheduler is enabled or disabled
    private boolean scheduler = false;

    // Gives access to parent thread threadGroup
    private AbstractThreadGroup threadGroup;

    private StandardJMeterEngine engine = null; // For access to stop methods.

    /*
     * The following variables may be set/read from multiple threads.
     */
    private volatile boolean running; // may be set from a different thread

    private volatile boolean onErrorStopTest;

    private volatile boolean onErrorStopTestNow;

    private volatile boolean onErrorStopThread;

    private volatile boolean onErrorStartNextLoop;

    private volatile Sampler currentSamplerForInterruption;

    private final ReentrantLock interruptLock = new ReentrantLock(); // ensure that interrupt cannot overlap with shutdown

    public JMeterThread(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier note) {
        this(test, monitor, note, false);
    }

    public JMeterThread(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier note, Boolean isSameUserOnNextIteration) {
        this.monitor = monitor;
        threadVars = new JMeterVariables();
        testTree = test;
        compiler = new TestCompiler(testTree);
        threadGroupLoopController = (Controller) testTree.getArray()[0];
        SearchByClass<TestIterationListener> threadListenerSearcher = new SearchByClass<>(TestIterationListener.class); // TL - IS
        test.traverse(threadListenerSearcher);
        testIterationStartListeners = threadListenerSearcher.getSearchResults();
        SearchByClass<SampleMonitor> sampleMonitorSearcher = new SearchByClass<>(SampleMonitor.class);
        test.traverse(sampleMonitorSearcher);
        sampleMonitors = sampleMonitorSearcher.getSearchResults();
        notifier = note;
        running = true;
        this.isSameUserOnNextIteration = isSameUserOnNextIteration;
    }

    public void setInitialContext(JMeterContext context) {
        threadVars.putAll(context.getVariables());
    }

    /**
     * Enable the scheduler for this JMeterThread.
     *
     * @param sche flag whether the scheduler should be enabled
     */
    public void setScheduled(boolean sche) {
        this.scheduler = sche;
    }

    /**
     * Set the StartTime for this Thread.
     *
     * @param stime the StartTime value.
     */
    public void setStartTime(long stime) {
        startTime = stime;
    }

    /**
     * Get the start time value.
     *
     * @return the start time value.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Set the EndTime for this Thread.
     *
     * @param etime the EndTime value.
     */
    public void setEndTime(long etime) {
        endTime = etime;
    }

    /**
     * Get the end time value.
     *
     * @return the end time value.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Check if the scheduled time is completed.
     */
    private void stopSchedulerIfNeeded() {
        long now = System.currentTimeMillis();
        if (now >= endTime) {
            running = false;
            log.info("Stopping because end time detected by thread: {}", threadName);
        }
    }

    /**
     * Wait until the scheduled start time if necessary
     */
    private void startScheduler() {
        long delay = startTime - System.currentTimeMillis();
        delayBy(delay, "startScheduler");
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void run() {
        // threadContext is not thread-safe, so keep within thread
        JMeterContext threadContext = JMeterContextService.getContext();
        LoopIterationListener iterationListener = null;
        try {
            iterationListener = initRun(threadContext);
            while (running) {
                Sampler sam = threadGroupLoopController.next();
                while (running && sam != null) {
                    processSampler(sam, null, threadContext);
                    threadContext.cleanAfterSample();

                    boolean lastSampleOk = TRUE.equals(threadContext.getVariables().get(LAST_SAMPLE_OK));
                    // restart of the next loop
                    // - was requested through threadContext
                    // - or the last sample failed AND the onErrorStartNextLoop option is enabled
                    if (threadContext.getTestLogicalAction() != TestLogicalAction.CONTINUE
                            || (onErrorStartNextLoop && !lastSampleOk)) {
                        if (log.isDebugEnabled() && onErrorStartNextLoop
                                && threadContext.getTestLogicalAction() != TestLogicalAction.CONTINUE) {
                            log.debug("Start Next Thread Loop option is on, Last sample failed, starting next thread loop");
                        }
                        if (onErrorStartNextLoop && !lastSampleOk) {
                            triggerLoopLogicalActionOnParentControllers(sam, threadContext, JMeterThread::continueOnThreadLoop);
                        } else {
                            switch (threadContext.getTestLogicalAction()) {
                                case BREAK_CURRENT_LOOP:
                                    triggerLoopLogicalActionOnParentControllers(sam, threadContext, JMeterThread::breakOnCurrentLoop);
                                    break;
                                case START_NEXT_ITERATION_OF_THREAD:
                                    triggerLoopLogicalActionOnParentControllers(sam, threadContext, JMeterThread::continueOnThreadLoop);
                                    break;
                                case START_NEXT_ITERATION_OF_CURRENT_LOOP:
                                    triggerLoopLogicalActionOnParentControllers(sam, threadContext, JMeterThread::continueOnCurrentLoop);
                                    break;
                                default:
                                    break;
                            }
                        }
                        threadContext.setTestLogicalAction(TestLogicalAction.CONTINUE);
                        sam = null;
                        setLastSampleOk(threadContext.getVariables(), true);
                    } else {
                        sam = threadGroupLoopController.next();
                    }
                }

                // It would be possible to add finally for Thread Loop here
                if (threadGroupLoopController.isDone()) {
                    running = false;
                    log.info("Thread is done: {}", threadName);
                }
            }
        }
        // Might be found by controller.next()
        catch (JMeterStopTestException e) { // NOSONAR
            if (log.isInfoEnabled()) {
                log.info("Stopping Test: {}", e.toString());
            }
            shutdownTest();
        } catch (JMeterStopTestNowException e) { // NOSONAR
            if (log.isInfoEnabled()) {
                log.info("Stopping Test Now: {}", e.toString());
            }
            stopTestNow();
        } catch (JMeterStopThreadException e) { // NOSONAR
            if (log.isInfoEnabled()) {
                log.info("Stop Thread seen for thread {}, reason: {}", getThreadName(), e.toString());
            }
        } catch (Exception | JMeterError e) {
            log.error("Test failed!", e);
        } catch (ThreadDeath e) {
            throw e; // Must not ignore this one
        } finally {
            currentSamplerForInterruption = null; // prevent any further interrupts
            interruptLock.lock();  // make sure current interrupt is finished, prevent another starting yet
            try {
                threadContext.clear();
                log.info("Thread finished: {}", threadName);
                threadFinished(iterationListener);
                monitor.threadFinished(this); // Tell the monitor we are done
                JMeterContextService.removeContext(); // Remove the ThreadLocal entry
            } finally {
                interruptLock.unlock(); // Allow any pending interrupt to complete (OK because currentSampler == null)
            }
        }
    }

    /**
     * Trigger break/continue/switch to next thread Loop  depending on consumer implementation
     *
     * @param sampler       Sampler Base sampler
     * @param threadContext
     * @param consumer      Consumer that will process the tree of elements up to root node
     */
    private void triggerLoopLogicalActionOnParentControllers(Sampler sampler, JMeterContext threadContext,
                                                             Consumer<FindTestElementsUpToRootTraverser> consumer) {
        TransactionSampler transactionSampler = null;
        if (sampler instanceof TransactionSampler) {
            transactionSampler = (TransactionSampler) sampler;
        }

        Sampler realSampler = findRealSampler(sampler);
        if (realSampler == null) {
            throw new IllegalStateException(
                    "Got null subSampler calling findRealSampler for:" +
                            (sampler != null ? sampler.getName() : "null") + ", sampler:" + sampler);
        }
        // Find parent controllers of current sampler
        FindTestElementsUpToRootTraverser pathToRootTraverser = new FindTestElementsUpToRootTraverser(realSampler);
        testTree.traverse(pathToRootTraverser);

        consumer.accept(pathToRootTraverser);

        // bug 52968
        // When using Start Next Loop option combined to TransactionController.
        // if an error occurs in a Sample (child of TransactionController)
        // then we still need to report the Transaction in error (and create the sample result)
        if (transactionSampler != null) {
            SamplePackage transactionPack = compiler.configureTransactionSampler(transactionSampler);
            doEndTransactionSampler(transactionSampler, null, transactionPack, threadContext);
        }
    }

    /**
     * Executes a continue of current loop, equivalent of "continue" in algorithm.
     * As a consequence it ends the first loop it finds on the path to root
     *
     * @param pathToRootTraverser {@link FindTestElementsUpToRootTraverser}
     */
    private static void continueOnCurrentLoop(FindTestElementsUpToRootTraverser pathToRootTraverser) {
        List<Controller> controllersToReinit = pathToRootTraverser.getControllersToRoot();
        for (Controller parentController : controllersToReinit) {
            if (parentController instanceof AbstractThreadGroup) {
                AbstractThreadGroup tg = (AbstractThreadGroup) parentController;
                tg.startNextLoop();
            } else if (parentController instanceof IteratingController) {
                ((IteratingController) parentController).startNextLoop();
                break;
            } else {
                parentController.triggerEndOfLoop();
            }
        }
    }

    /**
     * Executes a break of current loop, equivalent of "break" in algorithm.
     * As a consequence it ends the first loop it finds on the path to root
     *
     * @param pathToRootTraverser {@link FindTestElementsUpToRootTraverser}
     */
    private static void breakOnCurrentLoop(FindTestElementsUpToRootTraverser pathToRootTraverser) {
        List<Controller> controllersToReinit = pathToRootTraverser.getControllersToRoot();
        for (Controller parentController : controllersToReinit) {
            if (parentController instanceof AbstractThreadGroup) {
                AbstractThreadGroup tg = (AbstractThreadGroup) parentController;
                tg.breakThreadLoop();
            } else if (parentController instanceof IteratingController) {
                ((IteratingController) parentController).breakLoop();
                break;
            } else {
                parentController.triggerEndOfLoop();
            }
        }
    }

    /**
     * Executes a restart of Thread loop, equivalent of "continue" in algorithm but on Thread Loop.
     * As a consequence it ends all loop on the path to root
     *
     * @param pathToRootTraverser {@link FindTestElementsUpToRootTraverser}
     */
    private static void continueOnThreadLoop(FindTestElementsUpToRootTraverser pathToRootTraverser) {
        List<Controller> controllersToReinit = pathToRootTraverser.getControllersToRoot();
        for (Controller parentController : controllersToReinit) {
            if (parentController instanceof AbstractThreadGroup) {
                AbstractThreadGroup tg = (AbstractThreadGroup) parentController;
                tg.startNextLoop();
            } else {
                parentController.triggerEndOfLoop();
            }
        }
    }

    /**
     * Find the Real sampler (Not TransactionSampler) that really generated an error
     * The Sampler provided is not always the "real" one, it can be a TransactionSampler,
     * if there are some other controllers (SimpleController or other implementations) between this TransactionSampler and the real sampler,
     * triggerEndOfLoop will not be called for those controllers leaving them in "ugly" state.
     * the following method will try to find the sampler that really generate an error
     *
     * @return {@link Sampler}
     */
    private Sampler findRealSampler(Sampler sampler) {
        Sampler realSampler = sampler;
        while (realSampler instanceof TransactionSampler) {
            realSampler = ((TransactionSampler) realSampler).getSubSampler();
        }
        return realSampler;
    }

    /**
     * Process the current sampler, handling transaction samplers.
     *
     * @param current       sampler
     * @param parent        sampler
     * @param threadContext
     * @return SampleResult if a transaction was processed
     */
    private SampleResult processSampler(Sampler current, Sampler parent, JMeterContext threadContext) {
        SampleResult transactionResult = null;
        // Check if we are running a transaction
        TransactionSampler transactionSampler = null;
        // Find the package for the transaction
        SamplePackage transactionPack = null;
        try {
            if (current instanceof TransactionSampler) {
                transactionSampler = (TransactionSampler) current;
                transactionPack = compiler.configureTransactionSampler(transactionSampler);

                // Check if the transaction is done
                if (transactionSampler.isTransactionDone()) {
                    transactionResult = doEndTransactionSampler(transactionSampler,
                            parent,
                            transactionPack,
                            threadContext);
                    // Transaction is done, we do not have a sampler to sample
                    current = null;
                } else {
                    Sampler prev = current;
                    // It is the sub sampler of the transaction that will be sampled
                    current = transactionSampler.getSubSampler();
                    if (current instanceof TransactionSampler) {
                        SampleResult res = processSampler(current, prev, threadContext);// recursive call
                        threadContext.setCurrentSampler(prev);
                        current = null;
                        if (res != null) {
                            transactionSampler.addSubSamplerResult(res);
                        }
                    }
                }
            }

            // Check if we have a sampler to sample
            if (current != null) {
                executeSamplePackage(current, transactionSampler, transactionPack, threadContext);
            }

            if (scheduler) {
                // checks the scheduler to stop the iteration
                stopSchedulerIfNeeded();
            }

        } catch (JMeterStopTestException e) { // NOSONAR
            if (log.isInfoEnabled()) {
                log.info("Stopping Test: {}", e.toString());
            }
            shutdownTest();
        } catch (JMeterStopTestNowException e) { // NOSONAR
            if (log.isInfoEnabled()) {
                log.info("Stopping Test with interruption of current samplers: {}", e.toString());
            }
            stopTestNow();
        } catch (JMeterStopThreadException e) { // NOSONAR
            if (log.isInfoEnabled()) {
                log.info("Stopping Thread: {}", e.toString());
            }
            stopThread();
        } catch (Exception e) {
            if (current != null) {
                log.error("Error while processing sampler: '{}'.", current.getName(), e);
            } else {
                log.error("Error while processing sampler.", e);
            }
        }
        if (!running
                && transactionResult == null
                && transactionSampler != null
                && transactionPack != null) {
            transactionResult = doEndTransactionSampler(transactionSampler, parent, transactionPack, threadContext);
        }

        return transactionResult;
    }

    private void fillThreadInformation(SampleResult result,
                                       int nbActiveThreadsInThreadGroup,
                                       int nbTotalActiveThreads) {
        result.setGroupThreads(nbActiveThreadsInThreadGroup);
        result.setAllThreads(nbTotalActiveThreads);
        result.setThreadName(threadName);
    }

    /**
     * Execute the sampler with its pre/post processors, timers, assertions
     * Broadcast the result to the sample listeners
     */
    private void executeSamplePackage(Sampler current,
                                      TransactionSampler transactionSampler,
                                      SamplePackage transactionPack,
                                      JMeterContext threadContext) {

        threadContext.setCurrentSampler(current);
        // Get the sampler ready to sample
        SamplePackage pack = compiler.configureSampler(current);
        runPreProcessors(pack.getPreProcessors());

        // Hack: save the package for any transaction controllers
        threadVars.putObject(PACKAGE_OBJECT, pack);

        delay(pack.getTimers());
        SampleResult result = null;
        if (running) {
            Sampler sampler = pack.getSampler();
            // 执行前发给监听
            List<SampleListener> sampleListeners = pack.getSampleListeners();
            if (CollectionUtils.isNotEmpty(sampleListeners)) {
                for (SampleListener sampleListener : sampleListeners) {
                    try {
                        if (sampleListener instanceof MsResultCollector) {
                            SampleEvent event = new SampleEvent(null, current.getPropertyAsString("MS-RESOURCE-ID"), threadVars);
                            sampleListener.sampleStarted(event);
                            break;
                        }
                    } catch (RuntimeException e) {
                        log.error("自定义提前发送监听失败.", e);
                    }
                }
            }
            //======
            result = doSampling(threadContext, sampler);
        }
        // If we got any results, then perform processing on the result
        if (result != null) {
            if (!result.isIgnore()) {
                int nbActiveThreadsInThreadGroup = threadGroup.getNumberOfThreads();
                int nbTotalActiveThreads = JMeterContextService.getNumberOfThreads();
                fillThreadInformation(result, nbActiveThreadsInThreadGroup, nbTotalActiveThreads);
                SampleResult[] subResults = result.getSubResults();
                if (subResults != null) {
                    for (SampleResult subResult : subResults) {
                        fillThreadInformation(subResult, nbActiveThreadsInThreadGroup, nbTotalActiveThreads);
                    }
                }
                threadContext.setPreviousResult(result);
                runPostProcessors(pack.getPostProcessors());
                checkAssertions(pack.getAssertions(), result, threadContext);
                // PostProcessors can call setIgnore, so reevaluate here
                if (!result.isIgnore()) {
                    // Do not send subsamples to listeners which receive the transaction sample
                    List<SampleListener> sampleListeners = getSampleListeners(pack, transactionPack, transactionSampler);
                    notifyListeners(sampleListeners, result);
                }
                compiler.done(pack);
                // Add the result as subsample of transaction if we are in a transaction
                if (transactionSampler != null && !result.isIgnore()) {
                    transactionSampler.addSubSamplerResult(result);
                }
            } else {
                // This call is done by checkAssertions() , as we don't call it
                // for isIgnore, we explictely call it here
                setLastSampleOk(threadContext.getVariables(), result.isSuccessful());
            }
            // Check if thread or test should be stopped
            if (result.isStopThread() || (!result.isSuccessful() && onErrorStopThread)) {
                stopThread();
            }
            if (result.isStopTest() || (!result.isSuccessful() && onErrorStopTest)) {
                shutdownTest();
            }
            if (result.isStopTestNow() || (!result.isSuccessful() && onErrorStopTestNow)) {
                stopTestNow();
            }
            if (result.getTestLogicalAction() != TestLogicalAction.CONTINUE) {
                threadContext.setTestLogicalAction(result.getTestLogicalAction());
            }
        } else {
            compiler.done(pack); // Finish up
        }
    }

    /**
     * Call sample on Sampler handling:
     * <ul>
     *  <li>setting up ThreadContext</li>
     *  <li>initializing sampler if needed</li>
     *  <li>positioning currentSamplerForInterruption for potential interruption</li>
     *  <li>Playing SampleMonitor before and after sampling</li>
     *  <li>resetting currentSamplerForInterruption</li>
     * </ul>
     *
     * @param threadContext {@link JMeterContext}
     * @param sampler       {@link Sampler}
     * @return {@link SampleResult}
     */
    private SampleResult doSampling(JMeterContext threadContext, Sampler sampler) {
        sampler.setThreadContext(threadContext);
        sampler.setThreadName(threadName);
        TestBeanHelper.prepare(sampler);

        // Perform the actual sample
        currentSamplerForInterruption = sampler;
        if (!sampleMonitors.isEmpty()) {
            for (SampleMonitor sampleMonitor : sampleMonitors) {
                if (sampleMonitor instanceof TestElement) {
                    TestBeanHelper.prepare((TestElement) sampleMonitor);
                }
                sampleMonitor.sampleStarting(sampler);
            }
        }
        try {
            return sampler.sample(null);
        } finally {
            if (!sampleMonitors.isEmpty()) {
                for (SampleMonitor sampleMonitor : sampleMonitors) {
                    sampleMonitor.sampleEnded(sampler);
                }
            }
            currentSamplerForInterruption = null;
        }
    }

    private SampleResult doEndTransactionSampler(
            TransactionSampler transactionSampler, Sampler parent,
            SamplePackage transactionPack, JMeterContext threadContext) {
        // Get the transaction sample result
        SampleResult transactionResult = transactionSampler.getTransactionResult();
        fillThreadInformation(transactionResult, threadGroup.getNumberOfThreads(), JMeterContextService.getNumberOfThreads());

        // Check assertions for the transaction sample
        checkAssertions(transactionPack.getAssertions(), transactionResult, threadContext);
        // Notify listeners with the transaction sample result
        if (!(parent instanceof TransactionSampler)) {
            notifyListeners(transactionPack.getSampleListeners(), transactionResult);
        }
        compiler.done(transactionPack);
        return transactionResult;
    }

    /**
     * Get the SampleListeners for the sampler. Listeners who receive transaction sample
     * will not be in this list.
     *
     * @param samplePack
     * @param transactionPack
     * @param transactionSampler
     * @return the listeners who should receive the sample result
     */
    private List<SampleListener> getSampleListeners(SamplePackage samplePack, SamplePackage transactionPack, TransactionSampler transactionSampler) {
        List<SampleListener> sampleListeners = samplePack.getSampleListeners();
        // Do not send subsamples to listeners which receive the transaction sample
        if (transactionSampler != null) {
            List<SampleListener> onlySubSamplerListeners = new ArrayList<>();
            List<SampleListener> transListeners = transactionPack.getSampleListeners();
            for (SampleListener listener : sampleListeners) {
                // Check if this instance is present in transaction listener list
                boolean found = false;
                for (SampleListener trans : transListeners) {
                    // Check for the same instance
                    if (trans == listener) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    onlySubSamplerListeners.add(listener);
                }
            }
            sampleListeners = onlySubSamplerListeners;
        }
        return sampleListeners;
    }

    /**
     * Store {@link JMeterThread#LAST_SAMPLE_OK} in JMeter Variables context
     */
    private void setLastSampleOk(JMeterVariables variables, boolean value) {
        variables.put(LAST_SAMPLE_OK, Boolean.toString(value));
    }

    /**
     * @param threadContext
     * @return the iteration listener
     */
    private IterationListener initRun(JMeterContext threadContext) {
        threadVars.putObject(JMeterVariables.VAR_IS_SAME_USER_KEY, isSameUserOnNextIteration);
        threadContext.setVariables(threadVars);
        threadContext.setThreadNum(getThreadNum());
        setLastSampleOk(threadVars, true);
        threadContext.setThread(this);
        threadContext.setThreadGroup(threadGroup);
        threadContext.setEngine(engine);
        testTree.traverse(compiler);
        if (scheduler) {
            // set the scheduler to start
            startScheduler();
        }

        rampUpDelay(); // TODO - how to handle thread stopped here
        if (log.isInfoEnabled()) {
            log.info("Thread started: {}", Thread.currentThread().getName());
        }
        /*
         * Setting SamplingStarted before the controllers are initialised allows
         * them to access the running values of functions and variables (however
         * it does not seem to help with the listeners)
         */
        threadContext.setSamplingStarted(true);

        threadGroupLoopController.initialize();
        IterationListener iterationListener = new IterationListener();
        threadGroupLoopController.addIterationListener(iterationListener);

        threadStarted();
        return iterationListener;
    }

    private void threadStarted() {
        JMeterContextService.incrNumberOfThreads();
        threadGroup.incrNumberOfThreads();
        GuiPackage gp = GuiPackage.getInstance();
        if (gp != null) {// check there is a GUI
            gp.getMainFrame().updateCounts();
        }
        ThreadListenerTraverser startup = new ThreadListenerTraverser(true);
        testTree.traverse(startup); // call ThreadListener.threadStarted()
    }

    private void threadFinished(LoopIterationListener iterationListener) {
        ThreadListenerTraverser shut = new ThreadListenerTraverser(false);
        testTree.traverse(shut); // call ThreadListener.threadFinished()
        JMeterContextService.decrNumberOfThreads();
        threadGroup.decrNumberOfThreads();
        GuiPackage gp = GuiPackage.getInstance();
        if (gp != null) {// check there is a GUI
            gp.getMainFrame().updateCounts();
        }
        if (iterationListener != null) { // probably not possible, but check anyway
            threadGroupLoopController.removeIterationListener(iterationListener);
        }
    }

    // N.B. This is only called at the start and end of a thread, so there is not
    // necessary to cache the search results, thus saving memory
    static class ThreadListenerTraverser implements HashTreeTraverser {
        private final boolean isStart;

        ThreadListenerTraverser(boolean start) {
            isStart = start;
        }

        @Override
        public void addNode(Object node, HashTree subTree) {
            if (node instanceof ThreadListener) {
                ThreadListener tl = (ThreadListener) node;
                if (isStart) {
                    try {
                        tl.threadStarted();
                    } catch (Exception e) {
                        log.error("Error calling threadStarted", e);
                    }
                } else {
                    try {
                        tl.threadFinished();
                    } catch (Exception e) {
                        log.error("Error calling threadFinished", e);
                    }
                }
            }
        }

        @Override
        public void subtractNode() {
            // NOOP
        }

        @Override
        public void processPath() {
            // NOOP
        }
    }

    public String getThreadName() {
        return threadName;
    }

    /**
     * Set running flag to false which will interrupt JMeterThread on next flag test.
     * This is a clean shutdown.
     */
    public void stop() { // Called by StandardJMeterEngine, TestAction and AccessLogSampler
        running = false;
        log.info("Stopping: {}", threadName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean interrupt() {
        interruptLock.lock();
        try {
            Sampler samp = currentSamplerForInterruption; // fetch once; must be done under lock
            if (samp instanceof Interruptible) { // (also protects against null)
                if (log.isWarnEnabled()) {
                    log.warn("Interrupting: {} sampler: {}", threadName, samp.getName());
                }
                try {
                    boolean found = ((Interruptible) samp).interrupt();
                    if (!found) {
                        log.warn("No operation pending");
                    }
                    return found;
                } catch (Exception e) { // NOSONAR
                    if (log.isWarnEnabled()) {
                        log.warn("Caught Exception interrupting sampler: {}", e.toString());
                    }
                }
            } else if (samp != null) {
                if (log.isWarnEnabled()) {
                    log.warn("Sampler is not Interruptible: {}", samp.getName());
                }
            }
        } finally {
            interruptLock.unlock();
        }
        return false;
    }

    /**
     * Clean shutdown of test, which means wait for end of current running samplers
     */
    private void shutdownTest() {
        running = false;
        log.info("Shutdown Test detected by thread: {}", threadName);
        if (engine != null) {
            engine.askThreadsToStop();
        }
    }

    /**
     * Stop test immediately by interrupting running samplers
     */
    private void stopTestNow() {
        running = false;
        log.info("Stop Test Now detected by thread: {}", threadName);
        if (engine != null) {
            engine.stopTest();
        }
    }

    /**
     * Clean Exit of current thread
     */
    private void stopThread() {
        running = false;
        log.info("Stop Thread detected by thread: {}", threadName);
    }

    private void checkAssertions(List<Assertion> assertions, SampleResult parent, JMeterContext threadContext) {
        for (Assertion assertion : assertions) {
            TestBeanHelper.prepare((TestElement) assertion);
            if (assertion instanceof AbstractScopedAssertion) {
                AbstractScopedAssertion scopedAssertion = (AbstractScopedAssertion) assertion;
                String scope = scopedAssertion.fetchScope();
                if (scopedAssertion.isScopeParent(scope)
                        || scopedAssertion.isScopeAll(scope)
                        || scopedAssertion.isScopeVariable(scope)) {
                    processAssertion(parent, assertion);
                }
                if (scopedAssertion.isScopeChildren(scope)
                        || scopedAssertion.isScopeAll(scope)) {
                    recurseAssertionChecks(parent, assertion, 3);
                }
            } else {
                processAssertion(parent, assertion);
            }
        }
        setLastSampleOk(threadContext.getVariables(), parent.isSuccessful());
    }

    private void recurseAssertionChecks(SampleResult parent, Assertion assertion, int level) {
        if (level < 0) {
            return;
        }
        SampleResult[] children = parent.getSubResults();
        boolean childError = false;
        for (SampleResult childSampleResult : children) {
            processAssertion(childSampleResult, assertion);
            recurseAssertionChecks(childSampleResult, assertion, level - 1);
            if (!childSampleResult.isSuccessful()) {
                childError = true;
            }
        }
        // If parent is OK, but child failed, add a message and flag the parent as failed
        if (childError && parent.isSuccessful()) {
            AssertionResult assertionResult = new AssertionResult(((AbstractTestElement) assertion).getName());
            assertionResult.setResultForFailure("One or more sub-samples failed");
            parent.addAssertionResult(assertionResult);
            parent.setSuccessful(false);
        }
    }

    private void processAssertion(SampleResult result, Assertion assertion) {
        AssertionResult assertionResult;
        try {
            assertionResult = assertion.getResult(result);
        } catch (AssertionError e) {
            log.debug("Error processing Assertion.", e);
            assertionResult = new AssertionResult("Assertion failed! See log file (debug level, only).");
            assertionResult.setFailure(true);
            assertionResult.setFailureMessage(e.toString());
        } catch (JMeterError e) {
            log.error("Error processing Assertion.", e);
            assertionResult = new AssertionResult("Assertion failed! See log file.");
            assertionResult.setError(true);
            assertionResult.setFailureMessage(e.toString());
        } catch (Exception e) {
            log.error("Exception processing Assertion.", e);
            assertionResult = new AssertionResult("Assertion failed! See log file.");
            assertionResult.setError(true);
            assertionResult.setFailureMessage(e.toString());
        }
        result.setSuccessful(result.isSuccessful() && !(assertionResult.isError() || assertionResult.isFailure()));
        result.addAssertionResult(assertionResult);
    }

    private void runPostProcessors(List<PostProcessor> extractors) {
        for (PostProcessor ex : extractors) {
            TestBeanHelper.prepare((TestElement) ex);
            ex.process();
        }
    }

    private void runPreProcessors(List<PreProcessor> preProcessors) {
        for (PreProcessor ex : preProcessors) {
            if (log.isDebugEnabled()) {
                log.debug("Running preprocessor: {}", ((AbstractTestElement) ex).getName());
            }
            TestBeanHelper.prepare((TestElement) ex);
            ex.process();
        }
    }

    /**
     * Run all configured timers and sleep the total amount of time.
     * <p>
     * If the amount of time would amount to an ending after endTime, then
     * end the current thread by setting {@code running} to {@code false} and
     * return immediately.
     *
     * @param timers to be used for calculating the delay
     */
    private void delay(List<Timer> timers) {
        long totalDelay = 0;
        for (Timer timer : timers) {
            TestBeanHelper.prepare((TestElement) timer);
            long delay = timer.delay();
            if (APPLY_TIMER_FACTOR && timer.isModifiable()) {
                if (log.isDebugEnabled()) {
                    log.debug("Applying TIMER_FACTOR:{} on timer:{} for thread:{}", TIMER_FACTOR,
                            ((TestElement) timer).getName(), getThreadName());
                }
                delay = Math.round(delay * TIMER_FACTOR);
            }
            totalDelay += delay;
        }
        if (totalDelay > 0) {
            try {
                if (scheduler) {
                    // We reduce pause to ensure end of test is not delayed by a sleep ending after test scheduled end
                    // See Bug 60049
                    totalDelay = TIMER_SERVICE.adjustDelay(totalDelay, endTime, false);
                    if (totalDelay < 0) {
                        log.debug("The delay would be longer than the scheduled period, so stop thread now.");
                        running = false;
                        return;
                    }
                }
                TimeUnit.MILLISECONDS.sleep(totalDelay);
            } catch (InterruptedException e) {
                log.warn("The delay timer was interrupted - probably did not wait as long as intended.");
                Thread.currentThread().interrupt();
            }
        }
    }

    void notifyTestListeners() {
        threadVars.incIteration();
        for (TestIterationListener listener : testIterationStartListeners) {
            listener.testIterationStart(new LoopIterationEvent(threadGroupLoopController, threadVars.getIteration()));
            if (listener instanceof TestElement) {
                ((TestElement) listener).recoverRunningVersion();
            }
        }
    }

    private void notifyListeners(List<SampleListener> listeners, SampleResult result) {
        SampleEvent event = new SampleEvent(result, threadGroup.getName(), threadVars);
        notifier.notifyListeners(event, listeners);
    }

    /**
     * Set rampup delay for JMeterThread Thread
     *
     * @param delay Rampup delay for JMeterThread
     */
    public void setInitialDelay(int delay) {
        initialDelay = delay;
    }

    /**
     * Initial delay if ramp-up period is active for this threadGroup.
     */
    private void rampUpDelay() {
        delayBy(initialDelay, "RampUp");
    }

    /**
     * Wait for delay with RAMPUP_GRANULARITY
     *
     * @param delay delay in ms
     * @param type  Delay type
     */
    protected final void delayBy(long delay, String type) {
        if (delay > 0) {
            long start = System.currentTimeMillis();
            long end = start + delay;
            long now;
            long pause = RAMPUP_GRANULARITY;
            while (running && (now = System.currentTimeMillis()) < end) {
                long togo = end - now;
                if (togo < pause) {
                    pause = togo;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(pause); // delay between checks
                } catch (InterruptedException e) {
                    if (running) { // NOSONAR running may have been changed from another thread
                        log.warn("{} delay for {} was interrupted. Waited {} milli-seconds out of {}", type, threadName,
                                now - start, delay);
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Returns the threadNum.
     *
     * @return the threadNum
     */
    public int getThreadNum() {
        return threadNum;
    }

    /**
     * Sets the threadNum.
     *
     * @param threadNum the threadNum to set
     */
    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    private class IterationListener implements LoopIterationListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void iterationStart(LoopIterationEvent iterEvent) {
            notifyTestListeners();
        }
    }

    /**
     * Save the engine instance for access to the stop methods
     *
     * @param engine the engine which is used
     */
    public void setEngine(StandardJMeterEngine engine) {
        this.engine = engine;
    }

    /**
     * Should Test stop on sampler error?
     *
     * @param b true or false
     */
    public void setOnErrorStopTest(boolean b) {
        onErrorStopTest = b;
    }

    /**
     * Should Test stop abruptly on sampler error?
     *
     * @param b true or false
     */
    public void setOnErrorStopTestNow(boolean b) {
        onErrorStopTestNow = b;
    }

    /**
     * Should Thread stop on Sampler error?
     *
     * @param b true or false
     */
    public void setOnErrorStopThread(boolean b) {
        onErrorStopThread = b;
    }

    /**
     * Should Thread start next loop on Sampler error?
     *
     * @param b true or false
     */
    public void setOnErrorStartNextLoop(boolean b) {
        onErrorStartNextLoop = b;
    }

    public void setThreadGroup(AbstractThreadGroup group) {
        this.threadGroup = group;
    }

    /**
     * @return {@link ListedHashTree}
     */
    public ListedHashTree getTestTree() {
        return (ListedHashTree) testTree;
    }

    /**
     * @return {@link ListenerNotifier}
     */
    public ListenerNotifier getNotifier() {
        return notifier;
    }

}
