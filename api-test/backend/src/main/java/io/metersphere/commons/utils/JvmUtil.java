package io.metersphere.commons.utils;

import io.metersphere.utils.LoggerUtil;

public class JvmUtil {

    public static void memoryInfo() {
        int byteToMb = 1024 * 1024;
        Runtime rt = Runtime.getRuntime();
        long vmTotal = rt.totalMemory() / byteToMb;
        long vmFree = rt.freeMemory() / byteToMb;
        long vmMax = rt.maxMemory() / byteToMb;
        long vmUse = vmTotal - vmFree;

        String builder = "当前执行节点内存信息：" + "\n" +
                "当前JVM最大内存：" + vmMax + " M" + "\n" +
                "当前JVM占用的总内存：" + vmTotal + " M" + "\n" +
                "当前JVM空闲内存为：" + vmFree + " M" + "\n" +
                "当前JVM已用内存为：" + vmUse + " M" + "\n";
        LoggerUtil.info(builder);
    }
}
