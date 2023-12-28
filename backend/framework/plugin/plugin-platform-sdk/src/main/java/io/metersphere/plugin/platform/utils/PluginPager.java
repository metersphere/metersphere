package io.metersphere.plugin.platform.utils;

import lombok.Data;

@Data
public class PluginPager<T> {

    private T data;
    private long total;
    private long pageSize;
    private long current;

    public PluginPager() {
    }

    public PluginPager(long pageSize, long current) {
        this.data = null;
        this.total = 0;
        this.pageSize = pageSize;
        this.current = current;
    }

    public PluginPager(T list, long total, long pageSize, long current) {
        this.data = list;
        this.total = total;
        this.pageSize = pageSize;
        this.current = current;
    }
}
