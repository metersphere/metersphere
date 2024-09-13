package io.metersphere.api.parser.jmeter.xstream;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @Author: jianxing
 * @CreateTime: 2024-09-13  14:18
 * 参考 {@link com.thoughtworks.xstream.core.util.PrioritizedList}
 * 主要修改 PrioritizedItemIterator remove 方式，支持移除转换器
 */
public class MsPrioritizedList {
    private final Set set = new TreeSet();
    private int lowestPriority = Integer.MAX_VALUE;
    private int lastId = 0;

    public void add(Object item, int priority) {
        if (this.lowestPriority > priority) {
            this.lowestPriority = priority;
        }

        this.set.add(new MsPrioritizedList.PrioritizedItem(item, priority, ++this.lastId));
    }

    public Iterator iterator() {
        return new MsPrioritizedList.PrioritizedItemIterator(this.set.iterator());
    }

    private static class PrioritizedItemIterator implements Iterator {
        private Iterator iterator;

        public PrioritizedItemIterator(Iterator iterator) {
            this.iterator = iterator;
        }

        public void remove() {
            this.iterator.remove();
        }

        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        public Object next() {
            return ((MsPrioritizedList.PrioritizedItem)this.iterator.next()).value;
        }
    }

    private static class PrioritizedItem implements Comparable {
        final Object value;
        final int priority;
        final int id;

        public PrioritizedItem(Object value, int priority, int id) {
            this.value = value;
            this.priority = priority;
            this.id = id;
        }

        public int compareTo(Object o) {
            MsPrioritizedList.PrioritizedItem other = (MsPrioritizedList.PrioritizedItem)o;
            return this.priority != other.priority ? other.priority - this.priority : other.id - this.id;
        }

        public boolean equals(Object obj) {
            return this.id == ((MsPrioritizedList.PrioritizedItem)obj).id;
        }
    }
}
