package io.metersphere.report;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.metersphere.report.base.*;
import io.metersphere.report.dto.ErrorsTop5DTO;
import io.metersphere.report.dto.RequestStatisticsDTO;
import org.apache.bcel.verifier.statics.LONG_Upper;
import org.apache.commons.lang3.StringUtils;

import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
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

    private static RequestStatisticsDTO getOneRpsResult(Map<String, List<Metric>> map){
        RequestStatisticsDTO statisticsDTO = new RequestStatisticsDTO();
        List<RequestStatistics> requestStatisticsList = new ArrayList<>();
        Iterator<Map.Entry<String, List<Metric>>> iterator = map.entrySet().iterator();
        List<Integer> allelapse = new ArrayList<>();
        Integer totalAverage = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> entry = iterator.next();
            String label = entry.getKey();
            List<Metric> list = entry.getValue();
            List<String> timestampList = list.stream().map(Metric::getTimestamp).collect(Collectors.toList());
            int index=0;
            //总的响应时间
            int sumElapsed=0;
            Integer failSize = 0;
            Float totalBytes = 0f;
            List<Integer> elapsedList = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                try {
                    Metric row = list.get(i);
                    //响应时间
                    String elapsed = row.getElapsed();
                    sumElapsed += Integer.valueOf(elapsed);
                    totalAverage += Integer.valueOf(elapsed);
                    elapsedList.add(Integer.valueOf(elapsed));
                    allelapse.add(Integer.valueOf(elapsed));
                    //成功与否
                    String success = row.getSuccess();
                    if (!"true".equals(success)){
                        failSize++;
                    }
                    //字节
                    String bytes = row.getBytes();
                    totalBytes += Float.valueOf(bytes);
                    index++;
                }catch (Exception e){
                    System.out.println("exception i:"+i);
                }
            }

            Collections.sort(elapsedList);

            Integer tp90 = elapsedList.size()*90/100;
            Integer tp95 = elapsedList.size()*95/100;
            Integer tp99 = elapsedList.size()*99/100;
            Long l = Long.valueOf(timestampList.get(timestampList.size()-1)) - Long.valueOf(timestampList.get(0));

            RequestStatistics requestStatistics = new RequestStatistics();
            requestStatistics.setRequestLabel(label);
            requestStatistics.setSamples(index);
            DecimalFormat df = new DecimalFormat("0.00");
            String s = df.format((float)sumElapsed/index);
            requestStatistics.setAverage(s+"");
            /**
             * TP90的计算
             * 1，把一段时间内全部的请求的响应时间，从小到大排序，获得序列A
             * 2，总的请求数量，乘以90%，获得90%对应的请求个数C
             * 3，从序列A中找到第C个请求，它的响应时间，即为TP90的值
             * 其余相似的指标还有TP95, TP99
             */
            requestStatistics.setTp90(elapsedList.get(tp90)+"");
            requestStatistics.setTp95(elapsedList.get(tp95)+"");
            requestStatistics.setTp99(elapsedList.get(tp99)+"");
            requestStatistics.setMin(elapsedList.get(0)+"");
            requestStatistics.setMax(elapsedList.get(index-1)+"");
            requestStatistics.setErrors(String.format("%.2f",failSize*100.0/index)+"%");
            requestStatistics.setKo(failSize);
            /**
             * 所有的相同请求的bytes总和 / 1024 / 请求持续运行的时间=sum(bytes)/1024/total time
             */
            // todo Avg Bandwidth(KBytes/s)          请求之间时间戳间隔l 可能为0
            requestStatistics.setKbPerSec(String.format("%.2f",totalBytes*1.0/1024/(l*1.0/1000)));
            requestStatisticsList.add(requestStatistics);
        }
        Collections.sort(allelapse);

        Integer totalTP90 = allelapse.size()*90/100;
        Integer totalTP95 = allelapse.size()*95/100;
        Integer totalTP99 = allelapse.size()*99/100;

        Integer min = allelapse.get(0);
        Integer max = allelapse.get(allelapse.size() - 1);

        Integer allSamples = requestStatisticsList.stream().mapToInt(RequestStatistics::getSamples).sum();
        Integer failSize = requestStatisticsList.stream().mapToInt(RequestStatistics::getKo).sum();
        DecimalFormat df = new DecimalFormat("0.00");
        double errors = (double)failSize / allSamples * 100;
        String totalerrors = df.format(errors);
        double average = (double)totalAverage / allSamples;
        String totalaverage = df.format(average);

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
        // todo
//        statisticsDTO.setTotalAvgBandwidth();
        return statisticsDTO;
    }

    // report - Aggregate Report
    public static RequestStatisticsDTO getRequestStatistics(String jtlString) {
        List<Metric> totalLines = resolver(jtlString);
        Map<String, List<Metric>> map = totalLines.stream().collect(Collectors.groupingBy(Metric::getLabel));
        return getOneRpsResult(map);
    }

    // report - Errors
    public static List<Errors> getErrorsList(String jtlString) {
        List<Metric> totalLines = resolver(jtlString);
        List<Metric> falseList = totalLines.stream().filter(metric -> StringUtils.equals("false", metric.getSuccess())).collect(Collectors.toList());
        List<Errors> errorsList = new ArrayList<>();
        Map<String, List<Metric>> collect = falseList.stream().collect(Collectors.groupingBy(JtlResolver::getResponseCodeAndFailureMessage));
        Iterator<Map.Entry<String, List<Metric>>> iterator = collect.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> next = iterator.next();
            String key = next.getKey();
            List<Metric> value = next.getValue();
            Errors errors = new Errors();
            errors.setErrorType(key);
            errors.setErrorNumber(String.valueOf(value.size()));
            Integer errorSize = value.size();
            Integer errorAllSize = falseList.size();
            Integer allSamples = totalLines.size();
            DecimalFormat df = new DecimalFormat("0.00");
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

        Collections.sort(errorsTop5s, (t0, t1) -> t1.getErrorsAllSize().compareTo(t0.getErrorsAllSize()));

        if (errorsTop5s.size() >= 5) {
            errorsTop5s = errorsTop5s.subList(0, 5);
        }

        top5DTO.setLabel("Total");
        top5DTO.setErrorsTop5List(errorsTop5s);
        top5DTO.setTotalSamples(String.valueOf(totalLines.size()));
        top5DTO.setTotalErrors(String.valueOf(falseList.size()));
        Integer size = errorsTop5s.size();
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
        Integer max = 0;
        Integer totalElapsed = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> entry = iterator.next();
            List<Metric> list = entry.getValue();
            if (list.size() > max) {
                max = list.size();
            }
            for (int i = 0; i < list.size(); i++) {
                Metric metric = list.get(i);
                String elapsed = metric.getElapsed();
                totalElapsed += Integer.valueOf(elapsed);
            }
        }

        Collections.sort(total, Comparator.comparing(t0 -> Long.valueOf(t0.getTimestamp())));
        Long timestamp1 = Long.valueOf(total.get(0).getTimestamp());
        Long timestamp2 = Long.valueOf(total.get(total.size()-1).getTimestamp());
        Long seconds = (timestamp2 - timestamp1) / 1000;
        DecimalFormat df = new DecimalFormat("0.00");
        double avgThroughput = (double)total.size() / seconds;

        List<Metric> falseList = total.stream().filter(metric -> StringUtils.equals("false", metric.getSuccess())).collect(Collectors.toList());
        double errors = (double)falseList.size() / total.size() * 100;

        testOverview.setMaxUsers(String.valueOf(max));
        testOverview.setAvgThroughput(df.format(avgThroughput));
        testOverview.setErrors(df.format(errors));
        double avg = (double)totalElapsed / total.size() / 1000; // s
        testOverview.setAvgResponseTime(df.format(avg));
//        testOverview.setResponseTime90();
//        testOverview.setAvgBandwidth();
        return testOverview;
    }

}