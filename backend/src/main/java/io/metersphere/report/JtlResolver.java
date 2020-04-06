package io.metersphere.report;

import com.alibaba.fastjson.JSONObject;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.metersphere.report.base.*;
import io.metersphere.report.dto.ErrorsTop5DTO;
import io.metersphere.report.dto.RequestStatisticsDTO;
import org.apache.commons.lang3.StringUtils;

import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class JtlResolver {

    private static final Integer ERRORS_TOP_SIZE = 5;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

            /**
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
            /**
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
        Map<String, List<Metric>> collect = totalLineList.stream().collect(Collectors.groupingBy(Metric::getTimestamp));
        Iterator<Map.Entry<String, List<Metric>>> iterator = collect.entrySet().iterator();

        int maxUsers = 0, totalElapsed = 0;
        float totalBytes = 0f;

        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> entry = iterator.next();
            List<Metric> metricList = entry.getValue();

            if (metricList.size() > maxUsers) {
                maxUsers = metricList.size();
            }

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


    public static ChartsData getLoadChartData(String jtlString) {
        ChartsData data = new ChartsData();
        List<Metric> totalMetricList = JtlResolver.resolver(jtlString);

        List<String> users = new ArrayList<>();
        List<String> hits = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> timeList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>(5);

        DecimalFormat decimalFormat = new DecimalFormat("0.0");

        if (totalMetricList != null) {
            for (Metric metric : totalMetricList) {
                metric.setTimestamp(stampToDate(metric.getTimestamp()));
            }
        }
        Map<String, List<Metric>> collect = Objects.requireNonNull(totalMetricList).stream().collect(Collectors.groupingBy(Metric::getTimestamp));
        List<Map.Entry<String, List<Metric>>> entries = new ArrayList<>(collect.entrySet());
        entries.sort(JtlResolver::sortByDate);

        for (Map.Entry<String, List<Metric>> entry : entries) {
            int failSize = 0;
            List<Metric> metrics = entry.getValue();
            Map<String, List<Metric>> metricsMap = metrics.stream().collect(Collectors.groupingBy(Metric::getThreadName));
            int maxUsers = metricsMap.size();
            for (Metric metric : metrics) {
                String isSuccess = metric.getSuccess();
                if (!"true".equals(isSuccess)) {
                    failSize++;
                }
            }
            // todo
            timeList.add(entry.getKey());
            hits.add(decimalFormat.format(metrics.size() * 1.0 / maxUsers));
            users.add(String.valueOf(maxUsers));
            errors.add(String.valueOf(failSize));

        }

        resultMap.put("users", users);
        resultMap.put("hits", hits);
        resultMap.put("errors", errors);

        JSONObject serices = new JSONObject(resultMap);
        data.setxAxis(StringUtils.join(",", timeList));
        data.setSerices(serices.toString());

        return data;
    }

    public static ChartsData getResponseTimeChartData(String jtlString) {
        ChartsData chartsData = new ChartsData();
        List<Metric> totalMetricList = JtlResolver.resolver(jtlString);

        totalMetricList.forEach(metric -> {
            metric.setTimestamp(stampToDate(metric.getTimestamp()));
        });

        Map<String, List<Metric>> metricMap = totalMetricList.stream().collect(Collectors.groupingBy(Metric::getTimestamp));
        List<Map.Entry<String, List<Metric>>> entries = new ArrayList<>(metricMap.entrySet());
        entries.sort(JtlResolver::sortByDate);

        List<String> resTimeList = new ArrayList<>();
        List<String> timestampList = new ArrayList<>();

        for (Map.Entry<String, List<Metric>> entry : entries) {
            List<Metric> metricList = entry.getValue();
            int sumElapsedTime = metricList.stream().mapToInt(metric -> Integer.parseInt(metric.getElapsed())).sum();
            timestampList.add(entry.getKey());
            resTimeList.add(String.valueOf(sumElapsedTime / metricList.size()));
        }

        chartsData.setxAxis(StringUtils.join(",", timestampList));
        chartsData.setSerices(StringUtils.join(",", resTimeList));
        return chartsData;
    }

    private static String stampToDate(String s) {
        long lt = Long.parseLong(s);
        Date date = new Date(lt);
        return simpleDateFormat.format(date);
    }

    private static int sortByDate(Map.Entry<String, List<Metric>> map1, Map.Entry<String, List<Metric>> map2) {
        Date date1 = null, date2 = null;
        try {
            date1 = simpleDateFormat.parse(map1.getKey());
            date2 = simpleDateFormat.parse(map2.getKey());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (int) (Objects.requireNonNull(date1).getTime() - Objects.requireNonNull(date2).getTime());
    }
}