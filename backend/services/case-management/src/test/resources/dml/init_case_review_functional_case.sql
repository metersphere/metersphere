
INSERT INTO case_review(id, num, name, module_id, project_id, status, review_pass_rule, pos, start_time, end_time, case_count, pass_rate, tags, description, create_time, create_user, update_time, update_user)
VALUES ('case_review_associate_Id',10001,'用例关系名称1', 'test_module_one', 'case_review_relevence_project_gyq', 'COMPLETED', 'SINGLE', 001, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin'),
       ('case_review_associate_Id2',10002,'用例关系名称2', 'test_module_one', 'case_review_relevence_project_gyq', 'COMPLETED', 'SINGLE', 001, null, null, 1,100.00,null,null,1698058347559,'admin',1698058347559,'admin');

INSERT INTO case_review_functional_case(id, review_id, case_id, status, create_time, create_user, update_time)
VALUES ('associate_id1', 'case_review_associate_Id', 'associate_case_gyq_id', 'PASS', 1698058347559,'admin',1698058347559),
       ('associate_id2', 'case_review_associate_Id2', 'associate_case_gyq_id', 'PASS', 1698058347559,'admin',1698058347559);












