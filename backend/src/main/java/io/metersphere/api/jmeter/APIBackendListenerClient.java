package io.metersphere.api.jmeter;


import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.dto.automation.ApiTestReportVariable;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.service.*;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.request.testcase.TrackCount;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanReportService;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.springframework.http.HttpMethod;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JMeter BackendListener扩展, jmx脚本中使用
 */
public class APIBackendListenerClient extends AbstractBackendListenerClient implements Serializable {


    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        APIBackendListenerResultHandler apiBackendListenerResultHandler =
                CommonBeanFactory.getBean(APIBackendListenerResultHandler.class);
        apiBackendListenerResultHandler.handleSetupTest(context);
        super.setupTest(context);
    }


    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        APIBackendListenerResultHandler apiBackendListenerResultHandler =
                CommonBeanFactory.getBean(APIBackendListenerResultHandler.class);
        apiBackendListenerResultHandler.handleSampleResults(sampleResults, context);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        APIBackendListenerResultHandler apiBackendListenerResultHandler =
                CommonBeanFactory.getBean(APIBackendListenerResultHandler.class);
        apiBackendListenerResultHandler.handleTeardownTest(context);
        super.teardownTest(context);
    }

}
