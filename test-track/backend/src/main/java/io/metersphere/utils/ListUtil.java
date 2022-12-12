package io.metersphere.utils;

import java.util.Arrays;
import java.util.List;

public class ListUtil {

    public static boolean equalsList(List<String> list1, List<String> list2){
        // null情况
        if ( (list1 == null && list2 != null)
                || (list1 != null && list2 == null) ) {
            return false;
        }
        if (list1.size() != list2.size()) {
            return false;
        }
        String[] arr1 = list1.toArray(new String[]{});
        String[] arr2 = list2.toArray(new String[]{});
        Arrays.sort(arr1);
        Arrays.sort(arr1);
        return Arrays.equals(arr1,arr2);
    }
}
