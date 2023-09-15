
package io.metersphere.system.uid.buffer;

import java.util.List;

/**
 * Buffered UID provider(Lambda supported), which provides UID in the same one second
 */
@FunctionalInterface
public interface BufferedUidProvider {

    /**
     * Provides UID in one second
     */
    List<Long> provide(long momentInSecond);
}
