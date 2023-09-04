
package io.metersphere.sdk.uid.impl;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.uid.BitsAllocator;
import io.metersphere.sdk.uid.buffer.BufferPaddingExecutor;
import io.metersphere.sdk.uid.buffer.RejectedPutBufferHandler;
import io.metersphere.sdk.uid.buffer.RejectedTakeBufferHandler;
import io.metersphere.sdk.uid.buffer.RingBuffer;
import io.metersphere.sdk.util.LogUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * from {@link DefaultUidGenerator}, based on a lock free {@link RingBuffer}<p>
 */
@Service
public class CachedUidGenerator extends DefaultUidGenerator implements DisposableBean, InitializingBean {
    private static final int DEFAULT_BOOST_POWER = 3;

    /**
     * Spring properties
     */
    private int boostPower = DEFAULT_BOOST_POWER;
    private Long scheduleInterval;

    private RejectedPutBufferHandler rejectedPutBufferHandler;
    private RejectedTakeBufferHandler rejectedTakeBufferHandler;

    /**
     * RingBuffer
     */
    private RingBuffer ringBuffer;
    private BufferPaddingExecutor bufferPaddingExecutor;

    @Override
    public void afterPropertiesSet() {
        // initialize workerId & bitsAllocator
        super.afterPropertiesSet();

        // initialize RingBuffer & RingBufferPaddingExecutor
        this.initRingBuffer();
        LogUtils.info("Initialized RingBuffer successfully.");
    }

    @Override
    public long getUID() {
        try {
            return ringBuffer.take();
        } catch (Exception e) {
            LogUtils.error("Generate unique id exception. ", e);
            throw new MSException(e);
        }
    }

    @Override
    public String parseUID(long uid) {
        return super.parseUID(uid);
    }

    @Override
    public void destroy() throws Exception {
        bufferPaddingExecutor.shutdown();
    }

    /**
     * Get the UIDs in the same specified second under the max sequence
     * * @return UID list, size of {@link BitsAllocator#getMaxSequence()} + 1
     */
    protected List<Long> nextIdsForOneSecond(long currentSecond) {
        // Initialize result list size of (max sequence + 1)
        int listSize = (int) bitsAllocator.getMaxSequence() + 1;
        List<Long> uidList = new ArrayList<>(listSize);

        // Allocate the first sequence of the second, the others can be calculated with the offset
        long firstSeqUid = bitsAllocator.allocate(currentSecond - epochSeconds, workerId, 0L);
        for (int offset = 0; offset < listSize; offset++) {
            uidList.add(firstSeqUid + offset);
        }

        return uidList;
    }

    /**
     * Initialize RingBuffer & RingBufferPaddingExecutor
     */
    private void initRingBuffer() {
        // initialize RingBuffer
        int bufferSize = ((int) bitsAllocator.getMaxSequence() + 1) << boostPower;
        int paddingFactor = RingBuffer.DEFAULT_PADDING_PERCENT;
        this.ringBuffer = new RingBuffer(bufferSize, paddingFactor);
        LogUtils.info("Initialized ring buffer size:{}, paddingFactor:{}", bufferSize, paddingFactor);

        // initialize RingBufferPaddingExecutor
        boolean usingSchedule = (scheduleInterval != null);
        this.bufferPaddingExecutor = new BufferPaddingExecutor(ringBuffer, this::nextIdsForOneSecond, usingSchedule);
        if (usingSchedule) {
            bufferPaddingExecutor.setScheduleInterval(scheduleInterval);
        }

        LogUtils.info("Initialized BufferPaddingExecutor. Using schdule:{}, interval:{}", usingSchedule, scheduleInterval);

        // set rejected put/take handle policy
        this.ringBuffer.setBufferPaddingExecutor(bufferPaddingExecutor);
        if (rejectedPutBufferHandler != null) {
            this.ringBuffer.setRejectedPutHandler(rejectedPutBufferHandler);
        }
        if (rejectedTakeBufferHandler != null) {
            this.ringBuffer.setRejectedTakeHandler(rejectedTakeBufferHandler);
        }

        // fill in all slots of the RingBuffer
        bufferPaddingExecutor.paddingBuffer();

        // start buffer padding threads
        bufferPaddingExecutor.start();
    }

    /**
     * Setters for spring property
     */
    public void setBoostPower(int boostPower) {
        Assert.isTrue(boostPower > 0, "Boost power must be positive!");
        this.boostPower = boostPower;
    }

    public void setRejectedPutBufferHandler(RejectedPutBufferHandler rejectedPutBufferHandler) {
        Assert.notNull(rejectedPutBufferHandler, "RejectedPutBufferHandler can't be null!");
        this.rejectedPutBufferHandler = rejectedPutBufferHandler;
    }

    public void setRejectedTakeBufferHandler(RejectedTakeBufferHandler rejectedTakeBufferHandler) {
        Assert.notNull(rejectedTakeBufferHandler, "RejectedTakeBufferHandler can't be null!");
        this.rejectedTakeBufferHandler = rejectedTakeBufferHandler;
    }

    public void setScheduleInterval(long scheduleInterval) {
        Assert.isTrue(scheduleInterval > 0, "Schedule interval must positive!");
        this.scheduleInterval = scheduleInterval;
    }

}
