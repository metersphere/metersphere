package io.metersphere.system.utils;

import com.github.pagehelper.Page;

public class PageUtils {
    public static <T> Pager<T> setPageInfo(Page page, T list) {
        try {
            Pager<T> pager = new Pager<>();
            pager.setList(list);
            pager.setPageSize(page.getPageSize());
            pager.setCurrent(page.getPageNum());
            pager.setTotal(page.getTotal());
            return pager;
        } catch (Exception e) {
            throw new RuntimeException("Error saving current page number data！");
        }
    }
}
