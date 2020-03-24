package io.metersphere;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.MappingStrategy;
import org.junit.Test;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GenerateGraphTest {


    @Test
    public void test1() {
        File csvFile = new File("/Users/liuruibin/Desktop/0316.jtl");
        HeaderColumnNameMappingStrategy<Metric> ms = new HeaderColumnNameMappingStrategy<>();
        ms.setType(Metric.class);
        List<Metric> metrics = beanBuilderExample(csvFile.toPath(), ms);
        metrics.forEach(c -> {
            System.out.println(c.getTimestamp());
        });
    }

    public static List<Metric> beanBuilderExample(Path path, MappingStrategy<Metric> ms) {
        try (Reader reader = Files.newBufferedReader(path)) {

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
}
