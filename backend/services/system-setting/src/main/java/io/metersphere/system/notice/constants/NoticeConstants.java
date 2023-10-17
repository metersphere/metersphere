package io.metersphere.system.notice.constants;

import io.swagger.v3.oas.annotations.media.Schema;

public interface NoticeConstants {

    interface TaskType {
        //测试计划模块的任务
        @Schema(description = "message.test_plan_task")
        String TEST_PLAN_TASK = "TEST_PLAN_TASK";
        @Schema(description = "message.report_task")
        String TEST_PLAN_REPORT_TASK = "TEST_PLAN_REPORT_TASK";

        //缺陷管理模块的任务
        @Schema(description = "message.bug_task")
        String BUG_TASK = "BUG_TASK";
        @Schema(description = "message.bug_sync_task")
        String BUG_SYNC_TASK = "BUG_SYNC_TASK";


        //用例管理模块的任务
        @Schema(description = "message.functional_case_task")
        String FUNCTIONAL_CASE_TASK = "FUNCTIONAL_CASE_TASK";
        @Schema(description = "message.case_review_task")
        String CASE_REVIEW_TASK = "CASE_REVIEW_TASK";

        //接口测试模块的任务
        @Schema(description = "message.api_definition_task")
        String API_DEFINITION_TASK = "API_DEFINITION_TASK";
        @Schema(description = "message.api_scenario_task")
        String API_SCENARIO_TASK = "API_SCENARIO_TASK";
        @Schema(description = "message.report_task")
        String API_REPORT_TASK = "API_REPORT_TASK";

        //UI测试模块的任务
        @Schema(description = "message.ui_scenario_task")
        String UI_SCENARIO_TASK = "UI_SCENARIO_TASK";
        @Schema(description = "message.report_task")
        String UI_REPORT_TASK = "UI_REPORT_TASK";

        //性能测试模块的任务
        @Schema(description = "message.load_test_task")
        String LOAD_TEST_TASK = "LOAD_TEST_TASK";
        @Schema(description = "message.report_task")
        String LOAD_REPORT_TASK = "LOAD_REPORT_TASK";

        //jenkins任务
        @Schema(description = "message.jenkins_task")
        String JENKINS_TASK = "JENKINS_TASK";

        //定时任务
        @Schema(description = "message.schedule_task")
        String SCHEDULE_TASK = "SCHEDULE_TASK";

    }

    interface TriggerMode {
        //定时任务
        @Schema(description = "message.schedule_task")
        String SCHEDULE_TASK = "SCHEDULE_TASK";
        //批量执行
        @Schema(description = "message.batch_execution")
        String BATCH_EXECUTION = "BATCH_EXECUTION";
        //手动执行
        @Schema(description = "message.manual_execution")
        String MANUAL_EXECUTION = "MANUAL_EXECUTION";
        //测试计划
        @Schema(description = "message.test_plan_task")
        String TEST_PLAN_TASK = "TEST_PLAN_TASK";
        //jenkins任务
        @Schema(description = "message.jenkins_task")
        String JENKINS_TASK = "JENKINS_TASK";
    }


    interface Mode {
        String SCHEDULE = "SCHEDULE";
    }

    interface Module {
        @Schema(description = "message.test_plan_management")
        String TEST_PLAN_MANAGEMENT = "TEST_PLAN_MANAGEMENT";

        @Schema(description = "message.bug_management")
        String BUG_MANAGEMENT = "BUG_MANAGEMENT";

        @Schema(description = "message.case_management")
        String CASE_MANAGEMENT = "CASE_MANAGEMENT";

        @Schema(description = "message.api_test_management")
        String API_TEST_MANAGEMENT = "API_TEST_MANAGEMENT";

        @Schema(description = "message.ui_test_management")
        String UI_TEST_MANAGEMENT = "UI_TEST_MANAGEMENT";

        @Schema(description = "message.load_test_management")
        String LOAD_TEST_MANAGEMENT = "LOAD_TEST_MANAGEMENT";

        @Schema(description = "message.jenkins_task_management")
        String JENKINS_TASK_MANAGEMENT = "JENKINS_TASK_MANAGEMENT";

        @Schema(description = "message.schedule_task_management")
        String SCHEDULE_TASK_MANAGEMENT = "SCHEDULE_TASK_MANAGEMENT";
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
        @Schema(description = "message.create")
        String CREATE = "CREATE";

        @Schema(description = "message.update")
        String UPDATE = "UPDATE";

        @Schema(description = "message.delete")
        String DELETE = "DELETE";

        @Schema(description = "message.execute_successful")
        String EXECUTE_SUCCESSFUL = "EXECUTE_SUCCESSFUL";

        @Schema(description = "message.execute_failed")
        String EXECUTE_FAILED = "EXECUTE_FAILED";

        @Schema(description = "message.execute_completed")
        String EXECUTE_COMPLETED = "EXECUTE_COMPLETED";

        @Schema(description = "message.comment")
        String COMMENT = "COMMENT";

        @Schema(description = "message.at")
        String AT = "AT";

        @Schema(description = "message.replay")
        String REPLAY = "REPLAY";

        @Schema(description = "message.review_passed")
        String REVIEW_PASSED = "REVIEW_PASSED";

        @Schema(description = "message.review_fail")
        String REVIEW_FAIL = "REVIEW_FAIL";

        @Schema(description = "message.review_at")
        String REVIEW_AT = "REVIEW_AT";

        @Schema(description = "message.execute_passed")
        String EXECUTE_PASSED = "EXECUTE_PASSED";

        @Schema(description = "message.execute_fail")
        String EXECUTE_FAIL = "EXECUTE_FAIL";

        @Schema(description = "message.execute_at")
        String EXECUTE_AT = "EXECUTE_AT";

        @Schema(description = "message.review_completed")
        String REVIEW_COMPLETED = "REVIEW_COMPLETED";

        @Schema(description = "message.case_create")
        String CASE_CREATE = "CASE_CREATE";

        @Schema(description = "message.case_update")
        String CASE_UPDATE = "CASE_UPDATE";

        @Schema(description = "message.case_delete")
        String CASE_DELETE = "CASE_DELETE";

        @Schema(description = "message.case_execute_successful")
        String CASE_EXECUTE_SUCCESSFUL = "CASE_EXECUTE_SUCCESSFUL";

        @Schema(description = "message.case_execute_fake_error")
        String CASE_EXECUTE_FAKE_ERROR = "CASE_EXECUTE_FAKE_ERROR";

        @Schema(description = "message.case_execute_failed")
        String CASE_EXECUTE_FAILED = "CASE_EXECUTE_FAILED";

        @Schema(description = "message.scenario_execute_successful")
        String SCENARIO_EXECUTE_SUCCESSFUL = "SCENARIO_EXECUTE_SUCCESSFUL";

        @Schema(description = "message.scenario_execute_fake_error")
        String SCENARIO_EXECUTE_FAKE_ERROR = "SCENARIO_EXECUTE_FAKE_ERROR";

        @Schema(description = "message.scenario_execute_failed")
        String SCENARIO_EXECUTE_FAILED = "SCENARIO_EXECUTE_FAILED";

        @Schema(description = "message.open")
        String OPEN = "OPEN";

        @Schema(description = "message.close")
        String CLOSE = "CLOSE";
    }

    interface RelatedUser {
        @Schema(description = "message.create_user")
        //TODO:// 和实体类创建人应该保持一致，还需再改
        String CREATE_USER = "CREATE_USER";//创建人
        @Schema(description = "message.follow_people")
        String FOLLOW_PEOPLE = "FOLLOW_PEOPLE";//关注人
        @Schema(description = "message.operator")
        String OPERATOR = "OPERATOR"; //操作人
    }

    interface FieldSource {
        @Schema(description = "message.custom_field")
        String CUSTOM_FIELD = "CUSTOM_FIELD";//自定义字段
        @Schema(description = "message.case_field")
        String CASE_FIELD = "CASE_FIELD";//用例字段
        @Schema(description = "message.report_field")
        String REPORT_FIELD = "REPORT_FIELD"; //报告字段
    }


    interface TemplateText {
        @Schema(description = "message.test_plan_task_create")
        String TEST_PLAN_TASK_CREATE = "TEST_PLAN_TASK_CREATE"; // ${OPERATOR}创建了测试计划:${name}
        @Schema(description = "message.test_plan_task_update")
        String TEST_PLAN_TASK_UPDATE = "TEST_PLAN_TASK_UPDATE";// ${OPERATOR}更新了测试计划:${name}
        @Schema(description = "message.test_plan_task_delete")
        String TEST_PLAN_TASK_DELETE = "TEST_PLAN_TASK_DELETE";//${OPERATOR}删除了测试计划:${name}
        @Schema(description = "message.test_plan_task_execute")
        String TEST_PLAN_TASK_EXECUTE_SUCCESSFUL = "TEST_PLAN_TASK_EXECUTE_SUCCESSFUL";//${OPERATOR}执行了测试计划:${name}
        @Schema(description = "message.test_plan_task_execute")
        String TEST_PLAN_TASK_EXECUTE_FAILED = "TEST_PLAN_TASK_EXECUTE_FAILED";//${OPERATOR}通过定时任务执行了测试计划:${name}
        @Schema(description = "message.test_plan_report_task_delete")
        String TEST_PLAN_REPORT_TASK_DELETE = "TEST_PLAN_REPORT_TASK_DELETE";//${OPERATOR}删除了测试计划报告:${name}

        @Schema(description = "message.bug_task_create")
        String BUG_TASK_CREATE = "BUG_TASK_CREATE"; // ${OPERATOR}创建了缺陷:${title}
        @Schema(description = "message.bug_task_update")
        String BUG_TASK_UPDATE = "BUG_TASK_UPDATE";//${OPERATOR}更新了缺陷:${title}
        @Schema(description = "message.bug_task_delete")
        String BUG_TASK_DELETE = "BUG_TASK_DELETE";//${OPERATOR}删除了缺陷:${title}
        @Schema(description = "message.bug_task_comment")
        String BUG_TASK_COMMENT = "BUG_TASK_COMMENT";//${OPERATOR}评论了你的缺陷:${title}
        @Schema(description = "message.bug_task_at_comment")
        String BUG_TASK_AT = "BUG_TASK_AT";//${OPERATOR}评论了缺陷:${title} 并@了你
        @Schema(description = "message.bug_task_reply_comment")
        String BUG_TASK_REPLY = "BUG_TASK_REPLY";//${OPERATOR}在缺陷${title} 回复了你的评论
        @Schema(description = "message.bug_sync_task_execute_completed")
        String BUG_SYNC_TASK_EXECUTE_COMPLETED = "BUG_SYNC_TASK_EXECUTE_COMPLETED";//${OPERATOR}同步了${total}条缺陷

        @Schema(description = "message.functional_case_task_create")
        String FUNCTIONAL_CASE_TASK_CREATE = "FUNCTIONAL_CASE_TASK_CREATE"; // ${OPERATOR}创建了功能用例:${name}
        @Schema(description = "message.functional_case_task_update")
        String FUNCTIONAL_CASE_TASK_UPDATE = "FUNCTIONAL_CASE_TASK_UPDATE";//${OPERATOR}更新了功能用例:${name}
        @Schema(description = "message.functional_case_task_delete")
        String FUNCTIONAL_CASE_TASK_DELETE = "FUNCTIONAL_CASE_TASK_DELETE";//${OPERATOR}删除了功能用例:${name}
        @Schema(description = "message.functional_case_task_comment")
        String FUNCTIONAL_CASE_TASK_COMMENT = "FUNCTIONAL_CASE_TASK_COMMENT";//${OPERATOR}评论了你的功能用例:${name}
        @Schema(description = "message.functional_case_task_review")
        String FUNCTIONAL_CASE_TASK_REVIEW_PASSED = "FUNCTIONAL_CASE_TASK_REVIEW_PASSED";//${OPERATOR}评审了${reviewName}${name}
        @Schema(description = "message.functional_case_task_review")
        String FUNCTIONAL_CASE_TASK_REVIEW_FAIL = "FUNCTIONAL_CASE_TASK_REVIEW_FAIL";//${OPERATOR}评审了${reviewName}${name}
        @Schema(description = "message.functional_case_task_review_at")
        String FUNCTIONAL_CASE_TASK_REVIEW_AT = "FUNCTIONAL_CASE_TASK_REVIEW_AT";//${OPERATOR}在${reviewName}${name} @了你
        @Schema(description = "message.functional_case_task_plan")
        String FUNCTIONAL_CASE_TASK_EXECUTE_PASSED = "FUNCTIONAL_CASE_TASK_EXECUTE_PASSED";//${OPERATOR}评审了${testPlanName}${name}
        @Schema(description = "message.functional_case_task_plan")
        String FUNCTIONAL_CASE_TASK_EXECUTE_FAIL = "FUNCTIONAL_CASE_TASK_EXECUTE_FAIL";//${OPERATOR}评审了${testPlanName}${name}
        @Schema(description = "message.functional_case_task_plan_at")
        String FUNCTIONAL_CASE_TASK_EXECUTE_AT = "FUNCTIONAL_CASE_TASK_EXECUTE_AT";//${OPERATOR}在${testPlanName}${name}@了你
        @Schema(description = "message.functional_case_task_at_comment")
        String FUNCTIONAL_CASE_TASK_AT = "FUNCTIONAL_CASE_TASK_AT";//${OPERATOR}评论了功能用例:${name} 并@了你
        @Schema(description = "message.functional_case_task_reply_comment")
        String FUNCTIONAL_CASE_TASK_REPLY = "FUNCTIONAL_CASE_TASK_REPLY";//${OPERATOR}在用例${name} 回复了你的评论

        @Schema(description = "message.case_review_task_create")
        String CASE_REVIEW_TASK_CREATE = "CASE_REVIEW_TASK_CREATE"; // ${OPERATOR}创建了用例评审:${name}
        @Schema(description = "message.case_review_task_update")
        String CASE_REVIEW_TASK_UPDATE = "CASE_REVIEW_TASK_UPDATE";//${OPERATOR}更新了用例评审:${name}
        @Schema(description = "message.case_review_task_delete")
        String CASE_REVIEW_TASK_DELETE = "CASE_REVIEW_TASK_DELETE";//${OPERATOR}删除了用例评审:${name}
        @Schema(description = "message.case_review_task_review_completed")
        String CASE_REVIEW_TASK_REVIEW_COMPLETED = "CASE_REVIEW_TASK_REVIEW_COMPLETED";//${OPERATOR}完成了用例评审:${name}


        @Schema(description = "message.api_definition_task_create")
        String API_DEFINITION_TASK_CREATE = "API_DEFINITION_TASK_CREATE";//${OPERATOR}创建了接口文档:${name}
        @Schema(description = "message.api_definition_task_update")
        String API_DEFINITION_TASK_UPDATE = "API_DEFINITION_TASK_UPDATE";//${OPERATOR}更新了接口文档:${name}
        @Schema(description = "message.api_definition_task_delete")
        String API_DEFINITION_TASK_DELETE = "API_DEFINITION_TASK_DELETE";//${OPERATOR}删除了接口文档:${name}
        @Schema(description = "message.api_definition_task_case_create")
        String API_DEFINITION_TASK_CASE_CREATE = "API_DEFINITION_TASK_CASE_CREATE";//${OPERATOR}创建了接口用例:${name}
        @Schema(description = "message.api_definition_task_case_update")
        String API_DEFINITION_TASK_CASE_UPDATE = "API_DEFINITION_TASK_CASE_UPDATE";//${OPERATOR}更新了接口用例:${name}
        @Schema(description = "message.api_definition_task_case_delete")
        String API_DEFINITION_TASK_CASE_DELETE = "API_DEFINITION_TASK_CASE_DELETE";//${OPERATOR}删除了接口用例:${name}
        @Schema(description = "message.api_definition_task_case_execute")
        String API_DEFINITION_TASK_CASE_EXECUTE_SUCCESSFUL = "API_DEFINITION_TASK_CASE_EXECUTE_SUCCESSFUL";//${OPERATOR}执行了接口用例:${name}
        @Schema(description = "message.api_definition_task_case_execute")
        String API_DEFINITION_TASK_CASE_EXECUTE_FAKE_ERROR = "API_DEFINITION_TASK_CASE_EXECUTE_FAKE_ERROR";//${OPERATOR}执行了接口用例:${name}
        @Schema(description = "message.api_definition_task_case_execute")
        String API_DEFINITION_TASK_CASE_EXECUTE_FAILED = "API_DEFINITION_TASK_CASE_EXECUTE_FAILED";//${OPERATOR}执行了接口用例:${name}

        @Schema(description = "message.api_scenario_task_create")
        String API_SCENARIO_TASK_CREATE = "API_SCENARIO_TASK_CREATE";//${OPERATOR}创建了接口场景:${name}
        @Schema(description = "message.api_scenario_task_update")
        String API_SCENARIO_TASK_UPDATE = "API_SCENARIO_TASK_UPDATE";//${OPERATOR}更新了接口场景:${name}
        @Schema(description = "message.api_scenario_task_delete")
        String API_SCENARIO_TASK_DELETE = "API_SCENARIO_TASK_DELETE";//${OPERATOR}删除了接口场景:${name}
        @Schema(description = "message.api_scenario_task_scenario_execute")
        String API_SCENARIO_TASK_SCENARIO_EXECUTE_SUCCESSFUL = "API_SCENARIO_TASK_SCENARIO_EXECUTE_SUCCESSFUL";//${OPERATOR}执行了接口场景:${name}
        @Schema(description = "message.api_scenario_task_scenario_execute")
        String API_SCENARIO_TASK_SCENARIO_EXECUTE_FAKE_ERROR = "API_SCENARIO_TASK_SCENARIO_EXECUTE_FAKE_ERROR";//${OPERATOR}执行了接口场景:${name}
        @Schema(description = "message.api_scenario_task_scenario_execute")
        String API_SCENARIO_TASK_SCENARIO_EXECUTE_FAILED = "API_SCENARIO_TASK_SCENARIO_EXECUTE_FAILED";//${OPERATOR}执行了接口场景:${name}
        @Schema(description = "message.api_report_task_delete")
        String API_REPORT_TASK_DELETE = "API_REPORT_TASK_DELETE";//${OPERATOR}删除了接口报告:${name}

        @Schema(description = "message.ui_scenario_task_create")
        String UI_SCENARIO_TASK_CREATE = "UI_SCENARIO_TASK_CREATE";//${OPERATOR}创建了UI用例:${name}
        @Schema(description = "message.ui_scenario_task_update")
        String UI_SCENARIO_TASK_UPDATE = "UI_SCENARIO_TASK_UPDATE";//${OPERATOR}更新了UI用例:${name}
        @Schema(description = "message.ui_scenario_task_delete")
        String UI_SCENARIO_TASK_DELETE = "UI_SCENARIO_TASK_DELETE";//${OPERATOR}删除了UI用例:${name}
        @Schema(description = "message.ui_scenario_task_execute")
        String UI_SCENARIO_TASK_EXECUTE_SUCCESSFUL = "UI_SCENARIO_TASK_EXECUTE_SUCCESSFUL";//${OPERATOR}执行了UI用例:${name}
        @Schema(description = "message.ui_scenario_task_execute")
        String UI_SCENARIO_TASK_EXECUTE_FAILED = "UI_SCENARIO_TASK_EXECUTE_FAILED";//${OPERATOR}执行了UI用例:${name}
        @Schema(description = "message.ui_report_task_delete")
        String UI_REPORT_TASK_DELETE = "UI_REPORT_TASK_DELETE";//${OPERATOR}删除了UI报告:${name}

        @Schema(description = "message.load_test_task_create")
        String LOAD_TEST_TASK_CREATE = "LOAD_TEST_TASK_CREATE";//${OPERATOR}创建了性能用例:${name}
        @Schema(description = "message.load_test_task_update")
        String LOAD_TEST_TASK_UPDATE = "LOAD_TEST_TASK_UPDATE";//${OPERATOR}更新了性能用例:${name}
        @Schema(description = "message.load_test_task_delete")
        String LOAD_TEST_TASK_DELETE = "LOAD_TEST_TASK_DELETE";//${OPERATOR}删除了性能用例:${name}
        @Schema(description = "message.load_test_task_execute_completed")
        String LOAD_TEST_TASK_EXECUTE_COMPLETED = "LOAD_TEST_TASK_EXECUTE_COMPLETED";//${OPERATOR}执行了性能用例:${name}
        @Schema(description = "message.load_report_task_delete")
        String LOAD_REPORT_TASK_DELETE = "LOAD_REPORT_TASK_DELETE";//${OPERATOR}删除了性能报告:${name}

        @Schema(description = "message.jenkins_task_execute")
        String JENKINS_TASK_EXECUTE_SUCCESSFUL = "JENKINS_TASK_EXECUTE_SUCCESSFUL";//Jenkins执行了接口场景:${name}
        @Schema(description = "message.jenkins_task_execute")
        String JENKINS_TASK_EXECUTE_FAILED = "JENKINS_TASK_EXECUTE_FAILED";//Jenkins执行了接口场景:${name}

        @Schema(description = "message.schedule_task_open")
        String SCHEDULE_TASK_OPEN = "SCHEDULE_TASK_OPEN";//Jenkins执行了接口场景:${name}
        @Schema(description = "message.schedule_task_close")
        String SCHEDULE_TASK_CLOSE = "SCHEDULE_TASK_CLOSE";//Jenkins执行了接口场景:${name}

    }

    interface TemplateSubject {
        @Schema(description = "message.title.test_plan_task_create")
        String TEST_PLAN_TASK_CREATE = "TEST_PLAN_TASK_CREATE"; // 测试计划创建通知
        @Schema(description = "message.title.test_plan_task_update")
        String TEST_PLAN_TASK_UPDATE = "TEST_PLAN_TASK_UPDATE";// 测试计划更新通知
        @Schema(description = "message.title.test_plan_task_delete")
        String TEST_PLAN_TASK_DELETE = "TEST_PLAN_TASK_DELETE";//测试计划删除通知
        @Schema(description = "message.title.test_plan_task_execute_success")//测试计划执行成功通知
        String TEST_PLAN_TASK_EXECUTE_SUCCESSFUL = "TEST_PLAN_TASK_EXECUTE_SUCCESSFUL";
        @Schema(description = "message.title.test_plan_task_execute_failed")//测试计划执行失败通知
        String TEST_PLAN_TASK_EXECUTE_FAILED = "TEST_PLAN_TASK_EXECUTE_FAILED";
        @Schema(description = "message.title.test_plan_report_task_delete")//测试计划报告删除通知
        String TEST_PLAN_REPORT_TASK_DELETE = "TEST_PLAN_REPORT_TASK_DELETE";

        @Schema(description = "message.title.bug_task_create") //缺陷创建通知
        String BUG_TASK_CREATE = "BUG_TASK_CREATE";
        @Schema(description = "message.title.bug_task_update")//缺陷更新通知
        String BUG_TASK_UPDATE = "BUG_TASK_UPDATE";
        @Schema(description = "message.title.bug_task_delete")//缺陷删除通知
        String BUG_TASK_DELETE = "BUG_TASK_DELETE";
        @Schema(description = "message.title.bug_task_comment")//缺陷评论通知
        String BUG_TASK_COMMENT = "BUG_TASK_COMMENT";
        @Schema(description = "message.title.bug_task_comment")//缺陷评论通知
        String BUG_TASK_AT = "BUG_TASK_AT";
        @Schema(description = "message.title.bug_task_comment")//缺陷评论通知
        String BUG_TASK_REPLY = "BUG_TASK_REPLY";
        @Schema(description = "message.title.bug_sync_task_execute_completed")//同步缺陷执行完成通知
        String BUG_SYNC_TASK_EXECUTE_COMPLETED = "BUG_SYNC_TASK_EXECUTE_COMPLETED";


        @Schema(description = "message.title.functional_case_task_create")//功能用例创建通知
        String FUNCTIONAL_CASE_TASK_CREATE = "FUNCTIONAL_CASE_TASK_CREATE";
        @Schema(description = "message.title.functional_case_task_update")//功能用例更新通知
        String FUNCTIONAL_CASE_TASK_UPDATE = "FUNCTIONAL_CASE_TASK_UPDATE";
        @Schema(description = "message.title.functional_case_task_delete")//功能用例删除通知
        String FUNCTIONAL_CASE_TASK_DELETE = "FUNCTIONAL_CASE_TASK_DELETE";
        @Schema(description = "message.title.functional_case_task_comment")//功能用例评论通知
        String FUNCTIONAL_CASE_TASK_COMMENT = "FUNCTIONAL_CASE_TASK_COMMENT";
        @Schema(description = "message.title.functional_case_task_comment")
        String FUNCTIONAL_CASE_TASK_AT = "FUNCTIONAL_CASE_TASK_AT";//功能用例评论通知
        @Schema(description = "message.title.functional_case_task_comment")
        String FUNCTIONAL_CASE_TASK_REPLY = "FUNCTIONAL_CASE_TASK_REPLY";//功能用例评论通知
        @Schema(description = "message.title.functional_case_task_review_passed")
        String FUNCTIONAL_CASE_TASK_REVIEW_PASSED = "FUNCTIONAL_CASE_TASK_REVIEW_PASSED";//用例评审通过通知
        @Schema(description = "message.title.functional_case_task_review_fail")
        String FUNCTIONAL_CASE_TASK_REVIEW_FAIL = "FUNCTIONAL_CASE_TASK_REVIEW_FAIL";//用例评审不通过通知
        @Schema(description = "message.title.functional_case_task_review_at")
        String FUNCTIONAL_CASE_TASK_REVIEW_AT = "FUNCTIONAL_CASE_TASK_REVIEW_AT";//用例评审通知
        @Schema(description = "message.title.functional_case_task_execute_passed")
        String FUNCTIONAL_CASE_TASK_EXECUTE_PASSED = "FUNCTIONAL_CASE_TASK_EXECUTE_PASSED";//用例执行通过通知
        @Schema(description = "message.title.functional_case_task_execute_fail")
        String FUNCTIONAL_CASE_TASK_EXECUTE_FAIL = "FUNCTIONAL_CASE_TASK_EXECUTE_FAIL";//用例执行不通过通知
        @Schema(description = "message.title.functional_case_task_execute_at")
        String FUNCTIONAL_CASE_TASK_EXECUTE_AT = "FUNCTIONAL_CASE_TASK_EXECUTE_AT";//用例执行通知


        @Schema(description = "message.title.case_review_task_create")//用例评审创建通知
        String CASE_REVIEW_TASK_CREATE = "CASE_REVIEW_TASK_CREATE";
        @Schema(description = "message.title.case_review_task_update")//用例评审更新通知
        String CASE_REVIEW_TASK_UPDATE = "CASE_REVIEW_TASK_UPDATE";
        @Schema(description = "message.title.case_review_task_delete")//用例评审删除通知
        String CASE_REVIEW_TASK_DELETE = "CASE_REVIEW_TASK_DELETE";
        @Schema(description = "message.title.case_review_task_review_completed")//用例评审评审完成通知
        String CASE_REVIEW_TASK_REVIEW_COMPLETED = "CASE_REVIEW_TASK_REVIEW_COMPLETED";


        @Schema(description = "message.title.api_definition_task_create")//接口文档创建通知
        String API_DEFINITION_TASK_CREATE = "API_DEFINITION_TASK_CREATE";
        @Schema(description = "message.title.api_definition_task_update")//接口文档更新通知
        String API_DEFINITION_TASK_UPDATE = "API_DEFINITION_TASK_UPDATE";
        @Schema(description = "message.title.api_definition_task_delete")//接口文档删除通知
        String API_DEFINITION_TASK_DELETE = "API_DEFINITION_TASK_DELETE";
        @Schema(description = "message.title.api_definition_task_case_create")//接口用例创建通知
        String API_DEFINITION_TASK_CASE_CREATE = "API_DEFINITION_TASK_CASE_CREATE";
        @Schema(description = "message.title.api_definition_task_case_update")//接口用例更新通知
        String API_DEFINITION_TASK_CASE_UPDATE = "API_DEFINITION_TASK_CASE_UPDATE";
        @Schema(description = "message.title.api_definition_task_case_delete")//接口用例删除通知
        String API_DEFINITION_TASK_CASE_DELETE = "API_DEFINITION_TASK_CASE_DELETE";
        @Schema(description = "message.title.api_definition_task_case_execute_successful")//接口用例执行成功通知
        String API_DEFINITION_TASK_CASE_EXECUTE_SUCCESSFUL = "API_DEFINITION_TASK_CASE_EXECUTE_SUCCESSFUL";
        @Schema(description = "message.title.api_definition_task_case_execute_fake_error")//接口用例执行误报通知
        String API_DEFINITION_TASK_CASE_EXECUTE_FAKE_ERROR = "API_DEFINITION_TASK_CASE_EXECUTE_FAKE_ERROR";
        @Schema(description = "message.title.api_definition_task_case_execute_failed")//接口用例执行失败通知
        String API_DEFINITION_TASK_CASE_EXECUTE_FAILED = "API_DEFINITION_TASK_CASE_EXECUTE_FAILED";

        @Schema(description = "message.title.api_scenario_task_create")//接口场景创建通知
        String API_SCENARIO_TASK_CREATE = "API_SCENARIO_TASK_CREATE";
        @Schema(description = "message.title.api_scenario_task_update")//接口场景更新通知
        String API_SCENARIO_TASK_UPDATE = "API_SCENARIO_TASK_UPDATE";
        @Schema(description = "message.title.api_scenario_task_delete")//接口场景删除通知
        String API_SCENARIO_TASK_DELETE = "API_SCENARIO_TASK_DELETE";
        @Schema(description = "message.title.api_scenario_task_scenario_execute_successful")//接口场景执行成功通知
        String API_SCENARIO_TASK_SCENARIO_EXECUTE_SUCCESSFUL = "API_SCENARIO_TASK_SCENARIO_EXECUTE_SUCCESSFUL";
        @Schema(description = "message.title.api_scenario_task_scenario_execute_fake_error")//接口场景执误报通知
        String API_SCENARIO_TASK_SCENARIO_EXECUTE_FAKE_ERROR = "API_SCENARIO_TASK_SCENARIO_EXECUTE_FAKE_ERROR";
        @Schema(description = "message.title.api_scenario_task_scenario_execute_failed")//接口场景执行失败通知
        String API_SCENARIO_TASK_SCENARIO_EXECUTE_FAILED = "API_SCENARIO_TASK_SCENARIO_EXECUTE_FAILED";
        @Schema(description = "message.title.api_report_task_delete")//接口报告删除通知
        String API_REPORT_TASK_DELETE = "API_REPORT_TASK_DELETE";

        @Schema(description = "message.title.ui_scenario_task_create")//UI用例创建通知
        String UI_SCENARIO_TASK_CREATE = "UI_SCENARIO_TASK_CREATE";
        @Schema(description = "message.title.ui_scenario_task_update")//UI用例更新通知
        String UI_SCENARIO_TASK_UPDATE = "UI_SCENARIO_TASK_UPDATE";
        @Schema(description = "message.title.ui_scenario_task_delete")//UI用例删除通知
        String UI_SCENARIO_TASK_DELETE = "UI_SCENARIO_TASK_DELETE";
        @Schema(description = "message.title.ui_scenario_task_execute_successful")//UI用例执行成功通知
        String UI_SCENARIO_TASK_EXECUTE_SUCCESSFUL = "UI_SCENARIO_TASK_EXECUTE_SUCCESSFUL";
        @Schema(description = "message.title.ui_scenario_task_execute_failed")//UI用例执行失败通知
        String UI_SCENARIO_TASK_EXECUTE_FAILED = "UI_SCENARIO_TASK_EXECUTE_FAILED";
        @Schema(description = "message.title.ui_report_task_delete")//UI报告删除通知
        String UI_REPORT_TASK_DELETE = "UI_REPORT_TASK_DELETE";

        @Schema(description = "message.title.load_test_task_create")//性能用例创建通知
        String LOAD_TEST_TASK_CREATE = "LOAD_TEST_TASK_CREATE";
        @Schema(description = "message.title.load_test_task_update")//性能用例更新通知
        String LOAD_TEST_TASK_UPDATE = "LOAD_TEST_TASK_UPDATE";
        @Schema(description = "message.title.load_test_task_delete")//性能用例删除通知
        String LOAD_TEST_TASK_DELETE = "LOAD_TEST_TASK_DELETE";
        @Schema(description = "message.title.load_test_task_execute_completed")//性能用例执行完成通知
        String LOAD_TEST_TASK_EXECUTE_COMPLETED = "LOAD_TEST_TASK_EXECUTE_COMPLETED";
        @Schema(description = "message.title.load_report_task_delete")//性能报告删除通知
        String LOAD_REPORT_TASK_DELETE = "LOAD_REPORT_TASK_DELETE";

        @Schema(description = "message.title.jenkins_task_execute_successful")//Jenkins任务执行成功通知
        String JENKINS_TASK_EXECUTE_SUCCESSFUL = "JENKINS_TASK_EXECUTE_SUCCESSFUL";
        @Schema(description = "message.title.jenkins_task_execute_failed")//Jenkins任务执行失败通知
        String JENKINS_TASK_EXECUTE_FAILED = "JENKINS_TASK_EXECUTE_FAILED";

        @Schema(description = "message.title.schedule_task_open")//定时任务开启通知
        String SCHEDULE_TASK_OPEN = "SCHEDULE_TASK_OPEN";
        @Schema(description = "message.title.schedule_task_close")//定时任务关闭通知
        String SCHEDULE_TASK_CLOSE = "SCHEDULE_TASK_CLOSE";

    }

    interface SensitiveField {
        String deleted = "deleted";
        String refId = "refId";
        String versionId = "versionId";
        String reportId = "reportId";
        String moduleId = "moduleId";
    }
}
