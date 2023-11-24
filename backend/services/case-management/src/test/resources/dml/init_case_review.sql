
INSERT INTO organization(id, num, name, description, create_time, update_time, create_user, update_user, deleted, delete_user, delete_time) VALUE
    ('organization-gyq-case-review-test', null, 'organization-gyq-case-review-test', 'organization-gyq-case-review-test', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'admin', 0, null, null);
INSERT INTO project (id, num, organization_id, name, description, create_user, update_user, create_time, update_time) VALUES
                                                                                                                          ('project-gyq-case-review-test', null, 'organization-gyq-case-review-test', '用例评审项目', '系统默认创建的项目', 'admin', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);


INSERT INTO template (id, name, remark, internal, update_time, create_time, create_user, scope_type, scope_id, enable_third_part, ref_id, scene)
VALUES ('test_template_case_review_gyq_id', 'functional_case_review_gyq', '', b'0', 1696992836000, 1696992836000, 'admin', 'PROJECT', 'project-gyq-case-review-test', b'0', NULL, 'FUNCTIONAL');

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('CASE_REVIEW_TEST_GYQ_ID', 1, 'CASE_REVIEW_TEST_MODULE_ID', 'project-gyq-case-review-test', '100001', '关联需求测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'CASE_REVIEW_TEST_GYQ_ID', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('CASE_REVIEW_TEST_GYQ_ID2', 1, 'CASE_REVIEW_TEST_MODULE_ID', 'project-gyq-case-review-test', '100001', '关联需求测试', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'CASE_REVIEW_TEST_GYQ_ID2', 'UN_EXECUTED', true, b'0', b'0', 'gyq', 'gyq', '', 1698058347559, 1698058347559, NULL);

INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('CASE_REVIEW_TEST_GYQ_ID', 'gyq_custom_id_review1', '22');
INSERT INTO functional_case_custom_field(case_id, field_id, value) VALUES ('CASE_REVIEW_TEST_GYQ_ID', 'gyq_custom_id_review2', '33');



INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id_review1', 'functional_priority', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'test_template_case_review_gyq_id');

INSERT INTO custom_field(id, name, scene, `type`, remark, internal, scope_type, create_time, update_time, create_user, scope_id)
VALUES('gyq_custom_id_review2', 'level', 'FUNCTIONAL', 'SELECT', '', 1, 'ORGANIZATION', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'admin', 'test_template_case_review_gyq_id');

INSERT INTO functional_case_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user) VALUES ('CASE_REVIEW_TEST_MODULE_ID', 'project-gyq-case-review-test', '测试用例评审所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin');

INSERT INTO case_review_module(id, project_id, name, parent_id, pos, create_time, update_time, create_user, update_user) VALUES ('CASE_REVIEW_REAL_MODULE_ID', 'project-gyq-case-review-test', '用例评审所属模块', 'NONE', 0, 1669174143999, 1669174143999, 'admin', 'admin');

