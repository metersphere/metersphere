package io.metersphere.project.utils;

import io.metersphere.project.dto.ModuleSortCountResultDTO;

public class NodeSortUtils {

    //默认节点间隔
    public static final long DEFAULT_NODE_INTERVAL_POS = 4096;

    /**
     * 计算模块排序
     *
     * @param previousNodePos 前一个节点的pos     如果没有节点则为-1
     * @param nextNodePos     后一个节点的pos     如果没有节点则为-1
     * @return 计算后的pos值以及是否需要刷新整棵树的pos(如果两个节点之间的pos值小于2则需要刷新整棵树的pos)
     */
    public static ModuleSortCountResultDTO countModuleSort(long previousNodePos, long nextNodePos) {
        boolean refreshPos = false;
        long pos;
        if (nextNodePos < 0 && previousNodePos < 0) {
            pos = 0;
        } else if (nextNodePos < 0) {
            pos = previousNodePos + DEFAULT_NODE_INTERVAL_POS;
        } else if (previousNodePos < 0) {
            pos = nextNodePos / 2;
            if (pos < 2) {
                refreshPos = true;
            }
        } else {
            long quantityDifference = (nextNodePos - previousNodePos) / 2;
            if (quantityDifference <= 2) {
                refreshPos = true;
            }
            pos = previousNodePos + quantityDifference;
        }
        return new ModuleSortCountResultDTO(refreshPos, pos);
    }
}
