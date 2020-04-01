package io.metersphere.report;

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

    public static RequestStatisticsDTO getRequestStatistics(String jtlString){
        List<Metric> total = resolver(jtlString);
        Map<String, List<Metric>> map = total.stream().collect(Collectors.groupingBy(Metric::getLabel));
        List<RequestStatistics> requestStatisticsList = new ArrayList<>();
        Iterator<Map.Entry<String, List<Metric>>> iterator = map.entrySet().iterator();
        List<Integer> allelapse = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("0.00");
        int totalAverage = 0;
        float allBytes = 0f;
        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> entry = iterator.next();
            String label = entry.getKey();
            List<Metric> list = entry.getValue();
            int index=0;
            int sumElapsed=0;
            int failSize = 0;
            // 以list为单位 total bytes
            float totalBytes = 0f;
            List<Integer> elapsedList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                try {
                    Metric row = list.get(i);
                    String elapsed = row.getElapsed();
                    sumElapsed += Integer.parseInt(elapsed);
                    totalAverage += Integer.parseInt(elapsed);
                    elapsedList.add(Integer.valueOf(elapsed));
                    allelapse.add(Integer.valueOf(elapsed));
                    String success = row.getSuccess();
                    if (!"true".equals(success)){
                        failSize++;
                    }
                    String bytes = row.getBytes();
                    totalBytes += Float.parseFloat(bytes);
                    allBytes += Float.parseFloat(bytes);
                    index++;
                }catch (Exception e){
                    System.out.println("exception i:"+i);
                }
            }
            Collections.sort(elapsedList);

            int tp90 = elapsedList.size()*90/100;
            int tp95 = elapsedList.size()*95/100;
            int tp99 = elapsedList.size()*99/100;

            list.sort(Comparator.comparing(t0 -> Long.valueOf(t0.getTimestamp())));
            long time = Long.parseLong(list.get(list.size()-1).getTimestamp()) - Long.parseLong(list.get(0).getTimestamp()) + Long.parseLong(list.get(list.size()-1).getElapsed());

            RequestStatistics requestStatistics = new RequestStatistics();
            requestStatistics.setRequestLabel(label);
            requestStatistics.setSamples(index);

            String s = df.format((float)sumElapsed/index);
            requestStatistics.setAverage(s+"");

            /**
             * TP90的计算
             * 1，把一段时间内全部的请求的响应时间，从小到大排序，获得序列A
             * 2，总的请求数量，乘以90%，获得90%对应的请求个数C
             * 3，从序列A中找到第C个请求，它的响应时间，即为TP90的值
             * 其余相似的指标还有TP95, TP99
             */
            // todo tp90
            requestStatistics.setTp90(elapsedList.get(tp90)+"");
            requestStatistics.setTp95(elapsedList.get(tp95)+"");
            requestStatistics.setTp99(elapsedList.get(tp99)+"");

            double avgHits = (double)list.size() / (time * 1.0 / 1000);
            requestStatistics.setAvgHits(df.format(avgHits));

            requestStatistics.setMin(elapsedList.get(0)+"");
            requestStatistics.setMax(elapsedList.get(index-1)+"");
            requestStatistics.setErrors(df.format(failSize * 100.0 / index)+"%");
            requestStatistics.setKo(failSize);
            /**
             * 所有的相同请求的bytes总和 / 1024 / 请求持续运行的时间=sum(bytes)/1024/total time
             * total time = 最大时间戳 - 最小时间戳 + 最后请求的响应时间
             */
            requestStatistics.setKbPerSec(df.format(totalBytes * 1.0 / 1024 / (time * 1.0 / 1000)));
            requestStatisticsList.add(requestStatistics);
        }

        Collections.sort(allelapse);
        int totalTP90 = allelapse.size()*90/100;
        int totalTP95 = allelapse.size()*95/100;
        int totalTP99 = allelapse.size()*99/100;

        Integer min = allelapse.get(0);
        Integer max = allelapse.get(allelapse.size() - 1);

        int allSamples = requestStatisticsList.stream().mapToInt(RequestStatistics::getSamples).sum();
        int failSize = requestStatisticsList.stream().mapToInt(RequestStatistics::getKo).sum();

        double errors = (double)failSize / allSamples * 100;
        String totalerrors = df.format(errors);
        double average = (double)totalAverage / allSamples;
        String totalaverage = df.format(average);

        RequestStatisticsDTO statisticsDTO = new RequestStatisticsDTO();
        statisticsDTO.setRequestStatisticsList(requestStatisticsList);
        statisticsDTO.setTotalLabel("Total");
        statisticsDTO.setTotalSamples(String.valueOf(allSamples));
        statisticsDTO.setTotalErrors(totalerrors + "%");
        statisticsDTO.setTotalAverage(totalaverage);
        statisticsDTO.setTotalMin(String.valueOf(min));
        statisticsDTO.setTotalMax(String.valueOf(max));
        statisticsDTO.setTotalTP90(String.valueOf(allelapse.get(totalTP90)));
        statisticsDTO.setTotalTP95(String.valueOf(allelapse.get(totalTP95)));
        statisticsDTO.setTotalTP99(String.valueOf(allelapse.get(totalTP99)));

        total.sort(Comparator.comparing(t0 -> Long.valueOf(t0.getTimestamp())));
        long ms = Long.valueOf(total.get(total.size()-1).getTimestamp()) - Long.valueOf(total.get(0).getTimestamp()) + Long.parseLong(total.get(total.size()-1).getElapsed());
        double avgThroughput = (double)total.size() / (ms * 1.0 / 1000);
        statisticsDTO.setTotalAvgHits(df.format(avgThroughput));

        statisticsDTO.setTotalAvgBandwidth(df.format(allBytes * 1.0 / 1024 / (ms * 1.0 / 1000)));
        return statisticsDTO;
    }

    // report - Errors
    public static List<Errors> getErrorsList(String jtlString) {
        List<Metric> totalLines = resolver(jtlString);
        List<Metric> falseList = totalLines.stream().filter(metric -> StringUtils.equals("false", metric.getSuccess())).collect(Collectors.toList());
        List<Errors> errorsList = new ArrayList<>();
        Map<String, List<Metric>> collect = falseList.stream().collect(Collectors.groupingBy(JtlResolver::getResponseCodeAndFailureMessage));
        Iterator<Map.Entry<String, List<Metric>>> iterator = collect.entrySet().iterator();
        DecimalFormat df = new DecimalFormat("0.00");
        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> next = iterator.next();
            String key = next.getKey();
            List<Metric> value = next.getValue();
            Errors errors = new Errors();
            errors.setErrorType(key);
            errors.setErrorNumber(String.valueOf(value.size()));
            int errorSize = value.size();
            int errorAllSize = falseList.size();
            int allSamples = totalLines.size();
            errors.setPrecentOfErrors(df.format((double)errorSize / errorAllSize * 100) + "%");
            errors.setPrecentOfAllSamples(df.format((double)errorSize / allSamples * 100) + "%");
            errorsList.add(errors);
        }
        return errorsList;
    }

    private static String getResponseCodeAndFailureMessage(Metric metric) {
        return metric.getResponseCode() + "/" + metric.getResponseMessage();
    }

    // report - Errors Top 5
    public static ErrorsTop5DTO getErrorsTop5DTO(String jtlString) {
        List<Metric> totalLines = resolver(jtlString);
        ErrorsTop5DTO top5DTO = new ErrorsTop5DTO();
        List<Metric> falseList = totalLines.stream().filter(metric -> StringUtils.equals("false", metric.getSuccess())).collect(Collectors.toList());
        Map<String, List<Metric>> collect = falseList.stream().collect(Collectors.groupingBy(JtlResolver::getResponseCodeAndFailureMessage));
        Iterator<Map.Entry<String, List<Metric>>> iterator = collect.entrySet().iterator();
        List<ErrorsTop5> errorsTop5s = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> next = iterator.next();
            String key = next.getKey();
            List<Metric> value = next.getValue();
            ErrorsTop5 errorsTop5 = new ErrorsTop5();
            List<Metric> list = totalLines.stream()
                    .filter(metric -> StringUtils.equals(metric.getLabel(), value.get(0).getLabel())).collect(Collectors.toList());
            errorsTop5.setSamples(String.valueOf(list.size()));
            errorsTop5.setSample(value.get(0).getLabel());
            errorsTop5.setErrors(String.valueOf(value.size()));
            errorsTop5.setErrorsAllSize(value.size());
            errorsTop5.setError(key);
            errorsTop5s.add(errorsTop5);
        }

        errorsTop5s.sort((t0, t1) -> t1.getErrorsAllSize().compareTo(t0.getErrorsAllSize()));

        if (errorsTop5s.size() >= 5) {
            errorsTop5s = errorsTop5s.subList(0, 5);
        }

        top5DTO.setLabel("Total");
        top5DTO.setErrorsTop5List(errorsTop5s);
        top5DTO.setTotalSamples(String.valueOf(totalLines.size()));
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

    // report - TestOverview
    public static TestOverview getTestOverview(String jtlString) {
        TestOverview testOverview = new TestOverview();
        List<Metric> total = JtlResolver.resolver(jtlString);
        Map<String, List<Metric>> collect = total.stream().collect(Collectors.groupingBy(Metric::getTimestamp));
        Iterator<Map.Entry<String, List<Metric>>> iterator = collect.entrySet().iterator();
        int max = 0;
        int totalElapsed = 0;
        float totalBytes = 0f;
        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> entry = iterator.next();
            List<Metric> list = entry.getValue();
            if (list.size() > max) {
                max = list.size();
            }
            for (int i = 0; i < list.size(); i++) {
                Metric metric = list.get(i);
                String elapsed = metric.getElapsed();
                totalElapsed += Integer.parseInt(elapsed);
                String bytes = metric.getBytes();
                totalBytes += Float.parseFloat(bytes);
            }
        }

        total.sort(Comparator.comparing(t0 -> Long.valueOf(t0.getTimestamp())));
        DecimalFormat df = new DecimalFormat("0.00");

        testOverview.setMaxUsers(String.valueOf(max));

        List<Metric> list90 = total.subList(0, total.size() * 9 / 10);
        long sum = list90.stream().mapToLong(metric -> Long.parseLong(metric.getElapsed())).sum();
        double avg90 = (double)sum / 1000 / list90.size();
        testOverview.setResponseTime90(df.format(avg90));

        Long timestamp1 = Long.valueOf(total.get(0).getTimestamp());
        Long timestamp2 = Long.valueOf(total.get(total.size()-1).getTimestamp());
        long time = timestamp2 - timestamp1 + Long.parseLong(total.get(total.size()-1).getElapsed());
        double avgThroughput = (double)total.size() / (time * 1.0 / 1000);
        testOverview.setAvgThroughput(df.format(avgThroughput));

        List<Metric> falseList = total.stream().filter(metric -> StringUtils.equals("false", metric.getSuccess())).collect(Collectors.toList());
        double errors = falseList.size() * 1.0 / total.size() * 100;
        testOverview.setErrors(df.format(errors));

        double avg = totalElapsed * 1.0 / total.size() / 1000; // s
        testOverview.setAvgResponseTime(df.format(avg));

        double bandwidth = totalBytes * 1.0 / time;
        testOverview.setAvgBandwidth(df.format(bandwidth));

        return testOverview;
    }


    public static ChartsData getLoadChartData(String jtlString) {
        ChartsData data = new ChartsData();
        List<Metric> total = JtlResolver.resolver(jtlString);

        ////
        List<String> users = new ArrayList<>();
        List<String> hits = new ArrayList<>();
        List<String> erorrs = new ArrayList<>();
        List<String> timeList = new ArrayList<>();
        //// todo SimpleDateFormat
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("0.0");

        total.sort(Comparator.comparing(metric -> Long.valueOf(metric.getTimestamp())));
        total.forEach(metric -> {
            metric.setTimestamp(stampToDate(metric.getTimestamp()));
        });

        Map<String, List<Metric>> collect = total.stream().collect(Collectors.groupingBy(Metric::getTimestamp));
        List<Map.Entry<String, List<Metric>>> entries = new ArrayList<>(collect.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, List<Metric>>>() {
            @Override
            public int compare(Map.Entry<String, List<Metric>> t1, Map.Entry<String, List<Metric>> t2) {
                Date date1 = null,date2 = null;
                try {
                    date1 = simpleDateFormat.parse(t1.getKey());
                    date2 = simpleDateFormat.parse(t2.getKey());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return (int) (date1.getTime() - date2.getTime());
            }
        });

        for (int i = 0; i < entries.size(); i++) {
            int failSize = 0;
            Map.Entry<String, List<Metric>> map = entries.get(i);
            List<Metric> metrics = map.getValue();
            Map<String, List<Metric>> metricsMap = metrics.stream().collect(Collectors.groupingBy(Metric::getThreadName));
            int maxUsers = metricsMap.size();
            for (int j = 0; j < metrics.size(); j++) {
                Metric metric = metrics.get(j);
                String success = metric.getSuccess();
                if (!"true".equals(success)){
                    failSize++;
                }
            }
            // todo
            timeList.add(map.getKey());
            hits.add(df.format(metrics.size() * 1.0 / maxUsers));
            users.add(String.valueOf(maxUsers));
            erorrs.add(String.valueOf(failSize));

        }

        data.setTime(timeList);
        data.setUsers(users);
        data.setHits(hits);
        data.setErrors(erorrs);

        return data;
    }

    private static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = Long.parseLong(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
}