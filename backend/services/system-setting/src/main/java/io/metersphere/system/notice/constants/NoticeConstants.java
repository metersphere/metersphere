package io.metersphere.system.notice.constants;

public interface NoticeConstants {

    interface TaskType {
        String JENKINS_TASK = "JENKINS_TASK";
        String TEST_PLAN_TASK = "TEST_PLAN_TASK";
        String CASE_REVIEW_TASK = "CASE_REVIEW_TASK";
        String FUNCTIONAL_CASE_TASK = "FUNCTIONAL_CASE_TASK";
        String BUG_TASK = "BUG_TASK";
        String TRACK_HOME_TASK = "TRACK_HOME_TASK";
        String TRACK_REPORT_TASK = "TRACK_REPORT_TASK";
        String DEFECT_TASK = "DEFECT_TASK";
        String SWAGGER_TASK = "SWAGGER_TASK";
        String API_SCENARIO_TASK = "API_SCENARIO_TASK";
        String API_DEFINITION_TASK = "API_DEFINITION_TASK";
        String API_SCHEDULE_TASK = "API_SCHEDULE_TASK";
        String API_HOME_TASK = "API_HOME_TASK";
        String API_REPORT_TASK = "API_REPORT_TASK";
        String LOAD_REPORT_TASK = "LOAD_REPORT_TASK";
        String LOAD_TEST_TASK = "LOAD_TEST_TASK";
        String UI_SCENARIO_TASK = "UI_SCENARIO_TASK";
        String UI_DEFINITION_TASK = "UI_DEFINITION_TASK";
        String UI_HOME_TASK = "UI_HOME_TASK";
        String UI_REPORT_TASK = "UI_REPORT_TASK";
        String ENV_TASK = "ENV_TASK";
    }

    interface Mode {
        String API = "API";
        String SCHEDULE = "SCHEDULE";
    }

    interface Type {
        String MAIL = "MAIL";
        String IN_SITE = "IN_SITE";
        String DING_CUSTOM_ROBOT = "DING_CUSTOM_ROBOT";
        String DING_ENTERPRISE_ROBOT = "DING_ENTERPRISE_ROBOT";
        String WECOM_ROBOT = "WECOM_ROBOT";
        String LARK_ROBOT = "LARK_ROBOT";
        String CUSTOM_WEBHOOK_ROBOT = "CUSTOM_WEBHOOK_ROBOT";
    }

    interface Event {
        String EXECUTE_SUCCESSFUL = "EXECUTE_SUCCESSFUL";
        String EXECUTE_FAILED = "EXECUTE_FAILED";
        String EXECUTE_COMPLETED = "EXECUTE_COMPLETED";
        String CREATE = "CREATE";
        String UPDATE = "UPDATE";
        String DELETE = "DELETE";
        String COMPLETE = "COMPLETE";
        String REVIEW = "REVIEW";

        String CASE_CREATE = "CASE_CREATE";
        String CASE_UPDATE = "CASE_UPDATE";
        String CASE_DELETE = "CASE_DELETE";

        String MOCK_CREATE = "MOCK_CREATE";
        String MOCK_UPDATE = "MOCK_UPDATE";
        String MOCK_DELETE = "MOCK_DELETE";

        String COMMENT = "COMMENT";
        String IMPORT = "IMPORT";

        String CLOSE_SCHEDULE = "CLOSE_SCHEDULE";
    }

    interface RelatedUser {
        String CREATE_USER = "CREATE_USER";//创建人
        String EXECUTOR = "EXECUTOR";//负责人(评审人）
        String MAINTAINER = "MAINTAINER";//维护人
        String FOLLOW_PEOPLE = "FOLLOW_PEOPLE";//关注人
        String PROCESSOR = "PROCESSOR";//处理人
    }
}
