
package io.metersphere.system.uid.buffer;

/**
 * If cursor catches the tail it means that the ring buffer is empty, any more buffer take request will be rejected.
 * Specify the policy to handle the reject. This is a Lambda supported interface
 */
@FunctionalInterface
public interface RejectedTakeBufferHandler {

    /**
     * Reject take buffer request
     */
    void rejectTakeBuffer(RingBuffer ringBuffer);
}
