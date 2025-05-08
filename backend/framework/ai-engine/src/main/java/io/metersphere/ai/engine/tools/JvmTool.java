package io.metersphere.ai.engine.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Component
public class JvmTool {

    @Tool(description = "查询 JVM 信息")
    public String getJVMInfo() {
        var runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        var memoryMXBean = ManagementFactory.getMemoryMXBean();
        var osBean = ManagementFactory.getOperatingSystemMXBean();
        var heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        var runtime = Runtime.getRuntime();

        // JVM 参数格式化
        String jvmArgs = String.join(" ", runtimeMXBean.getInputArguments());

        // 构建返回字符串
        return """
                ==== JVM 运行信息 ====
                JVM 名称: %s
                JVM 版本: %s
                JVM 启动时间: %d ms
                JVM 运行时间: %d ms
                JVM 参数: %s
                
                ==== 内存信息 ====
                可用处理器数: %d
                堆内存 - 初始化: %d MB
                堆内存 - 最大: %d MB
                堆内存 - 已使用: %d MB
                JVM 最大可用内存: %d MB
                JVM 当前分配内存: %d MB
                JVM 空闲内存: %d MB
                
                ==== 操作系统信息 ====
                操作系统: %s
                处理器架构: %s
                """.formatted(
                runtimeMXBean.getVmName(),
                runtimeMXBean.getVmVersion(),
                runtimeMXBean.getStartTime(),
                runtimeMXBean.getUptime(),
                jvmArgs.isEmpty() ? "无" : jvmArgs,  // 处理无参数情况
                osBean.getAvailableProcessors(),
                heapMemoryUsage.getInit() / 1024 / 1024,
                heapMemoryUsage.getMax() / 1024 / 1024,
                heapMemoryUsage.getUsed() / 1024 / 1024,
                runtime.maxMemory() / 1024 / 1024,
                runtime.totalMemory() / 1024 / 1024,
                runtime.freeMemory() / 1024 / 1024,
                osBean.getName(),
                osBean.getArch()
        );
    }
}
