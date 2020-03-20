package io.metersphere;

import io.metersphere.commons.constants.JmeterReportType;
import kg.apc.jmeter.PluginsCMDWorker;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Test;

public class GenerateGraphTest {

    /*
    AggregateReport = JMeter's native Aggregate Report, can be saved only as CSV
    SynthesisReport = mix between JMeter's native Summary Report and Aggregate Report, can be saved only as CSV
    ThreadsStateOverTime = Active Threads Over Time
    BytesThroughputOverTime
    HitsPerSecond
    LatenciesOverTime
    PerfMon = PerfMon Metrics Collector
    DbMon = DbMon Metrics Collector, DataBase, get performance counters via sql
    JMXMon = JMXMon Metrics Collector, Java Management Extensions counters
    ResponseCodesPerSecond
    ResponseTimesDistribution
    ResponseTimesOverTime
    ResponseTimesPercentiles
    ThroughputVsThreads
    TimesVsThreads = Response Times VS Threads
    TransactionsPerSecond
    PageDataExtractorOverTime
    MergeResults = MergeResults Command Line Merge Tool to simplify the comparison of two or more load tests, need properties file (like merge-results.properties)
     */
    @Test
    public void test1() {
        JMeterUtils.setJMeterHome("/opt/fit2cloud/apache-jmeter-5.2.1");
        JMeterUtils.loadJMeterProperties("/opt/fit2cloud/apache-jmeter-5.2.1/bin/jmeter.properties");
        PluginsCMDWorker worker = new PluginsCMDWorker();
        worker.setPluginType(JmeterReportType.AggregateReport.name());
        worker.addExportMode(2);
        worker.setOutputCSVFile("/tmp/test0320.csv");
        worker.setInputFile("/Users/liuruibin/Desktop/0316.jtl");
        worker.doJob();
    }
}
