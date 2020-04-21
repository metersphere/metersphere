package io.metersphere.report;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.metersphere.report.base.*;
import io.metersphere.report.dto.ErrorsTop5DTO;
import io.metersphere.report.parse.ResultDataParse;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.processor.StatisticsSummaryConsumer;
import org.apache.jmeter.report.processor.graph.impl.ActiveThreadsGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.HitsPerSecondGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.ResponseTimeOverTimeGraphConsumer;
import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateReport {

    private static final Integer ERRORS_TOP_SIZE = 5;
    private static final String DATE_TIME_PATTERN = "yyyy/MM/dd HH:mm:ss";
    private static final String TIME_PATTERN = "HH:mm:ss";

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
        List<Metric> totalMetricList = resolver(jtlString);
        List<Errors> errorsList = new ArrayList<>();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        List<Metric> falseList = new ArrayList<>();
        for (Metric metric : totalMetricList) {
            if (StringUtils.equals("false", metric.getSuccess())) {
                falseList.add(metric);
            }
        }

        Map<String, List<Metric>> jtlMap = falseList.stream().collect(Collectors.groupingBy(GenerateReport::getResponseCodeAndFailureMessage));

        for (Map.Entry<String, List<Metric>> next : jtlMap.entrySet()) {
            String key = next.getKey();
            List<Metric> metricList = next.getValue();
            Errors errors = new Errors();
            errors.setErrorType(key);
            errors.setErrorNumber(String.valueOf(metricList.size()));
            int errorSize = metricList.size();
            int errorAllSize = falseList.size();
            int allSamples = totalMetricList.size();
            errors.setPrecentOfErrors(decimalFormat.format((double) errorSize / errorAllSize * 100) + "%");
            errors.setPrecentOfAllSamples(decimalFormat.format((double) errorSize / allSamples * 100) + "%");
            errorsList.add(errors);
        }

        return errorsList;
    }

    public static List<Statistics> getRequestStatistics(String jtlString) {
        Map<String, Object> statisticsDataMap = ResultDataParse.getSummryDataMap(jtlString, new StatisticsSummaryConsumer());
        return ResultDataParse.summaryMapParsing(statisticsDataMap);
    }

    private static String getResponseCodeAndFailureMessage(Metric metric) {
        return metric.getResponseCode() + "/" + metric.getResponseMessage();
    }

    public static ErrorsTop5DTO getErrorsTop5DTO(String jtlString) {
        List<Metric> totalMetricList = resolver(jtlString);
        ErrorsTop5DTO top5DTO = new ErrorsTop5DTO();
        List<ErrorsTop5> errorsTop5s = new ArrayList<>();

        List<Metric> falseList = Objects.requireNonNull(totalMetricList).stream()
                .filter(metric -> StringUtils.equals("false", metric.getSuccess()))
                .collect(Collectors.toList());

        Map<String, List<Metric>> collect = falseList.stream()
                .collect(Collectors.groupingBy(GenerateReport::getResponseCodeAndFailureMessage));

        for (Map.Entry<String, List<Metric>> next : collect.entrySet()) {
            String key = next.getKey();
            List<Metric> metricList = next.getValue();
            List<Metric> list = new ArrayList<>();
            for (Metric metric : totalMetricList) {
                if (StringUtils.equals(metric.getLabel(), metricList.get(0).getLabel())) {
                    list.add(metric);
                }
            }

            ErrorsTop5 errorsTop5 = new ErrorsTop5();
            errorsTop5.setSamples(String.valueOf(list.size()));
            errorsTop5.setSample(metricList.get(0).getLabel());
            errorsTop5.setErrors(String.valueOf(metricList.size()));
            errorsTop5.setErrorsAllSize(metricList.size());
            errorsTop5.setError(key);
            errorsTop5s.add(errorsTop5);
        }

        errorsTop5s.sort((t0, t1) -> t1.getErrorsAllSize().compareTo(t0.getErrorsAllSize()));

        if (errorsTop5s.size() >= ERRORS_TOP_SIZE) {
            errorsTop5s = errorsTop5s.subList(0, ERRORS_TOP_SIZE);
        }

        top5DTO.setLabel("Total");
        top5DTO.setErrorsTop5List(errorsTop5s);
        top5DTO.setTotalSamples(String.valueOf(totalMetricList.size()));
        top5DTO.setTotalErrors(String.valueOf(falseList.size()));
        int size = errorsTop5s.size();
        // Total行 信息
        top5DTO.setError1(size > 0 ? errorsTop5s.get(0).getError() : null);
        top5DTO.setError1Size(size > 0 ? errorsTop5s.get(0).getErrors() : null);
        top5DTO.setError2(size > 1 ? errorsTop5s.get(1).getError() : null);
        top5DTO.setError2Size(size > 1 ? errorsTop5s.get(1).getErrors() : null);
        top5DTO.setError3(size > 2 ? errorsTop5s.get(2).getError() : null);
        top5DTO.setError3Size(size > 2 ? errorsTop5s.get(2).getErrors() : null);
        top5DTO.setError4(size > 3 ? errorsTop5s.get(3).getError() : null);
        top5DTO.setError4Size(size > 3 ? errorsTop5s.get(3).getErrors() : null);
        top5DTO.setError5(size > 4 ? errorsTop5s.get(4).getError() : null);
        top5DTO.setError5Size(size > 4 ? errorsTop5s.get(4).getErrors() : null);

        return top5DTO;
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

    /**
     * @param "yyyy-MM-dd HH:mm:ss"
     * @return "HH:mm:ss"
     */
    private static String formatDate(String dateString) throws ParseException {
        SimpleDateFormat before = new SimpleDateFormat(DATE_TIME_PATTERN);
        SimpleDateFormat after = new SimpleDateFormat(TIME_PATTERN);
        return after.format(before.parse(dateString));
    }

}