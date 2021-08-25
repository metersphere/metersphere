package io.metersphere.commons.constants;

public interface NoticeConstants {

    interface TaskType {
        String JENKINS_TASK = "JENKINS_TASK";
        String TEST_PLAN_TASK = "TEST_PLAN_TASK";
        String REVIEW_TASK = "REVIEW_TASK";
        String DEFECT_TASK = "DEFECT_TASK";
        String SWAGGER_TASK = "SWAGGER_TASK";
        String API_AUTOMATION_TASK = "API_AUTOMATION_TASK";
        String API_DEFINITION_TASK = "API_DEFINITION_TASK";
        String API_HOME_TASK = "API_HOME_TASK";
        String API_REPORT_TASK = "API_REPORT_TASK";
        String PERFORMANCE_REPORT_TASK = "PERFORMANCE_REPORT_TASK";
        String PERFORMANCE_TEST_TASK = "PERFORMANCE_TEST_TASK";
        String TRACK_TEST_CASE_TASK = "TRACK_TEST_CASE_TASK";
        String TRACK_HOME_TASK = "TRACK_HOME_TASK";
        String TRACK_REPORT_TASK = "TRACK_REPORT_TASK";
    }

    interface Mode {
        String API = "API";
        String SCHEDULE = "SCHEDULE";
    }

    interface Type {
        String EMAIL = "EMAIL";
        String NAIL_ROBOT = "NAIL_ROBOT";
        String WECHAT_ROBOT = "WECHAT_ROBOT";
        String LARK = "LARK";
        String IN_SITE = "IN_SITE";
    }

    interface Event {
        String EXECUTE_SUCCESSFUL = "EXECUTE_SUCCESSFUL";
        String EXECUTE_FAILED = "EXECUTE_FAILED";
        String EXECUTE_COMPLETED = "EXECUTE_COMPLETED";
        String CREATE = "CREATE";
        String UPDATE = "UPDATE";
        String DELETE = "DELETE";
        String COMPLETE = "COMPLETE";

        String CASE_CREATE = "CASE_CREATE";
        String CASE_UPDATE = "CASE_UPDATE";
        String CASE_DELETE = "CASE_DELETE";

        String COMMENT = "COMMENT";
        String IMPORT = "IMPORT";

        String CLOSE_SCHEDULE = "CLOSE_SCHEDULE";
    }

    interface RelatedUser {
        String CREATOR = "CREATOR";//创建人
        String EXECUTOR = "EXECUTOR";//负责人(评审人）
        String MAINTAINER = "MAINTAINER";//维护人
        String FOLLOW_PEOPLE = "FOLLOW_PEOPLE";//关注人
        String PROCESSOR = "PROCESSOR";//处理人
    }
}
