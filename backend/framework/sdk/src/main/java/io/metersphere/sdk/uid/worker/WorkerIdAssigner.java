
package io.metersphere.sdk.uid.worker;

/**
 * Represents a worker id assigner for {@link io.metersphere.sdk.uid.impl.DefaultUidGenerator}
 * 

 */
public interface WorkerIdAssigner {

    /**
     * Assign worker id for {@link io.metersphere.sdk.uid.impl.DefaultUidGenerator}
     * 
     * @return assigned worker id
     */
    long assignWorkerId();

}
