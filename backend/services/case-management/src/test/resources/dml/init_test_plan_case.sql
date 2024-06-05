INSERT INTO test_plan(id, num, project_id, group_id, module_id, name, status, type, tags, create_time, create_user, update_time, update_user, planned_start_time, planned_end_time, actual_start_time, actual_end_time, description)
VALUE ('test_plan_associate_case_gyq_three', 500, 'test_plan_associate_case_project', 'NONE', 'case_plan_module', 'test_plan_associate_case_name_three', 'PREPARED', 'TEST_PLAN', null, UNIX_TIMESTAMP() * 1000,'admin',
        UNIX_TIMESTAMP() * 1000,'admin',UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, null);


INSERT INTO test_plan(id, num, project_id, group_id, module_id, name, status, type, tags, create_time, create_user, update_time, update_user, planned_start_time, planned_end_time, actual_start_time, actual_end_time, description)
VALUES ('test_plan_associate_case_gyq_one', 500, 'test_plan_associate_case_project', 'NONE', 'case_plan_module', 'test_plan_associate_case_name_one', 'PREPARED', 'TEST_PLAN', null, UNIX_TIMESTAMP() * 1000,'admin',
        UNIX_TIMESTAMP() * 1000,'admin',UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, null),
       ('test_plan_associate_case_gyq_two', 500, 'test_plan_associate_case_project', 'NONE', 'case_plan_module', 'test_plan_associate_case_name_two', 'PREPARED', 'TEST_PLAN', null, UNIX_TIMESTAMP() * 1000,'admin',
        UNIX_TIMESTAMP() * 1000,'admin',UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, null);


INSERT INTO project(id, num, organization_id, name, description, create_time, update_time, update_user, create_user, delete_time, deleted, delete_user, enable, module_setting)
    VALUE ('test_plan_associate_case_project', null, 'organization_plan_associate_case_project', 'test_plan_associate_case_name', null,
           UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', null, false, null, true, null);


INSERT INTO test_plan_functional_case(id, test_plan_id, functional_case_id, create_time, create_user, last_exec_time, last_exec_result, pos, test_plan_collection_id)
VALUES ('associate_case_plan_gyq_one','test_plan_associate_case_gyq_one', 'gyq_associate_function_case', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'SUCCESS', 200, '123'),
       ('associate_case_plan_gyq_three','test_plan_associate_case_gyq_one', 'gyq_associate_function_case', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'SUCCESS', 200, '123'),
       ('associate_case_plan_gyq_two','test_plan_associate_case_gyq_two', 'gyq_associate_function_case', UNIX_TIMESTAMP() * 1000, 'admin', UNIX_TIMESTAMP() * 1000, 'SUCCESS', 200, '123');