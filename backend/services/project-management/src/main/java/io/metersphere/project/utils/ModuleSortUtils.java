package io.metersphere.project.utils;

import java.util.HashMap;
import java.util.Map;

public class ModuleSortUtils {

    public static Map<String, Integer> hashMap = new HashMap<>(7);

    public static Map<String, Integer> getHashMap() {
        hashMap.put("workstation", 1);
        hashMap.put("testPlan", 2);
        hashMap.put("bugManagement", 3);
        hashMap.put("caseManagement", 4);
        hashMap.put("apiTest", 5);
        hashMap.put("uiTest", 6);
        hashMap.put("loadTest", 7);
        return hashMap;
    }


}
