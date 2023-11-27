package io.metersphere.functional.service;

import java.util.List;

public interface FunctionalCaseHistoryService {
    void saveHistoryLog(List<String> caseIds, String type, String userId);
}
