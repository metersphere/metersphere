package io.metersphere.performance.base;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class GranularityData {

    public static List<Data> dataList = new ArrayList<>();

    static {
        dataList.add(new Data(0, 100, 1_000));
        dataList.add(new Data(101, 500, 5_000));
        dataList.add(new Data(501, 1000, 10_000));
        dataList.add(new Data(1001, 3000, 30_000));
        dataList.add(new Data(3001, 6000, 60_000));
        dataList.add(new Data(6001, 30000, 300_000));
        dataList.add(new Data(30001, 60000, 600_000));
        dataList.add(new Data(60001, 180000, 1800_000));
        dataList.add(new Data(180001, 360000, 3600_000));
        dataList.add(new Data(360000, Integer.MAX_VALUE, 3600_000));
    }

    @Getter
    @Setter
    public static class Data {
        private Integer start;
        private Integer end;
        private Integer granularity;

        Data(Integer start, Integer end, Integer granularity) {
            this.start = start;
            this.end = end;
            this.granularity = granularity;
        }
    }
}
