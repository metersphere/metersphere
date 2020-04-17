package io.metersphere.report;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.report.base.*;
import io.metersphere.report.dto.ErrorsTop5DTO;
import io.metersphere.report.dto.RequestStatisticsDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.dashboard.JsonizerVisitor;
import org.apache.jmeter.report.processor.*;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.ActiveThreadsGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.HitsPerSecondGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.ResponseTimeOverTimeGraphConsumer;
import org.apache.jmeter.util.JMeterUtils;

import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
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

public class JtlResolver {

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

    public static RequestStatisticsDTO getRequestStatistics(String jtlString) {
        List<Integer> allElapseTimeList = new ArrayList<>();
        List<RequestStatistics> requestStatisticsList = new ArrayList<>();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        List<Metric> totalMetricList = resolver(jtlString);
        Map<String, List<Metric>> jtlLabelMap = totalMetricList.stream().collect(Collectors.groupingBy(Metric::getLabel));
        Iterator<Map.Entry<String, List<Metric>>> iterator = jtlLabelMap.entrySet().iterator();

        int totalElapsedTime = 0;
        float totalBytes = 0f;

        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> entry = iterator.next();
            String label = entry.getKey();
            List<Metric> metricList = entry.getValue();
            List<Integer> elapsedList = new ArrayList<>();

            int jtlSamplesSize = 0, oneLineElapsedTime = 0, failSize = 0;
            float oneLineBytes = 0f;

            for (int i = 0; i < metricList.size(); i++) {
                try {
                    Metric row = metricList.get(i);
                    String elapsed = row.getElapsed();
                    oneLineElapsedTime += Integer.parseInt(elapsed);
                    totalElapsedTime += Integer.parseInt(elapsed);
                    elapsedList.add(Integer.valueOf(elapsed));
                    allElapseTimeList.add(Integer.valueOf(elapsed));

                    String isSuccess = row.getSuccess();
                    if (!"true".equals(isSuccess)) {
                        failSize++;
                    }
                    String bytes = row.getBytes();
                    oneLineBytes += Float.parseFloat(bytes);
                    totalBytes += Float.parseFloat(bytes);
                    jtlSamplesSize++;
                } catch (Exception e) {
                    System.out.println("exception i:" + i);
                }
            }

            Collections.sort(elapsedList);

            int tp90 = elapsedList.size() * 90 / 100;
            int tp95 = elapsedList.size() * 95 / 100;
            int tp99 = elapsedList.size() * 99 / 100;

            metricList.sort(Comparator.comparing(t0 -> Long.valueOf(t0.getTimestamp())));
            long time = Long.parseLong(metricList.get(metricList.size() - 1).getTimestamp()) - Long.parseLong(metricList.get(0).getTimestamp())
                    + Long.parseLong(metricList.get(metricList.size() - 1).getElapsed());

            RequestStatistics requestStatistics = new RequestStatistics();
            requestStatistics.setRequestLabel(label);
            requestStatistics.setSamples(jtlSamplesSize);

            String average = decimalFormat.format((float) oneLineElapsedTime / jtlSamplesSize);
            requestStatistics.setAverage(average);

            /*
             * TP90的计算
             * 1，把一段时间内全部的请求的响应时间，从小到大排序，获得序列A
             * 2，总的请求数量，乘以90%，获得90%对应的请求个数C
             * 3，从序列A中找到第C个请求，它的响应时间，即为TP90的值
             * 其余相似的指标还有TP95, TP99
             */
            // todo tp90
            requestStatistics.setTp90(elapsedList.get(tp90) + "");
            requestStatistics.setTp95(elapsedList.get(tp95) + "");
            requestStatistics.setTp99(elapsedList.get(tp99) + "");

            double avgHits = (double) metricList.size() / (time * 1.0 / 1000);
            requestStatistics.setAvgHits(decimalFormat.format(avgHits));

            requestStatistics.setMin(elapsedList.get(0) + "");
            requestStatistics.setMax(elapsedList.get(jtlSamplesSize - 1) + "");
            requestStatistics.setErrors(decimalFormat.format(failSize * 100.0 / jtlSamplesSize) + "%");
            requestStatistics.setKo(failSize);
            /*
             * 所有的相同请求的bytes总和 / 1024 / 请求持续运行的时间=sum(bytes)/1024/total time
             * total time = 最大时间戳 - 最小时间戳 + 最后请求的响应时间
             */
            requestStatistics.setKbPerSec(decimalFormat.format(oneLineBytes * 1.0 / 1024 / (time * 1.0 / 1000)));
            requestStatisticsList.add(requestStatistics);
        }

        Collections.sort(allElapseTimeList);
        int totalTP90 = allElapseTimeList.size() * 90 / 100;
        int totalTP95 = allElapseTimeList.size() * 95 / 100;
        int totalTP99 = allElapseTimeList.size() * 99 / 100;

        Integer min = allElapseTimeList.get(0);
        Integer max = allElapseTimeList.get(allElapseTimeList.size() - 1);

        int allSamples = requestStatisticsList.stream().mapToInt(RequestStatistics::getSamples).sum();
        int failSize = requestStatisticsList.stream().mapToInt(RequestStatistics::getKo).sum();

        double errors = (double) failSize / allSamples * 100;
        String totalErrors = decimalFormat.format(errors);
        double average = (double) totalElapsedTime / allSamples;
        String totalAverage = decimalFormat.format(average);

        RequestStatisticsDTO statisticsDTO = new RequestStatisticsDTO();
        statisticsDTO.setRequestStatisticsList(requestStatisticsList);
        statisticsDTO.setTotalLabel("Total");
        statisticsDTO.setTotalSamples(String.valueOf(allSamples));
        statisticsDTO.setTotalErrors(totalErrors + "%");
        statisticsDTO.setTotalAverage(totalAverage);
        statisticsDTO.setTotalMin(String.valueOf(min));
        statisticsDTO.setTotalMax(String.valueOf(max));
        statisticsDTO.setTotalTP90(String.valueOf(allElapseTimeList.get(totalTP90)));
        statisticsDTO.setTotalTP95(String.valueOf(allElapseTimeList.get(totalTP95)));
        statisticsDTO.setTotalTP99(String.valueOf(allElapseTimeList.get(totalTP99)));

        totalMetricList.sort(Comparator.comparing(t0 -> Long.valueOf(t0.getTimestamp())));

        long ms = Long.parseLong(totalMetricList.get(totalMetricList.size() - 1).getTimestamp()) - Long.parseLong(totalMetricList.get(0).getTimestamp())
                + Long.parseLong(totalMetricList.get(totalMetricList.size() - 1).getElapsed());
        double avgThroughput = (double) totalMetricList.size() / (ms * 1.0 / 1000);

        statisticsDTO.setTotalAvgHits(decimalFormat.format(avgThroughput));
        statisticsDTO.setTotalAvgBandwidth(decimalFormat.format(totalBytes * 1.0 / 1024 / (ms * 1.0 / 1000)));

        return statisticsDTO;
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

        Map<String, List<Metric>> jtlMap = falseList.stream().collect(Collectors.groupingBy(JtlResolver::getResponseCodeAndFailureMessage));

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
                .collect(Collectors.groupingBy(JtlResolver::getResponseCodeAndFailureMessage));

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

        List<Metric> totalLineList = JtlResolver.resolver(jtlString);
        // todo
        List<Metric> totalLineList2 = JtlResolver.resolver(jtlString);
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
        Map<String, Object> activeThreadMap = getResultDataMap(jtlString, new ActiveThreadsGraphConsumer());
        Map<String, Object> hitsMap = getResultDataMap(jtlString, new HitsPerSecondGraphConsumer());
        List<ChartsData> activeThreadList = new ArrayList<>();
        List<ChartsData> hitsList = new ArrayList<>();
        mapResolver(activeThreadMap, activeThreadList, "users");
        mapResolver(hitsMap, hitsList, "hits");
        activeThreadList.addAll(hitsList);
        return activeThreadList;
    }

    public static List<ChartsData> getResponseTimeChartData(String jtlString) {
        Map<String, Object> activeThreadMap = getResultDataMap(jtlString, new ActiveThreadsGraphConsumer());
        Map<String, Object> responseTimeMap = getResultDataMap(jtlString, new ResponseTimeOverTimeGraphConsumer());
        List<ChartsData> activeThreadList = new ArrayList<>();
        List<ChartsData> responseTimeList = new ArrayList<>();
        mapResolver(activeThreadMap, activeThreadList, "users");
        mapResolver(responseTimeMap, responseTimeList, "responseTime");
        activeThreadList.addAll(responseTimeList);
        return activeThreadList;

    }

    public static void mapResolver(Map<String, Object> map, List list, String seriesName) {
        // ThreadGroup-1
        for (String key : map.keySet()) {
            MapResultData mapResultData = (MapResultData) map.get(key);
            ResultData maxY = mapResultData.getResult("maxY");
            ListResultData series = (ListResultData) mapResultData.getResult("series");
            if (series.getSize() > 0) {
                for (int j = 0; j < series.getSize(); j++) {
                    MapResultData resultData = (MapResultData) series.get(j);
                    // data, isOverall, label, isController
                    ListResultData data = (ListResultData) resultData.getResult("data");
                    ValueResultData label = (ValueResultData) resultData.getResult("label");

                    if (data.getSize() > 0) {
                        for (int i = 0; i < data.getSize(); i++) {
                            ListResultData listResultData = (ListResultData) data.get(i);
                            String result = listResultData.accept(new JsonizerVisitor());
                            result = result.substring(1, result.length() - 1);
                            String[] split = result.split(",");
                            ChartsData chartsData = new ChartsData();
                            BigDecimal bigDecimal = new BigDecimal(split[0]);
                            String timeStamp = bigDecimal.toPlainString();
                            String time = null;
                            try {
                                time = formatDate(stampToDate(DATE_TIME_PATTERN, timeStamp));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            chartsData.setxAxis(time);
                            chartsData.setyAxis(new BigDecimal(split[1].trim()));
                            if (series.getSize() == 1) {
                                chartsData.setGroupName(seriesName);
                            } else {
                                chartsData.setGroupName((String) label.getValue());
                            }
                            list.add(chartsData);
                        }
                    }
                }


            }
        }
    }

    public static Map<String, Object> getResultDataMap(String jtlString, AbstractOverTimeGraphConsumer timeGraphConsumer) {
        int row = 0;
        AbstractOverTimeGraphConsumer abstractOverTimeGraphConsumer = timeGraphConsumer;
        abstractOverTimeGraphConsumer.setGranularity(60000);
        // 这个路径不存在
        JMeterUtils.loadJMeterProperties("jmeter.properties");
        SampleMetadata sampleMetaData = createTestMetaData();
        SampleContext sampleContext = new SampleContext();
        abstractOverTimeGraphConsumer.setSampleContext(sampleContext);
        abstractOverTimeGraphConsumer.initialize();
        abstractOverTimeGraphConsumer.startConsuming();
        StringTokenizer tokenizer = new StringTokenizer(jtlString, "\n");
        // 去掉第一行
        tokenizer.nextToken();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            String[] data = line.split(",", -1);
            Sample sample = new Sample(row++, sampleMetaData, data);
            abstractOverTimeGraphConsumer.consume(sample, 0);
        }
        abstractOverTimeGraphConsumer.stopConsuming();
        return sampleContext.getData();
    }

    // Create a static SampleMetadataObject
    private static SampleMetadata createTestMetaData() {
        String columnsString = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";
        columnsString = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";

        String[] columns = new String[17];
        int lastComa = 0;
        int columnIndex = 0;
        for (int i = 0; i < columnsString.length(); i++) {
            if (columnsString.charAt(i) == ',') {
                columns[columnIndex] = columnsString.substring(lastComa, i);
                lastComa = i + 1;
                columnIndex++;
            } else if (i + 1 == columnsString.length()) {
                columns[columnIndex] = columnsString.substring(lastComa, i + 1);
            }
        }
        return new SampleMetadata(',', columns);
    }

    public static ReportTimeInfo getReportTimeInfo(String jtlString) {
        ReportTimeInfo reportTimeInfo = new ReportTimeInfo();
        List<Metric> totalLineList = JtlResolver.resolver(jtlString);

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