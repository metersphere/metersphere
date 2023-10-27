package io.metersphere.system.utils;

import lombok.Data;

@Data
public class Pager<T> {
    private T list;
    private long total;
    private long pageSize;
    private long current;

    public Pager() {
    }

    public Pager(T list, long total, long pageSize, long current) {
        this.list = list;
        this.total = total;
        this.pageSize = pageSize;
        this.current = current;
    }
}
