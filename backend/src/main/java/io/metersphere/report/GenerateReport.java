package io.metersphere.report;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.metersphere.report.base.*;
import io.metersphere.report.parse.ResultDataParse;
import org.apache.jmeter.report.processor.ErrorsSummaryConsumer;
import org.apache.jmeter.report.processor.StatisticsSummaryConsumer;
import org.apache.jmeter.report.processor.Top5ErrorsBySamplerConsumer;
import org.apache.jmeter.report.processor.graph.impl.ActiveThreadsGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.HitsPerSecondGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.ResponseTimeOverTimeGraphConsumer;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GenerateReport {

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
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        Map<String, Object> activeDataMap = ResultDataParse.getGraphDataMap(jtlString, new ActiveThreadsGraphConsumer());
        List<ChartsData> usersList = ResultDataParse.graphMapParsing(activeDataMap, "users");
        Optional<ChartsData> max = usersList.stream().max(Comparator.comparing(ChartsData::getyAxis));
        int maxUser = max.get().getyAxis().setScale(0, BigDecimal.ROUND_UP).intValue();

        Map<String, Object> hitsDataMap = ResultDataParse.getGraphDataMap(jtlString, new HitsPerSecondGraphConsumer());
        List<ChartsData> hitsList = ResultDataParse.graphMapParsing(hitsDataMap, "hits");
        double hits = hitsList.stream().map(ChartsData::getyAxis)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0);

        Map<String, Object> errorDataMap = ResultDataParse.getSummryDataMap(jtlString, new StatisticsSummaryConsumer());
        List<Statistics> statisticsList = ResultDataParse.summaryMapParsing(errorDataMap, Statistics.class);
        Optional<Double> error = statisticsList.stream().map(item -> Double.parseDouble(item.getError())).reduce(Double::sum);

        Map<String, Object> responseDataMap = ResultDataParse.getGraphDataMap(jtlString, new ResponseTimeOverTimeGraphConsumer());
        List<ChartsData> responseDataList = ResultDataParse.graphMapParsing(responseDataMap, "response");
        double responseTime = responseDataList.stream().map(ChartsData::getyAxis)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0);

        TestOverview testOverview = new TestOverview();
        testOverview.setMaxUsers(String.valueOf(maxUser));
        testOverview.setAvgThroughput(decimalFormat.format(hits));
        testOverview.setErrors(decimalFormat.format(error.get()));
        testOverview.setAvgResponseTime(decimalFormat.format(responseTime / 1000));

        // todo
        testOverview.setResponseTime90("0");
        testOverview.setAvgBandwidth("0");
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
        List<Metric> totalLineList = GenerateReport.resolver(jtlString);

        totalLineList.sort(Comparator.comparing(t0 -> Long.valueOf(t0.getTimestamp())));

        String startTimeStamp = totalLineList.get(0).getTimestamp();
        String endTimeStamp = totalLineList.get(totalLineList.size() - 1).getTimestamp();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String startTime = dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(startTimeStamp)), ZoneId.systemDefault()));
        String endTime = dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(endTimeStamp)), ZoneId.systemDefault()));

        // todo 时间问题
        long seconds = Duration.between(Instant.ofEpochMilli(Long.parseLong(startTimeStamp)), Instant.ofEpochMilli(Long.parseLong(endTimeStamp))).getSeconds();
        ReportTimeInfo reportTimeInfo = new ReportTimeInfo();
        reportTimeInfo.setStartTime(startTime);
        reportTimeInfo.setEndTime(endTime);
        reportTimeInfo.setDuration(String.valueOf(seconds));

        return reportTimeInfo;
    }

}