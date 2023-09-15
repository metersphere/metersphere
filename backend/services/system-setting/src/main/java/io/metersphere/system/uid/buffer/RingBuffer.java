
package io.metersphere.system.uid.buffer;

import io.metersphere.system.uid.utils.PaddedAtomicLong;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a ring buffer based on array.<br>
 * Using array could improve read element performance due to the CUP cache line. To prevent
 * the side effect of False Sharing, {@link PaddedAtomicLong} is using on 'tail' and 'cursor'<p>
 * <p>
 * A ring buffer is consisted of:
 * <li><b>slots:</b> each element of the array is a slot, which is be set with a UID
 * <li><b>flags:</b> flag array corresponding the same index with the slots, indicates whether can take or put slot
 * <li><b>tail:</b> a sequence of the max slot position to produce
 * <li><b>cursor:</b> a sequence of the min slot position to consume
 */
public class RingBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RingBuffer.class);

    /**
     * Constants
     */
    private static final int START_POINT = -1;
    private static final long CAN_PUT_FLAG = 0L;
    private static final long CAN_TAKE_FLAG = 1L;
    public static final int DEFAULT_PADDING_PERCENT = 50;

    /**
     * The size of RingBuffer's slots, each slot hold a UID
     */
    @Getter
    private final int bufferSize;
    private final long indexMask;
    private final long[] slots;
    private final PaddedAtomicLong[] flags;

    /**
     * Tail: last position sequence to produce
     */
    private final AtomicLong tail = new PaddedAtomicLong(START_POINT);

    /**
     * Cursor: current position sequence to consume
     */
    private final AtomicLong cursor = new PaddedAtomicLong(START_POINT);

    /**
     * Threshold for trigger padding buffer
     */
    private final int paddingThreshold;

    /**
     * Reject put/take buffer handle policy
     */
    private RejectedPutBufferHandler rejectedPutHandler = this::discardPutBuffer;
    private RejectedTakeBufferHandler rejectedTakeHandler = this::exceptionRejectedTakeBuffer;

    /**
     * Executor of padding buffer
     */
    private BufferPaddingExecutor bufferPaddingExecutor;

    /**
     * Constructor with buffer size, paddingFactor default as {@value #DEFAULT_PADDING_PERCENT}
     *
     * @param bufferSize must be positive & a power of 2
     */
    public RingBuffer(int bufferSize) {
        this(bufferSize, DEFAULT_PADDING_PERCENT);
    }

    /**
     * Constructor with buffer size & padding factor
     *
     * @param bufferSize    must be positive & a power of 2
     * @param paddingFactor percent in (0 - 100). When the count of rest available UIDs reach the threshold, it will trigger padding buffer<br>
     *                      Sample: paddingFactor=20, bufferSize=1000 -> threshold=1000 * 20 /100,
     *                      padding buffer will be triggered when tail-cursor<threshold
     */
    public RingBuffer(int bufferSize, int paddingFactor) {
        // check buffer size is positive & a power of 2; padding factor in (0, 100)
        Assert.isTrue(bufferSize > 0L, "RingBuffer size must be positive");
        Assert.isTrue(Integer.bitCount(bufferSize) == 1, "RingBuffer size must be a power of 2");
        Assert.isTrue(paddingFactor > 0 && paddingFactor < 100, "RingBuffer size must be positive");

        this.bufferSize = bufferSize;
        this.indexMask = bufferSize - 1;
        this.slots = new long[bufferSize];
        this.flags = initFlags(bufferSize);

        this.paddingThreshold = bufferSize * paddingFactor / 100;
    }

    /**
     * Put an UID in the ring & tail moved<br>
     * We use 'synchronized' to guarantee the UID fill in slot & publish new tail sequence as atomic operations<br>
     *
     * <b>Note that: </b> It is recommended to put UID in a serialize way, cause we once batch generate a series UIDs and put
     * the one by one into the buffer, so it is unnecessary put in multi-threads
     * * @return false means that the buffer is full, apply {@link RejectedPutBufferHandler}
     */
    public synchronized boolean put(long uid) {
        long currentTail = tail.get();
        long currentCursor = cursor.get();

        // tail catches the cursor, means that you can't put any cause of RingBuffer is full
        long distance = currentTail - (currentCursor == START_POINT ? 0 : currentCursor);
        if (distance == bufferSize - 1) {
            rejectedPutHandler.rejectPutBuffer(this, uid);
            return false;
        }

        // 1. pre-check whether the flag is CAN_PUT_FLAG
        int nextTailIndex = calSlotIndex(currentTail + 1);
        if (flags[nextTailIndex].get() != CAN_PUT_FLAG) {
            rejectedPutHandler.rejectPutBuffer(this, uid);
            return false;
        }

        // 2. put UID in the next slot
        // 3. update next slot' flag to CAN_TAKE_FLAG
        // 4. publish tail with sequence increase by one
        slots[nextTailIndex] = uid;
        flags[nextTailIndex].set(CAN_TAKE_FLAG);
        tail.incrementAndGet();

        // The atomicity of operations above, guarantees by 'synchronized'. In another word,
        // the take operation can't consume the UID we just put, until the tail is published(tail.incrementAndGet())
        return true;
    }

    /**
     * Take an UID of the ring at the next cursor, this is a lock free operation by using atomic cursor<p>
     * <p>
     * Before getting the UID, we also check whether reach the padding threshold,
     * the padding buffer operation will be triggered in another thread<br>
     * If there is no more available UID to be taken, the specified {@link RejectedTakeBufferHandler} will be applied<br>
     *
     * @return UID
     * @throws IllegalStateException if the cursor moved back
     */
    public long take() {
        // spin get next available cursor
        long currentCursor = cursor.get();
        long nextCursor = cursor.updateAndGet(old -> old == tail.get() ? old : old + 1);

        // check for safety consideration, it never occurs
        Assert.isTrue(nextCursor >= currentCursor, "Curosr can't move back");

        // trigger padding in an async-mode if reach the threshold
        long currentTail = tail.get();
        if (currentTail - nextCursor < paddingThreshold) {
            LOGGER.info("Reach the padding threshold:{}. tail:{}, cursor:{}, rest:{}", paddingThreshold, currentTail,
                    nextCursor, currentTail - nextCursor);
            bufferPaddingExecutor.asyncPadding();
        }

        // cursor catch the tail, means that there is no more available UID to take
        if (nextCursor == currentCursor) {
            rejectedTakeHandler.rejectTakeBuffer(this);
        }

        // 1. check next slot flag is CAN_TAKE_FLAG
        int nextCursorIndex = calSlotIndex(nextCursor);
        Assert.isTrue(flags[nextCursorIndex].get() == CAN_TAKE_FLAG, "Curosr not in can take status");

        // 2. get UID from next slot
        // 3. set next slot flag as CAN_PUT_FLAG.
        long uid = slots[nextCursorIndex];
        flags[nextCursorIndex].set(CAN_PUT_FLAG);

        // Note that: Step 2,3 can not swap. If we set flag before get value of slot, the producer may overwrite the
        // slot with a new UID, and this may cause the consumer take the UID twice after walk a round the ring
        return uid;
    }

    /**
     * Calculate slot index with the slot sequence (sequence % bufferSize)
     */
    protected int calSlotIndex(long sequence) {
        return (int) (sequence & indexMask);
    }

    /**
     * Discard policy for {@link RejectedPutBufferHandler}, we just do logging
     */
    protected void discardPutBuffer(RingBuffer ringBuffer, long uid) {
        LOGGER.warn("Rejected putting buffer for uid:{}. {}", uid, ringBuffer);
    }

    /**
     * Policy for {@link RejectedTakeBufferHandler}, throws {@link RuntimeException} after logging
     */
    protected void exceptionRejectedTakeBuffer(RingBuffer ringBuffer) {
        LOGGER.warn("Rejected take buffer. {}", ringBuffer);
        throw new RuntimeException("Rejected take buffer. " + ringBuffer);
    }

    /**
     * Initialize flags as CAN_PUT_FLAG
     */
    private PaddedAtomicLong[] initFlags(int bufferSize) {
        PaddedAtomicLong[] flags = new PaddedAtomicLong[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            flags[i] = new PaddedAtomicLong(CAN_PUT_FLAG);
        }

        return flags;
    }

    /**
     * Getters
     */
    public long getTail() {
        return tail.get();
    }

    public long getCursor() {
        return cursor.get();
    }

    /**
     * Setters
     */
    public void setBufferPaddingExecutor(BufferPaddingExecutor bufferPaddingExecutor) {
        this.bufferPaddingExecutor = bufferPaddingExecutor;
    }

    public void setRejectedPutHandler(RejectedPutBufferHandler rejectedPutHandler) {
        this.rejectedPutHandler = rejectedPutHandler;
    }

    public void setRejectedTakeHandler(RejectedTakeBufferHandler rejectedTakeHandler) {
        this.rejectedTakeHandler = rejectedTakeHandler;
    }

    @Override
    public String toString() {
        return "RingBuffer [bufferSize=" + bufferSize +
                ", tail=" + tail +
                ", cursor=" + cursor +
                ", paddingThreshold=" + paddingThreshold + "]";
    }

}
