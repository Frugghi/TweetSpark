package com.etsy.statsd.profiler.profilers;

import com.etsy.statsd.profiler.Arguments;
import com.etsy.statsd.profiler.Profiler;
import com.etsy.statsd.profiler.reporter.Reporter;

import java.lang.management.RuntimeMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;

/**
 * Profiles CPU load
 *
 * @author Tommaso Madonia
 */
public class CPULoadProfiler extends Profiler {
    public static final long PERIOD = 1;

    private OperatingSystemMXBean operatingSystemMXBean;
    private RuntimeMXBean runtimeMXBean;
    private long oldUptime;
    private long oldCpuTime;

    public CPULoadProfiler(Reporter reporter, Arguments arguments) {
        super(reporter, arguments);
        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        oldUptime = -1;
        oldCpuTime = -1;
    }

    /**
     * Profile CPU load
     */
    @Override
    public void profile() {
        recordStats();
    }

    @Override
    public void flushData() {
        recordStats();
    }

    @Override
    public long getPeriod() {
        return PERIOD;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    protected void handleArguments(Arguments arguments) { /* No arguments needed */ }

    /**
     * Records all CPU load statistics
     */
    private void recordStats() {
        recordGaugeValue("system.cpu.load", (long) (operatingSystemMXBean.getSystemLoadAverage() * 100));
        if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOperatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) this.operatingSystemMXBean;
            recordGaugeValue("jvm.cpu.load", (long) (sunOperatingSystemMXBean.getProcessCpuLoad() * 100));
            int availableProcessors = sunOperatingSystemMXBean.getAvailableProcessors();
            long uptime = runtimeMXBean.getUptime();
            long cpuTime = sunOperatingSystemMXBean.getProcessCpuTime();
            if (oldUptime > 0 && oldCpuTime > 0) {
                double cpuUsage = Math.min(99F, (cpuTime - oldCpuTime) / ((uptime - oldUptime) * 10000F * availableProcessors));
                recordGaugeValue("jvm.cpu.usage", (long) (cpuUsage * 100));
            }
            oldUptime = uptime;
            oldCpuTime = cpuTime;
        }
    }


}
