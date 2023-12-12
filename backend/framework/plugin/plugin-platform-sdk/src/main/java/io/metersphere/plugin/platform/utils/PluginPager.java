package io.metersphere.plugin.platform.utils;

import lombok.Data;

@Data
public class PluginPager<T> {

    private T list;
    private long total;
    private long pageSize;
    private long current;

    public PluginPager() {
    }

    public PluginPager(T list, long total, long pageSize, long current) {
        this.list = list;
        this.total = total;
        this.pageSize = pageSize;
        this.current = current;
    }
}
