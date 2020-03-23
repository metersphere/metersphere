package io.metersphere.report;

import com.alibaba.fastjson.JSONObject;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.metersphere.report.base.Metric;
import io.metersphere.report.base.RequestStatistics;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class JtlResolver {

    private List<Metric> resolver(String jtlString) {
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

    private List<RequestStatistics> getOneRpsResult(Map<String, List<Metric>> map){
        List<RequestStatistics> requestStatisticsList = new ArrayList<>();
        Iterator<Map.Entry<String, List<Metric>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Metric>> entry = iterator.next();
            String label = entry.getKey();
            List<Metric> list = entry.getValue();
            List<String> timestampList = list.stream().map(Metric::getTimestamp).collect(Collectors.toList());
            int index=0;
            //总的响应时间
            int sumElapsed=0;
            Integer failSize = 0;
            Integer totalBytes = 0;
            List<Integer> elapsedList = new ArrayList<Integer>();

            for (int i = 0; i < list.size(); i++) {
                try {
                    Metric row = list.get(i);
                    //响应时间
                    String elapsed = row.getElapsed();
                    sumElapsed += Integer.valueOf(elapsed);
                    elapsedList.add(Integer.valueOf(elapsed));
                    //成功与否
                    String success = row.getSuccess();
                    if (!"true".equals(success)){
                        failSize++;
                    }
                    //字节
                    String bytes = row.getBytes();
                    totalBytes += Integer.valueOf(bytes);

                    index++;
                }catch (Exception e){
                    System.out.println("exception i:"+i);
                }
            }

            Collections.sort(elapsedList, new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
                    return o1-o2;
                }
            });

            Integer tp90 = elapsedList.size()*9/10;
            Integer tp95 = elapsedList.size()*95/100;
            Integer tp99 = elapsedList.size()*99/100;

            Long l = Long.valueOf(timestampList.get(index-1)) - Long.valueOf(timestampList.get(0));

            RequestStatistics requestStatistics = new RequestStatistics();
            requestStatistics.setRequestLabel(label);
            requestStatistics.setSamples(index+"");
            requestStatistics.setAverage(sumElapsed/index+"");
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
            requestStatistics.setKbPerSec(String.format("%.2f",totalBytes*1.0/1024/(l*1.0/1000)));
            requestStatisticsList.add(requestStatistics);
        }
        return requestStatisticsList;
    }

    public List<RequestStatistics> getRequestStatistics(String jtlString) {
        List<Metric> totalLines = resolver(jtlString);
        Map<String, List<Metric>> map = totalLines.stream().collect(Collectors.groupingBy(Metric::getLabel));
        return getOneRpsResult(map);
    }

}