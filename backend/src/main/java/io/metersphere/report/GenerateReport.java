package io.metersphere.report;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.metersphere.report.base.*;
import io.metersphere.report.parse.ResultDataParse;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.processor.ErrorsSummaryConsumer;
import org.apache.jmeter.report.processor.StatisticsSummaryConsumer;
import org.apache.jmeter.report.processor.Top5ErrorsBySamplerConsumer;
import org.apache.jmeter.report.processor.graph.impl.ActiveThreadsGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.HitsPerSecondGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.ResponseTimeOverTimeGraphConsumer;
import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateReport {

    private static final String DATE_TIME_PATTERN = "yyyy/MM/dd HH:mm:ss";

    private static List<Metric> resolver(String jtlString) {
        HeaderColumnNameMappingStrategy<Metric> ms = new HeaderColumnNameMappingStrategy<>();
        ms.setType(Metric.class);
        try (Reader reader = new StringReader(jtlString)) {
            CsvToBean<Metric> cb = new CsvToBeanBuilder<Metric>(reader)
                    .withType(Metric.class)
                    .withSkipLines(0)
                    .withMappingStrategy(ms)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            return cb.parse();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static List<Errors> getErrorsList(String jtlString) {
        Map<String, Object> statisticsDataMap = ResultDataParse.getSummryDataMap(jtlString, new ErrorsSummaryConsumer());
        return ResultDataParse.summaryMapParsing(statisticsDataMap, Errors.class);
    }

    public static List<ErrorsTop5> getErrorsTop5List(String jtlString) {
        Map<String, Object> statisticsDataMap = ResultDataParse.getSummryDataMap(jtlString, new Top5ErrorsBySamplerConsumer());
        return ResultDataParse.summaryMapParsing(statisticsDataMap, ErrorsTop5.class);
    }

    public static List<Statistics> getRequestStatistics(String jtlString) {
        Map<String, Object> statisticsDataMap = ResultDataParse.getSummryDataMap(jtlString, new StatisticsSummaryConsumer());
        return ResultDataParse.summaryMapParsing(statisticsDataMap, Statistics.class);
    }

    public static TestOverview getTestOverview(String jtlString) {
        TestOverview testOverview = new TestOverview();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        List<Metric> totalLineList = GenerateReport.resolver(jtlString);
        // todo 修改测试概览的数值
        List<Metric> totalLineList2 = GenerateReport.resolver(jtlString);
        // 时间戳转时间
        for (Metric metric : totalLineList2) {
            metric.setTimestamp(stampToDate(DATE_TIME_PATTERN, metric.getTimestamp()));
        }

        Map<String, List<Metric>> collect2 = Objects.requireNonNull(totalLineList2).stream().collect(Collectors.groupingBy(Metric::getTimestamp));
        List<Map.Entry<String, List<Metric>>> entries = new ArrayList<>(collect2.entrySet());
        int maxUsers = 0;
        for (Map.Entry<String, List<Metric>> entry : entries) {
            List<Metric> metrics = entry.getValue();
            Map<String, List<Metric>> metricsMap = metrics.stream().collect(Collectors.groupingBy(Metric::getThreadName));
            if (metricsMap.size() > maxUsers) {
                maxUsers = metricsMap.size();
            }
        }

        Map<String, List<Metric>> collect = totalLineList.stream().collect(Collectors.groupingBy(Metric::getTimestamp));
        Iterator<Map.Entry<String, List<Metric>>> iterator = collect.entrySet().iterator();

        int totalElapsed = 0;
        float totalBytes = 0f;

        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> entry = iterator.next();
            List<Metric> metricList = entry.getValue();

            for (Metric metric : metricList) {
                String elapsed = metric.getElapsed();
                totalElapsed += Integer.parseInt(elapsed);
                String bytes = metric.getBytes();
                totalBytes += Float.parseFloat(bytes);
            }
        }

        totalLineList.sort(Comparator.comparing(t0 -> Long.valueOf(t0.getTimestamp())));

        testOverview.setMaxUsers(String.valueOf(maxUsers));

        List<Metric> list90 = totalLineList.subList(0, totalLineList.size() * 9 / 10);
        long sum = list90.stream().mapToLong(metric -> Long.parseLong(metric.getElapsed())).sum();
        double avg90 = (double) sum / 1000 / list90.size();
        testOverview.setResponseTime90(decimalFormat.format(avg90));

        long timesStampStart = Long.parseLong(totalLineList.get(0).getTimestamp());
        long timesStampEnd = Long.parseLong(totalLineList.get(totalLineList.size() - 1).getTimestamp());
        long time = timesStampEnd - timesStampStart + Long.parseLong(totalLineList.get(totalLineList.size() - 1).getElapsed());
        double avgThroughput = (double) totalLineList.size() / (time * 1.0 / 1000);
        testOverview.setAvgThroughput(decimalFormat.format(avgThroughput));

        List<Metric> falseList = totalLineList.stream().filter(metric -> StringUtils.equals("false", metric.getSuccess())).collect(Collectors.toList());
        double errors = falseList.size() * 1.0 / totalLineList.size() * 100;
        testOverview.setErrors(decimalFormat.format(errors));

        double avg = totalElapsed * 1.0 / totalLineList.size() / 1000;
        testOverview.setAvgResponseTime(decimalFormat.format(avg));

        double bandwidth = totalBytes * 1.0 / time;
        testOverview.setAvgBandwidth(decimalFormat.format(bandwidth));

        return testOverview;
    }

    public static List<ChartsData> getLoadChartData(String jtlString) {
        Map<String, Object> activeThreadMap = ResultDataParse.getGraphDataMap(jtlString, new ActiveThreadsGraphConsumer());
        Map<String, Object> hitsMap = ResultDataParse.getGraphDataMap(jtlString, new HitsPerSecondGraphConsumer());
        List<ChartsData> resultList = ResultDataParse.graphMapParsing(activeThreadMap, "users");
        List<ChartsData> hitsList = ResultDataParse.graphMapParsing(hitsMap, "hits");
        resultList.addAll(hitsList);
        return resultList;
    }

    public static List<ChartsData> getResponseTimeChartData(String jtlString) {
        Map<String, Object> activeThreadMap = ResultDataParse.getGraphDataMap(jtlString, new ActiveThreadsGraphConsumer());
        Map<String, Object> responseTimeMap = ResultDataParse.getGraphDataMap(jtlString, new ResponseTimeOverTimeGraphConsumer());
        List<ChartsData> resultList = ResultDataParse.graphMapParsing(activeThreadMap, "users");
        List<ChartsData> responseTimeList = ResultDataParse.graphMapParsing(responseTimeMap,  "responseTime");
        resultList.addAll(responseTimeList);
        return resultList;
    }

    public static ReportTimeInfo getReportTimeInfo(String jtlString) {
        ReportTimeInfo reportTimeInfo = new ReportTimeInfo();
        List<Metric> totalLineList = GenerateReport.resolver(jtlString);

        totalLineList.sort(Comparator.comparing(t0 -> Long.valueOf(t0.getTimestamp())));

        String startTimeStamp = totalLineList.get(0).getTimestamp();
        String endTimeStamp = totalLineList.get(totalLineList.size() - 1).getTimestamp();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String startTime = dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(startTimeStamp)), ZoneId.systemDefault()));
        String endTime = dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(endTimeStamp)), ZoneId.systemDefault()));
        reportTimeInfo.setStartTime(startTime);
        reportTimeInfo.setEndTime(endTime);

        Date startDate = new Date(Long.parseLong(startTimeStamp));
        Date endDate = new Date(Long.parseLong(endTimeStamp));
        long timestamp = endDate.getTime() - startDate.getTime();
        reportTimeInfo.setDuration(String.valueOf(timestamp * 1.0 / 1000 / 60));

        // todo 时间问题
        long seconds = Duration.between(Instant.ofEpochMilli(Long.parseLong(startTimeStamp)), Instant.ofEpochMilli(Long.parseLong(endTimeStamp))).getSeconds();
        reportTimeInfo.setDuration(String.valueOf(seconds));

        return reportTimeInfo;
    }

    private static String stampToDate(String pattern, String timeStamp) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(timeStamp)), ZoneId.systemDefault());
        return localDateTime.format(dateTimeFormatter);
    }

}