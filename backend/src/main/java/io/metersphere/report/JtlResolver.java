package io.metersphere.report;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.metersphere.report.base.Metric;
import io.metersphere.report.base.RequestStatistics;
import io.metersphere.report.base.RequestStatisticsDTO;

import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
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
            Long l = Long.valueOf(timestampList.get(index-1)) - Long.valueOf(timestampList.get(0));

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
            // todo Avg Bandwidth(KBytes/s)
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

    public static RequestStatisticsDTO getRequestStatistics(String jtlString) {
        List<Metric> totalLines = resolver(jtlString);
        Map<String, List<Metric>> map = totalLines.stream().collect(Collectors.groupingBy(Metric::getLabel));
        return getOneRpsResult(map);
    }

}