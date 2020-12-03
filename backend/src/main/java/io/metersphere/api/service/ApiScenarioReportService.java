package io.metersphere.api.service;

import io.metersphere.api.jmeter.TestResult;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.security.util.Cache;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportService {

    private static Cache cache = Cache.newHardMemoryCache(0, 3600 * 24);

    public void addResult(TestResult res) {
        if (!res.getScenarios().isEmpty()) {
            cache.put(res.getTestId(), res);
        } else {
            MSException.throwException(Translator.get("test_not_found"));
        }
    }

}
