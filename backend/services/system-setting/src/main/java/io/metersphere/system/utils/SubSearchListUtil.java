package io.metersphere.system.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SubSearchListUtil {

    public static int DEFAULT_BATCH_SIZE = 50;

    public static <T> List<T> doForSearchList(Function<Object, List<T>> selectListFunc){
        // 创建一个 List 来保存合并后的结果
        List<T> allResults = new ArrayList<>();
        int pageNumber = 1;
        boolean count = true;
        Page<Object> page = PageHelper.startPage(pageNumber, DEFAULT_BATCH_SIZE, count);
        Pager<List<T>> listPager = PageUtils.setPageInfo(page, selectListFunc.apply(pageNumber));
        long total = listPager.getTotal();
        allResults.addAll(listPager.getList());
        count = false;
        for (int i = 1; i < ((int)Math.ceil((double) total/DEFAULT_BATCH_SIZE)); i ++) {
            Page<Object> pageCycle = PageHelper.startPage(i+1, DEFAULT_BATCH_SIZE, count);
            Pager<List<T>> listPagerCycle = PageUtils.setPageInfo(pageCycle, selectListFunc.apply(i+1));
            List<T> pageResults = listPagerCycle.getList();
            allResults.addAll(pageResults);
        }
        return allResults;
    }

    public static <T, V>  List<T>  doForSearchByCountList(List<V> totalList,  Function<Object, List<T>> selectInListFunc){
        if (CollectionUtils.isEmpty(totalList)) {
            return new ArrayList<>();
        }
        // 创建一个 List 来保存合并后的结果
        List<T> allResults = new ArrayList<>();
        int size = totalList.size();

        for (int start = 0; start < size; start += DEFAULT_BATCH_SIZE) {
            int end = Math.min(size, start + DEFAULT_BATCH_SIZE);
            List<V> subList = totalList.subList(start, end);
            List<T> results = selectInListFunc.apply(new ArrayList<>(subList));
            allResults.addAll(results);
        }

        return allResults;
    }

}
