package io.metersphere.report.parse;

import io.metersphere.commons.utils.MsJMeterUtils;
import io.metersphere.report.base.ChartsData;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.dashboard.JsonizerVisitor;
import org.apache.jmeter.report.processor.*;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ResultDataParse {

    private static final String DATE_TIME_PATTERN = "yyyy/MM/dd HH:mm:ss";
    private static final String TIME_PATTERN = "HH:mm:ss";

    public static <T> List<T> summaryMapParsing(Map<String, Object> map, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (String key : map.keySet()) {
            MapResultData mapResultData = (MapResultData) map.get(key);
            ListResultData items = (ListResultData) mapResultData.getResult("items");
            if (items.getSize() > 0) {
                for (int i = 0; i < items.getSize(); i++) {
                    MapResultData resultData = (MapResultData) items.get(i);
                    ListResultData data = (ListResultData) resultData.getResult("data");
                    int size = data.getSize();
                    String[] strArray = new String[size];
                    for (int j = 0; j < size; j++) {
                        ValueResultData valueResultData = (ValueResultData) data.get(j);
                        if (valueResultData.getValue() == null) {
                            strArray[j] = "";
                        } else {
                            String accept = valueResultData.accept(new JsonizerVisitor());
                            strArray[j] = accept.replace("\\", "");
                        }
                    }
                    T t = null;
                    try {
                        t = setParam(clazz, strArray);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    list.add(t);
                }
            }
        }
        return list;
    }

    public static List<ChartsData> graphMapParsing(Map<String, Object> map, String seriesName) {
        List<ChartsData> list = new ArrayList<>();
        // ThreadGroup
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
        return list;
    }

    public static Map<String, Object> getGraphDataMap(String jtlString, AbstractOverTimeGraphConsumer timeGraphConsumer) {
        AbstractOverTimeGraphConsumer abstractOverTimeGraphConsumer = timeGraphConsumer;
        abstractOverTimeGraphConsumer.setGranularity(60000);
        abstractOverTimeGraphConsumer.initialize();
        SampleContext sampleContext = initJmeterConsumer(jtlString, abstractOverTimeGraphConsumer);
        return sampleContext.getData();
    }

    public static Map<String, Object> getSummryDataMap(String jtlString, AbstractSummaryConsumer<?> summaryConsumer) {
        AbstractSummaryConsumer<?> abstractSummaryConsumer = summaryConsumer;
        SampleContext sampleContext = initJmeterConsumer(jtlString, summaryConsumer);
        return sampleContext.getData();
    }

    private static SampleContext initJmeterConsumer(String jtlString, AbstractSampleConsumer abstractSampleConsumer) {
        int row = 0;
        // 使用反射获取properties
        MsJMeterUtils.loadJMeterProperties("jmeter.properties");
        SampleMetadata sampleMetaData = createTestMetaData();
        SampleContext sampleContext = new SampleContext();
        abstractSampleConsumer.setSampleContext(sampleContext);
        abstractSampleConsumer.startConsuming();
        StringTokenizer tokenizer = new StringTokenizer(jtlString, "\n");
        // 去掉第一行
        tokenizer.nextToken();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            String[] data = line.split(",", -1);
            Sample sample = new Sample(row++, sampleMetaData, data);
            abstractSampleConsumer.consume(sample, 0);
        }
        abstractSampleConsumer.stopConsuming();
        return sampleContext;
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

    private static String stampToDate(String pattern, String timeStamp) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(timeStamp)), ZoneId.systemDefault());
        return localDateTime.format(dateTimeFormatter);
    }

    private static String formatDate(String dateString) throws ParseException {
        SimpleDateFormat before = new SimpleDateFormat(DATE_TIME_PATTERN);
        SimpleDateFormat after = new SimpleDateFormat(TIME_PATTERN);
        return after.format(before.parse(dateString));
    }

    private static <T> T setParam(Class<T> clazz, Object[] args)
            throws Exception {
        if (clazz == null || args == null) {
            throw new IllegalArgumentException();
        }
        T t = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null || fields.length > args.length) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            fields[i].set(t, args[i]);
        }
        return t;
    }
}
